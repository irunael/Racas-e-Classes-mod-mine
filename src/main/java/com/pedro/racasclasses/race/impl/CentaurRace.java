package com.pedro.racasclasses.race.impl;

import com.pedro.racasclasses.race.Race;
import com.pedro.racasclasses.race.RacialWeakness;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CentaurRace implements Race {

    @Override
    public String getId() { return "centaur"; }

    @Override
    public String getDisplayName() { return "Centaur"; }

    @Override
    public double getMaxHealth() { return 24.0; }

    @Override
    public double getAttackDamage() { return 1.1; }

    @Override
    public double getMovementSpeed() { return 0.12; }

    @Override
    public double getScale() { return 1.15; }

    @Override
    public boolean hasNightVision() { return false; }

    // ===== Passiva: +2 dano desarmado (cascos) =====

    @Override
    public void onAttackEntity(ServerPlayer player, LivingIncomingDamageEvent event) {
        if (!player.getMainHandItem().isEmpty()) return;
        event.setAmount(event.getAmount() + 2.0f);
    }

    // ===== Passiva: Imunidade a queda (2 blocos) =====

    @Override
    public void onFall(ServerPlayer player, LivingFallEvent event) {
        if (event.getDistance() <= 2.0f) {
            event.setDamageMultiplier(0.0f);
        }
    }

    // ===== Estado do Charge =====

    private static class ChargeState {
        long startTick;
        boolean active;
    }

    private static final Map<UUID, ChargeState> CHARGE_STATES = new HashMap<>();
    private static final Map<UUID, Long> ABILITY_COOLDOWNS = new HashMap<>();
    private static final int CHARGE_DURATION_TICKS = 100; // 5s
    private static final int COOLDOWN_TICKS = 600; // 30s

    // ===== Habilidade H: Charge (Investida) =====

    @Override
    public boolean canUseAbility() { return true; }

    @Override
    public void executeAbility(ServerPlayer player) {
        long currentTick = player.serverLevel().getServer().getTickCount();
        ChargeState state = CHARGE_STATES.computeIfAbsent(player.getUUID(), k -> new ChargeState());

        // Se já está em charge, não faz nada
        if (state.active) {
            player.sendSystemMessage(Component.literal("§cVocê já está em investida!"));
            return;
        }

        // Verifica cooldown
        Long readyAt = ABILITY_COOLDOWNS.get(player.getUUID());
        if (readyAt != null && currentTick < readyAt) {
            double seconds = (readyAt - currentTick) / 20.0;
            player.sendSystemMessage(Component.literal(String.format("§cInvestida em recarga: §f%.1fs", seconds)));
            return;
        }

        // Ativa o charge
        state.active = true;
        state.startTick = currentTick;
        player.sendSystemMessage(Component.literal("§aInvestida!"));

        ServerLevel level = player.serverLevel();
        Vec3 pos = player.position();
        level.playSound(null, pos.x, pos.y, pos.z, 
            SoundEvents.HORSE_GALLOP, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    // ===== Tick: Controle do Charge =====

    @Override
    public void onPlayerTick(ServerPlayer player) {
        long currentTick = player.serverLevel().getServer().getTickCount();
        ChargeState state = CHARGE_STATES.get(player.getUUID());

        if (state == null || !state.active) return;

        long elapsed = currentTick - state.startTick;

        // Charge terminou por tempo?
        if (elapsed >= CHARGE_DURATION_TICKS) {
            endCharge(player, state, currentTick);
            return;
        }

        // Aplica Speed III
        if (!player.hasEffect(MobEffects.MOVEMENT_SPEED) || 
            player.getEffect(MobEffects.MOVEMENT_SPEED).getAmplifier() < 2) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20, 2, false, false));
        }

        // Spawna partículas de poeira atrás
        if (currentTick % 5 == 0) { // A cada 0.25s
            ServerLevel level = player.serverLevel();
            Vec3 pos = player.position();
            Vec3 look = player.getLookAngle();
            Vec3 behindPos = pos.subtract(look.scale(0.5));

            level.sendParticles(ParticleTypes.POOF, 
                behindPos.x, behindPos.y, behindPos.z, 
                3, 0.2, 0.1, 0.2, 0.02);
        }

        // Checa colisão com mobs
        checkChargeCollision(player, state, currentTick);
    }

    // ===== Verifica colisão durante Charge =====

    private void checkChargeCollision(ServerPlayer player, ChargeState state, long currentTick) {
        ServerLevel level = player.serverLevel();
        Vec3 pos = player.position();

        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, 
            new AABB(pos.add(-1.5, -0.5, -1.5), pos.add(1.5, 2, 1.5)), 
            e -> e != player && e.isAlive()
        );

        if (!targets.isEmpty()) {
            LivingEntity target = targets.get(0);

            // Aplica 4 de dano
            target.hurt(level.damageSources().playerAttack(player), 4.0f);

            // Knockback alto (2 blocos pra trás)
            Vec3 direction = target.position().subtract(pos).normalize();
            target.setDeltaMovement(direction.x * 2.5, 0.5, direction.z * 2.5);
            target.hurtMarked = true;

            // Partículas e som
            level.sendParticles(ParticleTypes.EXPLOSION, 
                target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(), 
                10, 0.3, 0.3, 0.3, 0.1);

            level.playSound(null, pos.x, pos.y, pos.z, 
                SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0f, 0.8f);

            player.sendSystemMessage(Component.literal("§eImpacto!"));

            // Termina o charge
            endCharge(player, state, currentTick);
        }
    }

    // ===== Termina o Charge =====

    private void endCharge(ServerPlayer player, ChargeState state, long currentTick) {
        state.active = false;
        ABILITY_COOLDOWNS.put(player.getUUID(), currentTick + COOLDOWN_TICKS);
        player.sendSystemMessage(Component.literal("§7Investida terminou."));
    }

    // ===== Fraqueza: +30% dano perfurante =====
    @Override
    public void onPlayerHurt(ServerPlayer player, LivingIncomingDamageEvent event) {
        RacialWeakness.applyPiercing(event, 1.3f);
    }
}
