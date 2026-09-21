package com.pedro.racasclasses.race.impl;

import com.pedro.racasclasses.RacasClasses;
import com.pedro.racasclasses.race.Race;
import com.pedro.racasclasses.race.RaceElytra;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.network.chat.Component;

public class AarakocraRace implements Race {

    @Override
    public String getId() { return "aarakocra"; }

    @Override
    public String getDisplayName() { return "Aarakocra"; }

    @Override
    public double getMovementSpeed() { return 0.10; }

    // ===== Talons: +3 de dano desarmado (total 4) =====

    @Override
    public void onAttackEntity(ServerPlayer player, LivingIncomingDamageEvent event) {
        if (!player.getMainHandItem().isEmpty()) return;
        event.setAmount(event.getAmount() + 3.0f);
    }

    // ===== Entrada na raça: equipa elytra =====

    @Override
    public void onRaceEnter(ServerPlayer player) {
        equipElytra(player);
        RacasClasses.LOGGER.info("[AARAKOCRA] Elytra equipada!");
    }

    // ===== Saída da raça: remove elytra =====

    @Override
    public void onRaceExit(ServerPlayer player) {
        removeElytra(player);
        RacasClasses.LOGGER.info("[AARAKOCRA] Elytra removida!");
    }

    // ===== Ressurreição: re-equipa =====

    @Override
    public void onPlayerRespawn(ServerPlayer player) {
        equipElytra(player);
        RacasClasses.LOGGER.info("[AARAKOCRA] Elytra re-equipada após morrer!");
    }

    // ===== Helpers =====

    public static ItemStack createElytra(ServerPlayer player) {
        return RaceElytra.create(player);
    }

    private void equipElytra(ServerPlayer player) {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);

        if (RaceElytra.isRaceElytra(chest)) return;

        // Se tinha armadura, joga pro inventário
        if (!chest.isEmpty()) {
            if (!player.getInventory().add(chest.copy())) {
                player.drop(chest.copy(), false);
            }
        }

        player.setItemSlot(EquipmentSlot.CHEST, createElytra(player));
    }

    private void removeElytra(ServerPlayer player) {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (RaceElytra.isRaceElytra(chest)) {
            player.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
        }
    }
    // ===== Super Jump =====

    private static final Map<UUID, Long> SUPER_JUMP_COOLDOWNS = new HashMap<>();
    private static final int SUPER_JUMP_COOLDOWN_TICKS = 200; // 10s

    @Override
    public boolean canSuperJump() { return true; }

    @Override
    public void executeSuperJump(ServerPlayer player) {
        // Só pula no chão
        if (!player.onGround()) return;

        long currentTick = player.serverLevel().getServer().getTickCount();
        Long readyAt = SUPER_JUMP_COOLDOWNS.get(player.getUUID());

        // Tá em cooldown?
        if (readyAt != null && currentTick < readyAt) {
            double seconds = (readyAt - currentTick) / 20.0;
            player.sendSystemMessage(Component.literal(String.format("§cSuper pulo em recarga: §f%.1fs", seconds)));
            return;
        }

        // Aplica o pulo (5 blocos de altura)
        player.setDeltaMovement(0, 1.0, 0);
        player.hurtMarked = true;

        // Marca o cooldown
        SUPER_JUMP_COOLDOWNS.put(player.getUUID(), currentTick + SUPER_JUMP_COOLDOWN_TICKS);
    }
}
