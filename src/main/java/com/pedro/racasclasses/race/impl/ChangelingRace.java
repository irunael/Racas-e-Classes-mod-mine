package com.pedro.racasclasses.race.impl;

import com.mojang.authlib.GameProfile;
import com.pedro.racasclasses.race.Race;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public class ChangelingRace implements Race {

    private static final Map<UUID, Long> ABILITY_COOLDOWNS = new HashMap<>();
    private static final Map<UUID, DisguiseData> ACTIVE_DISGUISES = new HashMap<>();
    private static final String ORIGINAL_NAME = "changeling_original_name";
    private static final String DISGUISED_UNTIL = "changeling_disguised_until";
    private static final String DISGUISED_AS = "changeling_disguised_as";

    private static class DisguiseData {
        final String playerName;
        final GameProfile targetProfile;
        final long endTick;
        
        DisguiseData(String playerName, GameProfile targetProfile, long endTick) {
            this.playerName = playerName;
            this.targetProfile = targetProfile;
            this.endTick = endTick;
        }
    }

    @Override
    public String getId() { return "changeling"; }

    @Override
    public String getDisplayName() { return "Changeling"; }

    @Override
    public double getMovementSpeed() { return 0.11; }

    // ===== Shape Change (tecla H) - Copia skin e nome de um player =====

    @Override
    public boolean canUseAbility() { return true; }

    @Override
    public void executeAbility(ServerPlayer player) {
        long currentTick = player.serverLevel().getServer().getTickCount();
        Long readyAt = ABILITY_COOLDOWNS.get(player.getUUID());

        if (readyAt != null && currentTick < readyAt) {
            player.sendSystemMessage(Component.literal(String.format(
                    "§cShape Change em recarga: §f%.1fs", (readyAt - currentTick) / 20.0)));
            return;
        }

        // Lista todos os players online (exceto ele mesmo)
        var players = player.serverLevel().getServer().getPlayerList().getPlayers().stream()
                .filter(p -> p != player)
                .toList();

        if (players.isEmpty()) {
            player.sendSystemMessage(Component.literal("§cNenhum jogador disponível para copiar!"));
            return;
        }

        // Menu simplificado: mostra lista no chat e permite escolha por comando
        player.sendSystemMessage(Component.literal("§a=== Escolha um jogador para copiar ==="));
        for (int i = 0; i < players.size(); i++) {
            player.sendSystemMessage(Component.literal(String.format("§e%d. §f%s", 
                i + 1, players.get(i).getName().getString())));
        }
        player.sendSystemMessage(Component.literal("§7Digite no chat: §e1 §7até §e" + players.size()));
        
        // Salva lista temporária para escolha
        player.getPersistentData().putInt("changeling_choosing", 1);
        StringBuilder sb = new StringBuilder();
        for (ServerPlayer p : players) {
            sb.append(p.getName().getString()).append(",");
        }
        player.getPersistentData().putString("changeling_player_list", sb.toString());
    }
    
    @Override
    public void onPlayerTick(ServerPlayer player) {
        long currentTick = player.serverLevel().getServer().getTickCount();
        
        // Checa se o disfarce expirou
        if (player.getPersistentData().contains(DISGUISED_UNTIL)) {
            long endTick = player.getPersistentData().getLong(DISGUISED_UNTIL);
            
            if (currentTick >= endTick) {
                // Remove disfarce
                restoreOriginalName(player);
                player.sendSystemMessage(Component.literal("§7Seu disfarce acabou."));
            }
        }
    }
    
    // Método para aplicar disfarce (chamado pelo comando ou chat handler)
    public static void applyDisguise(ServerPlayer player, String targetName) {
        long currentTick = player.serverLevel().getServer().getTickCount();
        
        // Procura o player alvo
        ServerPlayer target = player.serverLevel().getServer().getPlayerList()
                .getPlayerByName(targetName);
        
        if (target == null) {
            player.sendSystemMessage(Component.literal("§cJogador não encontrado!"));
            return;
        }
        
        // Salva nome original
        if (!player.getPersistentData().contains(ORIGINAL_NAME)) {
            player.getPersistentData().putString(ORIGINAL_NAME, 
                player.getName().getString());
        }
        
        // Aplica disfarce por 60 segundos
        long endTick = currentTick + 1200; // 60 segundos
        player.getPersistentData().putLong(DISGUISED_UNTIL, endTick);
        player.getPersistentData().putString(DISGUISED_AS, targetName);
        
        // Muda nome customizado (aparece acima da cabeça)
        player.setCustomName(Component.literal(targetName));
        player.setCustomNameVisible(true);
        
        // Cooldown de 120 segundos
        ABILITY_COOLDOWNS.put(player.getUUID(), currentTick + 2400);
        
        player.sendSystemMessage(Component.literal(
            "§aVocê assumiu a forma de §e" + targetName + " §apor 60 segundos!"));
        
        // Limpa flags de escolha
        player.getPersistentData().remove("changeling_choosing");
        player.getPersistentData().remove("changeling_player_list");
    }
    
    private static void restoreOriginalName(ServerPlayer player) {
        player.setCustomNameVisible(false);
        player.setCustomName(null);
        player.getPersistentData().remove(DISGUISED_UNTIL);
        player.getPersistentData().remove(DISGUISED_AS);
    }
}

