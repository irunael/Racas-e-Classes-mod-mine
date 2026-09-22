package com.pedro.racasclasses.race.impl;

import com.pedro.racasclasses.RacasClasses;
import com.pedro.racasclasses.race.Race;
import com.pedro.racasclasses.race.RaceElytra;
import com.pedro.racasclasses.race.RacialWeakness;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AasimarRace implements Race {

    // ===== Healing Hands =====
    private static final Map<UUID, Long> HEAL_COOLDOWNS = new HashMap<>();
    private static final int HEAL_COOLDOWN_TICKS = 1200; // 60s
    private static final float HEAL_AMOUNT = 8.0f;       // 4 corações

    // ===== Super Jump =====
    private static final Map<UUID, Long> SUPER_JUMP_COOLDOWNS = new HashMap<>();
    private static final int SUPER_JUMP_COOLDOWN_TICKS = 200; // 10s

    @Override
    public String getId() { return "aasimar"; }

    @Override
    public String getDisplayName() { return "Aasimar"; }

    @Override
    public double getMaxHealth() { return 24.0; }

    @Override
    public boolean hasNightVision() { return true; }

    // ===== Dano +20% corpo a corpo =====

    @Override
    public void onAttackEntity(ServerPlayer player, LivingIncomingDamageEvent event) {
        event.setAmount(event.getAmount() * 1.20f);
    }

    // ===== Resistências + Fraquezas =====

    @Override
    public void onPlayerHurt(ServerPlayer player, LivingIncomingDamageEvent event) {
        DamageSource source = event.getSource();

        // --- Resistência: Wither (50%) ---
        if (source.is(DamageTypes.WITHER)) {
            event.setAmount(event.getAmount() * 0.5f);
            return;
        }

        // Fraqueza nova: +30% magia. A redução antiga de 50% em MAGIC
        // saiu pra essa fraqueza ser sentida; Wither continua reduzido.
        if (source.is(DamageTypes.MAGIC) || source.is(DamageTypes.INDIRECT_MAGIC)
                || source.is(DamageTypes.DRAGON_BREATH)) {
            RacialWeakness.scale(event, 1.3f);
            return;
        }

        // --- Fraqueza: Fogo/Lava (+50%) ---
        boolean isFire =
                source.is(DamageTypes.IN_FIRE)
                        || source.is(DamageTypes.ON_FIRE)
                        || source.is(DamageTypes.LAVA)
                        || source.is(DamageTypes.HOT_FLOOR);

        if (isFire) {
            event.setAmount(event.getAmount() * 1.5f);
            return;
        }

        // --- Fraqueza: Armas de contato (+20%) ---
        boolean isWeapon =
                source.is(DamageTypes.PLAYER_ATTACK)
                        || source.is(DamageTypes.MOB_ATTACK)
                        || source.is(DamageTypes.MOB_ATTACK_NO_AGGRO)
                        || source.is(DamageTypes.ARROW)
                        || source.is(DamageTypes.TRIDENT)
                        || source.is(DamageTypes.MOB_PROJECTILE)
                        || source.is(DamageTypes.THROWN);

        if (isWeapon) {
            event.setAmount(event.getAmount() * 1.2f);
        }
    }

    // ===== Entrada/Saída: elytra =====

    @Override
    public void onRaceEnter(ServerPlayer player) {
        equipElytra(player);
        RacasClasses.LOGGER.info("[AASIMAR] Elytra equipada!");
    }

    @Override
    public void onRaceExit(ServerPlayer player) {
        removeElytra(player);
        RacasClasses.LOGGER.info("[AASIMAR] Elytra removida!");
    }

    @Override
    public void onPlayerRespawn(ServerPlayer player) {
        equipElytra(player);
        RacasClasses.LOGGER.info("[AASIMAR] Elytra re-equipada após morrer!");
    }

    // ===== Habilidade ativa (tecla H) =====

    @Override
    public boolean canUseAbility() { return true; }

    @Override
    public void executeAbility(ServerPlayer player) {
        long currentTick = player.serverLevel().getServer().getTickCount();
        Long readyAt = HEAL_COOLDOWNS.get(player.getUUID());

        if (readyAt != null && currentTick < readyAt) {
            double seconds = (readyAt - currentTick) / 20.0;
            player.sendSystemMessage(Component.literal(
                    String.format("§cCura em recarga: §f%.1fs", seconds)));
            return;
        }

        // Cura (não passa do máximo)
        float newHealth = Math.min(player.getHealth() + HEAL_AMOUNT, player.getMaxHealth());
        player.setHealth(newHealth);

        HEAL_COOLDOWNS.put(player.getUUID(), currentTick + HEAL_COOLDOWN_TICKS);
        player.sendSystemMessage(Component.literal("§aHealing Hands ativado!"));
    }

    // ===== Super Jump (tecla R) =====

    @Override
    public boolean canSuperJump() { return true; }

    @Override
    public void executeSuperJump(ServerPlayer player) {
        if (!player.onGround()) return;

        long currentTick = player.serverLevel().getServer().getTickCount();
        Long readyAt = SUPER_JUMP_COOLDOWNS.get(player.getUUID());

        if (readyAt != null && currentTick < readyAt) {
            double seconds = (readyAt - currentTick) / 20.0;
            player.sendSystemMessage(Component.literal(
                    String.format("§cSuper pulo em recarga: §f%.1fs", seconds)));
            return;
        }

        player.setDeltaMovement(0, 1.0, 0);
        player.hurtMarked = true;

        SUPER_JUMP_COOLDOWNS.put(player.getUUID(), currentTick + SUPER_JUMP_COOLDOWN_TICKS);
    }

    // ===== Helpers da elytra (reaproveita do Aarakocra) =====

    public static ItemStack createElytra(ServerPlayer player) {
        return RaceElytra.create(player);
    }

    private void equipElytra(ServerPlayer player) {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (RaceElytra.isRaceElytra(chest)) return;

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
}
