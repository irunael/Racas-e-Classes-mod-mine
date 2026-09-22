package com.pedro.racasclasses.race.impl;

import com.pedro.racasclasses.race.Race;
import com.pedro.racasclasses.race.RacialWeakness;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SatyrRace implements Race {

    // ===== Super Jump (R) =====
    private static final Map<UUID, Long> SUPER_JUMP_COOLDOWNS = new HashMap<>();
    private static final int SUPER_JUMP_COOLDOWN_TICKS = 200; // 10s

    // ===== Instrumento Mágico (H) =====
    private static final Map<UUID, Long> ABILITY_COOLDOWNS = new HashMap<>();
    private static final int ABILITY_COOLDOWN_TICKS = 900; // 45s
    private static final int ABILITY_DURATION_TICKS = 200; // 10s
    private static final double ABILITY_RADIUS = 6.0;

    @Override
    public String getId() { return "satyr"; }

    @Override
    public String getDisplayName() { return "Satyr"; }

    @Override
    public double getMaxHealth() { return 20.0; }

    @Override
    public double getMovementSpeed() { return 0.11; }

    @Override
    public double getScale() { return 0.85; }

    // ===== Tick: Speed I permanente + Regen perto de jukebox =====

    @Override
    public void onPlayerTick(ServerPlayer player) {
        if (player.tickCount % 20 != 0) return;

        // Speed I permanente
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, 0, false, false, false));

        // Regeneration I perto de jukebox tocando
        if (hasPlayingJukeboxNearby(player)) {
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 0, false, false, false));
        }
    }

    private boolean hasPlayingJukeboxNearby(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BlockPos center = player.blockPosition();

        for (int x = -8; x <= 8; x++) {
            for (int y = -4; y <= 4; y++) {
                for (int z = -8; z <= 8; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    BlockState state = level.getBlockState(pos);

                    if (state.is(Blocks.JUKEBOX)) {
                        if (level.getBlockEntity(pos) instanceof JukeboxBlockEntity jukebox) {
                            if (!jukebox.getTheItem().isEmpty()) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    // ===== Resistência mágica (-30%) + Fraqueza ferro =====

    @Override
    public void onPlayerHurt(ServerPlayer player, LivingIncomingDamageEvent event) {
        // Resistência mágica
        if (event.getSource().is(DamageTypes.MAGIC)
                || event.getSource().is(DamageTypes.INDIRECT_MAGIC)
                || event.getSource().is(DamageTypes.WITHER)) {
            event.setAmount(event.getAmount() * 0.7f);
        }

        // Fraqueza: +30% dano de armas de ferro
        RacialWeakness.applyIronWeapon(event, 1.3f);
    }

    // ===== Imunidade a queda (até 5 blocos) =====

    public void onFall(ServerPlayer player, net.neoforged.neoforge.event.entity.living.LivingFallEvent event) {
        if (event.getDistance() <= 5.0f) {
            event.setCanceled(true);
        } else {
            event.setDistance(event.getDistance() - 5.0f);
        }
    }

    // ===== Super Jump (R) =====

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

    // ===== Instrumento Mágico (H) =====

    @Override
    public boolean canUseAbility() { return true; }

    @Override
    public void executeAbility(ServerPlayer player) {
        long currentTick = player.serverLevel().getServer().getTickCount();
        Long readyAt = ABILITY_COOLDOWNS.get(player.getUUID());

        if (readyAt != null && currentTick < readyAt) {
            double seconds = (readyAt - currentTick) / 20.0;
            player.sendSystemMessage(Component.literal(
                    String.format("§cInstrumento em recarga: §f%.1fs", seconds)));
            return;
        }

        ServerLevel level = player.serverLevel();
        AABB area = player.getBoundingBox().inflate(ABILITY_RADIUS);

        // Toca som
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.NOTE_BLOCK_FLUTE, SoundSource.PLAYERS, 1.0f, 1.0f);

        // Aplica Nausea + Weakness em todos (incluindo players)
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e.isAlive());

        for (LivingEntity target : targets) {
            target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, ABILITY_DURATION_TICKS, 0, false, false, true));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, ABILITY_DURATION_TICKS, 0, false, false, true));
        }

        ABILITY_COOLDOWNS.put(player.getUUID(), currentTick + ABILITY_COOLDOWN_TICKS);
        player.sendSystemMessage(Component.literal("§aInstrumento Mágico ativado!"));
    }
}