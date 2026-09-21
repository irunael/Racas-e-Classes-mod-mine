package com.pedro.racasclasses.race.impl;

import com.pedro.racasclasses.race.Race;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.Monster;

public class SirenRace implements Race {

    private static final Map<UUID, Long> ABILITY_COOLDOWNS = new HashMap<>();

    @Override
    public String getId() { return "siren"; }

    @Override
    public String getDisplayName() { return "Siren"; }

    @Override
    public boolean hasNightVision() { return true; }

    @Override
    public double getSwimSpeedBonus() { return 0.30; }

    @Override
    public void onPlayerTick(ServerPlayer player) {
        if (player.tickCount % 100 == 0) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.WATER_BREATHING, 200, 0, false, false, false));
        }
    }

    // ===== Charming Song (tecla H) =====

    @Override
    public boolean canUseAbility() { return true; }

    @Override
    public void executeAbility(ServerPlayer player) {
        long currentTick = player.serverLevel().getServer().getTickCount();
        Long readyAt = ABILITY_COOLDOWNS.get(player.getUUID());

        if (readyAt != null && currentTick < readyAt) {
            player.sendSystemMessage(Component.literal(String.format(
                    "§cCharming Song em recarga: §f%.1fs", (readyAt - currentTick) / 20.0)));
            return;
        }

        for (Monster monster : player.level().getEntitiesOfClass(
                Monster.class, player.getBoundingBox().inflate(8))) {
            monster.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
            monster.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));
        }

        player.serverLevel().sendParticles(
                ParticleTypes.NOTE, player.getX(), player.getY() + 1, player.getZ(),
                16, 2, 1, 2, 0.1);
        player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.NOTE_BLOCK_HARP, SoundSource.PLAYERS, 1.0f, 1.0f);
        ABILITY_COOLDOWNS.put(player.getUUID(), currentTick + 600);
        player.sendSystemMessage(Component.literal("§aCharming Song!"));
    }
}
