package com.pedro.racasclasses.event;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.pedro.racasclasses.RacasClasses;
import com.pedro.racasclasses.attribute.AttributeBonus;
import com.pedro.racasclasses.attribute.AttributeData;
import com.pedro.racasclasses.capability.ModAttachments;
import com.pedro.racasclasses.capability.PlayerRaceData;
import com.pedro.racasclasses.race.Race;
import com.pedro.racasclasses.race.RaceRegistry;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = RacasClasses.MODID)
public class RaceCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
                Commands.literal("raca")
                        .then(Commands.literal("escolher")
                                .then(Commands.argument("raca", StringArgumentType.word())
                                        .executes(ctx -> {
                                            String raceName = StringArgumentType.getString(ctx, "raca");
                                            return chooseRace(ctx.getSource(), raceName, null);
                                        })
                                        .then(Commands.argument("subraca", StringArgumentType.word())
                                                .executes(ctx -> {
                                                    String raceName = StringArgumentType.getString(ctx, "raca");
                                                    String subrace = StringArgumentType.getString(ctx, "subraca");
                                                    return chooseRace(ctx.getSource(), raceName, subrace);
                                                })
                                        )
                                )
                        )
                        .then(Commands.literal("ver")
                                .executes(ctx -> showRace(ctx.getSource()))
                        )
                        .then(Commands.literal("reset")
                                .executes(ctx -> resetRace(ctx.getSource()))
                        )
                        .then(Commands.literal("breath")
                                .executes(ctx -> useBreath(ctx.getSource()))
                        )
                        .then(Commands.literal("listar")
                                .executes(ctx -> listRaces(ctx.getSource()))
                        )
        );
    }

    private static int useBreath(CommandSourceStack source) {
        ServerPlayer player;
        try {
            player = source.getPlayerOrException();
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cEsse comando só pode ser usado por um jogador."));
            return 0;
        }

        boolean success = BreathWeapon.tryUseBreath(player);
        return success ? 1 : 0;
    }

    private static int chooseRace(CommandSourceStack source, String raceName, String subraceName) {
        ServerPlayer player;
        try {
            player = source.getPlayerOrException();
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cEsse comando só pode ser usado por um jogador."));
            return 0;
        }

        Race race = RaceRegistry.get(raceName);
        if (race == null) {
            source.sendFailure(Component.literal("§cRaça desconhecida: §f" + raceName));
            source.sendFailure(Component.literal("§7Use §f/raca listar§7 pra ver todas."));
            return 0;
        }

        String validatedSubrace = null;
        if (race.hasSubrace() && subraceName != null) {
            String normalized = subraceName.toLowerCase().replace(" ", "").replace("_", "");
            for (String valid : race.getSubraceIds()) {
                if (valid.equalsIgnoreCase(normalized)) {
                    validatedSubrace = valid;
                    break;
                }
            }
            if (validatedSubrace == null) {
                source.sendFailure(Component.literal("§cSub-raça desconhecida: §f" + subraceName));
                source.sendFailure(Component.literal("§7Opções: §f" + String.join(", ", race.getSubraceIds())));
                return 0;
            }
        }

        PlayerRaceData data = player.getData(ModAttachments.PLAYER_RACE);
        Race oldRace = RaceRegistry.get(data.getRaceId());

        // Chama onRaceExit da raça antiga (se houver)
        if (oldRace != null && !oldRace.getId().equals(race.getId())) {
            oldRace.onRaceExit(player);
        }

        data.setRaceId(race.getId());

        // ASI racial primeiro; sub-raça soma em onSubraceChosen.
        race.applyInitialAttributes(player);

        // Sub-raça
        if (race.hasSubrace()) {
            String subraceId;
            if (validatedSubrace != null) {
                subraceId = validatedSubrace;
                race.onSubraceChosen(player, subraceId);
            } else {
                String[] options = race.getSubraceIds();
                subraceId = options[player.getRandom().nextInt(options.length)];
                race.onSubraceChosen(player, subraceId);
                String displayName = race.getSubraceDisplayName(player);
                source.sendSuccess(() -> Component.literal("§7Você não escolheu uma sub-raça — sorteamos: §f" + displayName), false);
            }
        }

        RaceEventHandler.applyRaceAttributes(player);
        AttributeBonus.applyVanillaModifiers(player);

        // Chama onRaceEnter da nova raça
        race.onRaceEnter(player);
        player.syncData(ModAttachments.PLAYER_RACE);
        player.syncData(ModAttachments.PLAYER_ATTRIBUTES);

        source.sendSuccess(() -> Component.literal("§aVocê agora é um §f" + race.getDisplayName() + "§a!"), false);
        if (race.hasSubrace()) {
            String sub = race.getSubraceDisplayName(player);
            source.sendSuccess(() -> Component.literal("§7Sub-raça: §f" + sub), false);
        }
        AttributeData chosen = player.getData(ModAttachments.PLAYER_ATTRIBUTES);
        source.sendSuccess(() -> Component.literal(String.format(
                "§7Início: §fSTR %d DEX %d CON %d INT %d WIS %d LUCK %d §7| pontos livres: §f%d",
                chosen.getStrengthLevel(), chosen.getDexterityLevel(), chosen.getConstitutionLevel(),
                chosen.getIntelligenceLevel(), chosen.getWisdomLevel(), chosen.getLuckLevel(),
                chosen.getAvailablePoints())), false);

        return 1;
    }

    private static int showRace(CommandSourceStack source) {
        ServerPlayer player;
        try {
            player = source.getPlayerOrException();
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cEsse comando só pode ser usado por um jogador."));
            return 0;
        }

        PlayerRaceData data = player.getData(ModAttachments.PLAYER_RACE);
        Race race = RaceRegistry.get(data.getRaceId());

        if (race == null) {
            source.sendSuccess(() -> Component.literal("§eVocê ainda não escolheu uma raça. Use §f/raca listar§e."), false);
            return 1;
        }

        source.sendSuccess(() -> Component.literal("§7Raça: §f" + race.getDisplayName()), false);
        if (race.hasSubrace()) {
            String sub = race.getSubraceDisplayName(player);
            source.sendSuccess(() -> Component.literal("§7Sub-raça: §f" + sub), false);
        }

        AttributeData attrs = player.getData(ModAttachments.PLAYER_ATTRIBUTES);
        source.sendSuccess(() -> Component.literal(String.format(
                "§7Atributos: §fSTR %d §7/ §fDEX %d §7/ §fCON %d §7/ §fINT %d §7/ §fWIS %d §7/ §fLUCK %d §7| livres: §f%d",
                attrs.getStrengthLevel(), attrs.getDexterityLevel(), attrs.getConstitutionLevel(),
                attrs.getIntelligenceLevel(), attrs.getWisdomLevel(), attrs.getLuckLevel(),
                attrs.getAvailablePoints())), false);

        return 1;
    }

    private static int resetRace(CommandSourceStack source) {
        ServerPlayer player;
        try {
            player = source.getPlayerOrException();
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cEsse comando só pode ser usado por um jogador."));
            return 0;
        }

        PlayerRaceData data = player.getData(ModAttachments.PLAYER_RACE);
        Race oldRace = RaceRegistry.get(data.getRaceId());

        // Chama onRaceExit da raça antiga
        if (oldRace != null) {
            oldRace.onRaceExit(player);
        }

        data.setRaceId("none");
        data.setDragonbornSubrace("red");
        data.setElfSubrace("wood");
        data.setGnomeSubrace("forest");
        data.setHalflingSubrace("lightfoot");
        data.setTieflingSubrace("asmodeus");

        // Zera trilhas e pontos livres. Nível de personagem e XP ficam
        // (o player não perde progresso de cap); na próxima escolha os
        // pontos de level-up voltam pela fórmula nível−1.
        AttributeData attrs = player.getData(ModAttachments.PLAYER_ATTRIBUTES);
        attrs.resetAllocatedAttributes();

        RaceEventHandler.applyRaceAttributes(player);
        AttributeBonus.applyVanillaModifiers(player);
        player.syncData(ModAttachments.PLAYER_RACE);
        player.syncData(ModAttachments.PLAYER_ATTRIBUTES);

        source.sendSuccess(() -> Component.literal("§aRaça resetada. Atributos raciais zerados."), false);
        return 1;
    }

    private static int listRaces(CommandSourceStack source) {
        var races = RaceRegistry.getAll();

        if (races.isEmpty()) {
            source.sendFailure(Component.literal("§cNenhuma raça registrada!"));
            return 0;
        }

        source.sendSuccess(() -> Component.literal("§7Raças disponíveis (§f" + races.size() + "§7):"), false);

        for (var race : races) {
            String id = race.getId();
            String name = race.getDisplayName();

            StringBuilder line = new StringBuilder("§f- §a" + id + " §7→ §f" + name);

            if (race.hasSubrace()) {
                line.append(" §7(subs: §f")
                        .append(String.join(", ", race.getSubraceIds()))
                        .append("§7)");
            }

            source.sendSuccess(() -> Component.literal(line.toString()), false);
        }
        return 1;
    }
}
