package com.pedro.racasclasses.network;

import com.pedro.racasclasses.RacasClasses;
import com.pedro.racasclasses.capability.ModAttachments;
import com.pedro.racasclasses.capability.PlayerRaceData;
import com.pedro.racasclasses.capability.PlayerClassData;
import com.pedro.racasclasses.race.Race;
import com.pedro.racasclasses.race.RaceRegistry;
import com.pedro.racasclasses.event.IllusionistAbilities;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

@EventBusSubscriber(modid = RacasClasses.MODID)
public class ServerPayloadHandler {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("2");

        registrar.playToServer(
                SuperJumpPayload.TYPE,
                SuperJumpPayload.STREAM_CODEC,
                ServerPayloadHandler::handleSuperJump
        );

        registrar.playToServer(
                AbilityPayload.TYPE,
                AbilityPayload.STREAM_CODEC,
                ServerPayloadHandler::handleAbility
        );

        registrar.playToServer(
                ChooseRacePayload.TYPE,
                ChooseRacePayload.STREAM_CODEC,
                ServerPayloadHandler::handleChooseRace
        );

        registrar.playToServer(ClassPrimaryPayload.TYPE, ClassPrimaryPayload.STREAM_CODEC,
                ServerPayloadHandler::handleClassPrimary);
        registrar.playToServer(ClassSecondaryPayload.TYPE, ClassSecondaryPayload.STREAM_CODEC,
                ServerPayloadHandler::handleClassSecondary);
    }

    private static void handleClassPrimary(ClassPrimaryPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) IllusionistAbilities.useIllusions(player);
        });
    }

    private static void handleClassSecondary(ClassSecondaryPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) IllusionistAbilities.useDaggers(player);
        });
    }

    private static void handleSuperJump(SuperJumpPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            PlayerRaceData data = player.getData(ModAttachments.PLAYER_RACE);
            Race race = RaceRegistry.get(data.getRaceId());

            if (race == null) return;
            if (!race.canSuperJump()) return;

            race.executeSuperJump(player);
        });
    }

    private static void handleAbility(AbilityPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            PlayerRaceData data = player.getData(ModAttachments.PLAYER_RACE);
            Race race = RaceRegistry.get(data.getRaceId());

            if (race == null) return;
            if (!race.canUseAbility()) return;

            race.executeAbility(player);
        });
    }

    private static void handleChooseRace(ChooseRacePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            Race race = RaceRegistry.get(payload.raceId());
            if (race == null) return;

            PlayerRaceData data = player.getData(ModAttachments.PLAYER_RACE);
            PlayerClassData classData = player.getData(ModAttachments.PLAYER_CLASS);
            boolean isNewRace = data.getRaceId().equals("none");

            // O assistente só completa uma ficha nova/incompleta; nunca troca
            // silenciosamente escolhas já gravadas por um pacote adulterado.
            if (!isNewRace && !data.getRaceId().equals(race.getId())) return;
            if (!classData.getClassId().equals("none")
                    && (!classData.getClassId().equals(payload.classId())
                    || !classData.getSubclassId().equals(payload.subclassId()))) return;
            if (!payload.classId().equals("rogue") || !payload.subclassId().equals("illusionist")) return;

            String validatedSubrace = "none";
            if (race.hasSubrace()) {
                for (String option : race.getSubraceIds()) {
                    if (option.equalsIgnoreCase(payload.subraceId())) {
                        validatedSubrace = option;
                        break;
                    }
                }
                if (validatedSubrace.equals("none")) return;
            }

            data.setRaceId(race.getId());
            if (race.hasSubrace()) {
                race.onSubraceChosen(player, validatedSubrace);
            }

            classData.setClassId(payload.classId());
            classData.setSubclassId(payload.subclassId());

            com.pedro.racasclasses.event.RaceEventHandler.applyRaceAttributes(player);
            if (isNewRace) race.onRaceEnter(player);
            player.syncData(ModAttachments.PLAYER_RACE);
            player.syncData(ModAttachments.PLAYER_CLASS);

            String message = "§aRaça escolhida: §f" + race.getDisplayName();
            if (race.hasSubrace()) {
                message += " §7— linhagem: §f" + race.getSubraceDisplayName(player);
            }
            message += " §7— classe: §fLadino §7— arquétipo: §dIlusionista";
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(message));
        });
    }
}
