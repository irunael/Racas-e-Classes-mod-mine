package com.pedro.racasclasses.race.impl;

import com.pedro.racasclasses.race.Race;
import com.pedro.racasclasses.race.RacialWeakness;

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
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WukongRace implements Race {

    @Override
    public String getId() { return "wukong"; }

    @Override
    public String getDisplayName() { return "Wukong"; }

    @Override
    public double getMaxHealth() { return 22.0; }

    @Override
    public double getAttackDamage() { return 1.2; }

    @Override
    public double getMovementSpeed() { return 0.11; }

    @Override
    public boolean hasNightVision() { return true; }

    // ===== Passiva: Salto +1 bloco =====

    @Override
    public double getJumpStrengthBonus() { return 0.1; }

    // ===== Tick: Escalada + Hidrofobia + Reset do double jump =====

    @Override
    public void onPlayerTick(ServerPlayer player) {
        // Reseta double jump quando toca no chão
        checkGroundReset(player);

        // Escalada: SHIFT + olhando para bloco sólido
        if (player.isShiftKeyDown() && !player.onGround()) {
            Vec3 lookVec = player.getLookAngle();
            net.minecraft.core.Direction lookDir = net.minecraft.core.Direction.getNearest(lookVec.x, 0, lookVec.z);

            net.minecraft.core.BlockPos frontPos = player.blockPosition().relative(lookDir);
            net.minecraft.world.level.block.state.BlockState frontBlock = player.level().getBlockState(frontPos);

            if (!frontBlock.isAir() && frontBlock.isSolid()) {
                double lookAngle = Math.toDegrees(Math.acos(lookVec.y));
                if (lookAngle > 30 && lookAngle < 150) {
                    Vec3 motion = player.getDeltaMovement();
                    player.setDeltaMovement(motion.x, 0.3, motion.z);
                    player.hurtMarked = true;
                    player.fallDistance = 0;
                }
            }
        }

        // Hidrofobia: 1 de dano por tick na água (não chuva)
        if (player.isInWater()) {
            ServerLevel level = player.serverLevel();
            if (!level.isRainingAt(player.blockPosition())) {
                player.hurt(level.damageSources().drown(), 1.0f);
            }
        }
    }

    // ===== Fraqueza: +30% dano de raio =====
    @Override
    public void onPlayerHurt(ServerPlayer player, LivingIncomingDamageEvent event) {
        RacialWeakness.applyLightning(event, 1.3f);
    }

    // ===== Pulo Duplo =====

    public void tryDoubleJump(ServerPlayer player) {
        if (player.onGround()) return;

        long currentTick = player.serverLevel().getServer().getTickCount();
        long lastJump = player.getPersistentData().getLong("wukong_last_double_jump");

        if (player.getPersistentData().getBoolean("wukong_used_double_jump")) {
            return;
        }

        player.setDeltaMovement(player.getDeltaMovement().x, 0.6, player.getDeltaMovement().z);
        player.hurtMarked = true;

        player.getPersistentData().putBoolean("wukong_used_double_jump", true);
        player.getPersistentData().putLong("wukong_last_double_jump", currentTick);

        player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.5f, 1.5f);
    }

    private void checkGroundReset(ServerPlayer player) {
        if (player.onGround() && player.getPersistentData().getBoolean("wukong_used_double_jump")) {
            player.getPersistentData().putBoolean("wukong_used_double_jump", false);
        }
    }

    // ===== Habilidade H: Rugido =====

    private static final Map<UUID, Long> ABILITY_COOLDOWNS = new HashMap<>();
    private static final int COOLDOWN_TICKS = 1800; // 90s

    @Override
    public boolean canUseAbility() { return true; }

    @Override
    public void executeAbility(ServerPlayer player) {
        long currentTick = player.serverLevel().getServer().getTickCount();
        Long readyAt = ABILITY_COOLDOWNS.get(player.getUUID());

        if (readyAt != null && currentTick < readyAt) {
            double seconds = (readyAt - currentTick) / 20.0;
            player.sendSystemMessage(Component.literal(String.format("§cRugido em recarga: §f%.1fs", seconds)));
            return;
        }

        ServerLevel level = player.serverLevel();
        Vec3 pos = player.position();

        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class,
                new AABB(pos.add(-6, -6, -6), pos.add(6, 6, 6)),
                e -> e != player && e.isAlive()
        );

        if (targets.isEmpty()) {
            player.sendSystemMessage(Component.literal("§7Nenhum alvo por perto."));
        } else {
            for (LivingEntity target : targets) {
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 1, 0, false, false));
                target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 1, 0, false, false));
            }

            player.sendSystemMessage(Component.literal("§aRugido!"));
        }

        level.playSound(null, pos.x, pos.y, pos.z,
                SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 2.0f, 0.8f);

        ABILITY_COOLDOWNS.put(player.getUUID(), currentTick + COOLDOWN_TICKS);
    }
}