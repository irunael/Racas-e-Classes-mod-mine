package com.pedro.racasclasses.race.impl;

import com.pedro.racasclasses.race.Race;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class KoboldRace implements Race {

    // ===== Draconic Cry (tecla H) =====
    private static final Map<UUID, Long> ABILITY_COOLDOWNS = new HashMap<>();
    private static final int ABILITY_COOLDOWN_TICKS = 600;  // 30s
    private static final int STRENGTH_DURATION = 100;       // 5s
    private static final int REGEN_DURATION = 300;          // 15s

    @Override
    public String getId() { return "kobold"; }

    @Override
    public String getDisplayName() { return "Kobold"; }

    @Override
    public double getMaxHealth() { return 16.0; }

    @Override
    public double getMovementSpeed() { return 0.12; }

    @Override
    public double getScale() { return 0.50; }

    @Override
    public boolean hasNightVision() { return true; }

    // ===== Draconic Cry =====

    @Override
    public boolean canUseAbility() { return true; }

    @Override
    public void executeAbility(ServerPlayer player) {
        long currentTick = player.serverLevel().getServer().getTickCount();
        Long readyAt = ABILITY_COOLDOWNS.get(player.getUUID());

        if (readyAt != null && currentTick < readyAt) {
            double seconds = (readyAt - currentTick) / 20.0;
            player.sendSystemMessage(Component.literal(
                    String.format("§cDraconic Cry em recarga: §f%.1fs", seconds)));
            return;
        }

        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, STRENGTH_DURATION, 0, false, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, REGEN_DURATION, 0, false, false, true));

        ABILITY_COOLDOWNS.put(player.getUUID(), currentTick + ABILITY_COOLDOWN_TICKS);
        player.sendSystemMessage(Component.literal("§aDraconic Cry ativado!"));
    }

    // ===== Pack Tactics + Sunlight Sensitivity =====

    @Override
    public void onAttackEntity(ServerPlayer player, LivingIncomingDamageEvent event) {
        // Pack Tactics: +10% dano se tiver aliado perto
        ServerLevel level = player.serverLevel();
        AABB area = player.getBoundingBox().inflate(5.0);
        List<Player> allies = level.getEntitiesOfClass(Player.class, area,
                p -> p != player && p.isAlive());

        if (!allies.isEmpty()) {
            event.setAmount(event.getAmount() * 1.10f);
        }
    }

    @Override
    public void onPlayerTick(ServerPlayer player) {
        if (player.tickCount % 20 != 0) return;

        ServerLevel level = player.serverLevel();

        // Sunlight Sensitivity: Weakness no sol
        if (level.isDay() && level.canSeeSky(player.blockPosition().above())) {
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 0, false, false, false));
        }
    }

    // ===== Fraqueza: +20% dano no sol =====

    @Override
    public void onPlayerHurt(ServerPlayer player, LivingIncomingDamageEvent event) {
        ServerLevel level = player.serverLevel();

        if (level.isDay() && level.canSeeSky(player.blockPosition().above())) {
            event.setAmount(event.getAmount() * 1.2f);
        }
    }
}