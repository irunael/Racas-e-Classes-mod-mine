package com.pedro.racasclasses.event;

import com.pedro.racasclasses.RacasClasses;
import com.pedro.racasclasses.capability.ModAttachments;
import com.pedro.racasclasses.capability.PlayerClassData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = RacasClasses.MODID)
public final class ClassCommand {
    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("classe")
                .then(Commands.literal("escolher")
                        .then(Commands.literal("ladino")
                                .executes(ctx -> choose(ctx.getSource()))
                                .then(Commands.literal("ilusionista").executes(ctx -> choose(ctx.getSource())))))
                .then(Commands.literal("ver").executes(ctx -> show(ctx.getSource())))
                .then(Commands.literal("listar").executes(ctx -> list(ctx.getSource())))
                .then(Commands.literal("reset").executes(ctx -> reset(ctx.getSource()))));
    }

    private static ServerPlayer player(CommandSourceStack source) {
        try { return source.getPlayerOrException(); }
        catch (Exception ignored) { source.sendFailure(Component.literal("§cComando exclusivo para jogadores.")); return null; }
    }

    private static int choose(CommandSourceStack source) {
        ServerPlayer player = player(source); if (player == null) return 0;
        PlayerClassData data = player.getData(ModAttachments.PLAYER_CLASS);
        data.setClassId("rogue"); data.setSubclassId("illusionist");
        player.syncData(ModAttachments.PLAYER_CLASS);
        source.sendSuccess(() -> Component.literal("§aClasse escolhida: §fLadino §7— §dIlusionista§a. Use §fC§a e §fZ§a."), false);
        return 1;
    }

    private static int show(CommandSourceStack source) {
        ServerPlayer player = player(source); if (player == null) return 0;
        PlayerClassData data = player.getData(ModAttachments.PLAYER_CLASS);
        String text = data.getClassId().equals("rogue") ? "§7Classe: §fLadino §7— §dIlusionista" : "§eVocê ainda não escolheu uma classe.";
        source.sendSuccess(() -> Component.literal(text), false); return 1;
    }

    private static int list(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("§f- §aLadino §7(subclasse: §dIlusionista§7)"), false);
        source.sendSuccess(() -> Component.literal("§7Escolha com §f/classe escolher ladino ilusionista"), false); return 1;
    }

    private static int reset(CommandSourceStack source) {
        ServerPlayer player = player(source); if (player == null) return 0;
        PlayerClassData data = player.getData(ModAttachments.PLAYER_CLASS);
        data.setClassId("none"); data.setSubclassId("none"); player.syncData(ModAttachments.PLAYER_CLASS);
        source.sendSuccess(() -> Component.literal("§aClasse resetada."), false); return 1;
    }

    @SubscribeEvent
    public static void cloneData(PlayerEvent.Clone event) {
        if (!event.isWasDeath() || !(event.getOriginal() instanceof ServerPlayer oldPlayer)
                || !(event.getEntity() instanceof ServerPlayer newPlayer)) return;
        PlayerClassData oldData = oldPlayer.getData(ModAttachments.PLAYER_CLASS);
        PlayerClassData newData = newPlayer.getData(ModAttachments.PLAYER_CLASS);
        newData.setClassId(oldData.getClassId()); newData.setSubclassId(oldData.getSubclassId());
        newPlayer.syncData(ModAttachments.PLAYER_CLASS);
    }
}
