package com.pedro.racasclasses.network;

import com.pedro.racasclasses.RacasClasses;
import com.pedro.racasclasses.capability.ModAttachments;
import com.pedro.racasclasses.capability.PlayerRaceData;
import com.pedro.racasclasses.capability.PlayerClassData;
import com.pedro.racasclasses.race.Race;
import com.pedro.racasclasses.race.RaceRegistry;
import com.pedro.racasclasses.event.IllusionistAbilities;
import com.pedro.racasclasses.attribute.Attribute;
import com.pedro.racasclasses.attribute.AttributeBonus;
import com.pedro.racasclasses.attribute.AttributeData;
import com.pedro.racasclasses.client.ClientPayloadHandler;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

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
                DoubleJumpPayload.TYPE,
                DoubleJumpPayload.STREAM_CODEC,
                ServerPayloadHandler::handleDoubleJump
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

        registrar.playToServer(OpenAttributeScreenPayload.TYPE, OpenAttributeScreenPayload.STREAM_CODEC,
                ServerPayloadHandler::handleOpenAttributes);
        registrar.playToServer(DistributeAttributePayload.TYPE, DistributeAttributePayload.STREAM_CODEC,
                ServerPayloadHandler::handleDistributeAttribute);

        IPayloadHandler<OpenAttributeScreenS2CPayload> openClient = (payload, context) -> {};
        if (FMLEnvironment.dist == Dist.CLIENT) {
            openClient = ClientPayloadHandler::handleOpenAttributes;
        }
        registrar.playToClient(OpenAttributeScreenS2CPayload.TYPE, OpenAttributeScreenS2CPayload.STREAM_CODEC, openClient);
    }

    private static void handleOpenAttributes(OpenAttributeScreenPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            player.syncData(ModAttachments.PLAYER_ATTRIBUTES);
            PacketDistributor.sendToPlayer(player, new OpenAttributeScreenS2CPayload());
        });
    }

    private static void handleDistributeAttribute(DistributeAttributePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            Attribute attribute = Attribute.fromId(payload.attributeId());
            if (attribute == null) return;

            AttributeData data = player.getData(ModAttachments.PLAYER_ATTRIBUTES);
            if (!data.tryDistribute(attribute)) {
                player.displayClientMessage(Component.literal("§cSem pontos ou trilha no máximo (50)."), true);
                return;
            }

            AttributeBonus.applyVanillaModifiers(player);
            player.syncData(ModAttachments.PLAYER_ATTRIBUTES);
            player.displayClientMessage(Component.literal(
                    "§a" + attribute.getDisplayName() + " §7→ §f" + data.getLevel(attribute)), true);
        });
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

    private static void handleDoubleJump(DoubleJumpPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            PlayerRaceData data = player.getData(ModAttachments.PLAYER_RACE);
            Race race = RaceRegistry.get(data.getRaceId());

            if (race instanceof com.pedro.racasclasses.race.impl.WukongRace wukong) {
                wukong.tryDoubleJump(player);
            }
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
            if (isNewRace) {
                race.applyInitialAttributes(player);
                if (race.hasSubrace()) {
                    race.onSubraceChosen(player, validatedSubrace);
                }
            }

            classData.setClassId(payload.classId());
            classData.setSubclassId(payload.subclassId());

            com.pedro.racasclasses.event.RaceEventHandler.applyRaceAttributes(player);
            AttributeBonus.applyVanillaModifiers(player);
            if (isNewRace) race.onRaceEnter(player);
            player.syncData(ModAttachments.PLAYER_RACE);
            player.syncData(ModAttachments.PLAYER_CLASS);
            player.syncData(ModAttachments.PLAYER_ATTRIBUTES);

            String message = "§aRaça escolhida: §f" + race.getDisplayName();
            if (race.hasSubrace()) {
                message += " §7— linhagem: §f" + race.getSubraceDisplayName(player);
            }
            message += " §7— classe: §fLadino §7— arquétipo: §dIlusionista";
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(message));
        });
    }
}
