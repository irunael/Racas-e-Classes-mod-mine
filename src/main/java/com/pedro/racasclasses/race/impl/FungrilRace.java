package com.pedro.racasclasses.race.impl;

import com.pedro.racasclasses.race.Race;
import com.pedro.racasclasses.race.RacialWeakness;
import net.minecraft.core.BlockPos;
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
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FungrilRace implements Race {

    @Override
    public String getId() { return "fungril"; }

    @Override
    public String getDisplayName() { return "Fungril"; }

    @Override
    public double getMaxHealth() { return 22.0; }

    @Override
    public double getAttackDamage() { return 1.0; }

    @Override
    public double getMovementSpeed() { return 0.08; }

    @Override
    public double getScale() { return 1.05; }

    @Override
    public double getArmor() { return 3.0; }

    @Override
    public boolean hasNightVision() { return true; }

    // ===== Estrutura de Spore Area =====

    private static class SporeArea {
        BlockPos center;
        long startTick;
        UUID ownerId;

        SporeArea(BlockPos center, long startTick, UUID ownerId) {
            this.center = center;
            this.startTick = startTick;
            this.ownerId = ownerId;
        }
    }

    private static final Map<UUID, Long> ABILITY_COOLDOWNS = new HashMap<>();
    private static final Map<UUID, SporeArea> ACTIVE_SPORE_AREAS = new HashMap<>();
    private static final int COOLDOWN_TICKS = 3600; // 180s
    private static final int SPORE_DURATION_TICKS = 600; // 30s

    // ===== Tick: Aura Venenosa + Spore Cloud =====

    @Override
    public void onPlayerTick(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 pos = player.position();

        // ===== PASSIVA: Aura Venenosa (raio 1 bloco, a cada 1s = 20 ticks) =====
        if (player.tickCount % 20 == 0) {
            List<LivingEntity> nearbyMobs = level.getEntitiesOfClass(LivingEntity.class,
                new AABB(pos.add(-1, -1, -1), pos.add(1, 2, 1)),
                e -> e != player && e.isAlive()
            );

            for (LivingEntity mob : nearbyMobs) {
                mob.addEffect(new MobEffectInstance(MobEffects.POISON, 40, 0, false, false));
            }
        }

        // ===== HABILIDADE: Spore Cloud (área estática) =====
        SporeArea area = ACTIVE_SPORE_AREAS.get(player.getUUID());
        if (area != null) {
            long currentTick = level.getServer().getTickCount();
            long elapsed = currentTick - area.startTick;

            // Terminou a duração?
            if (elapsed >= SPORE_DURATION_TICKS) {
                ACTIVE_SPORE_AREAS.remove(player.getUUID());
                player.sendSystemMessage(Component.literal("§7Nuvem de esporos dissipou."));
                return;
            }

            // Aplica Poison II em mobs dentro da área (a cada 1s = 20 ticks)
            if (player.tickCount % 20 == 0) {
                Vec3 center = Vec3.atCenterOf(area.center);
                List<LivingEntity> mobsInArea = level.getEntitiesOfClass(LivingEntity.class,
                    new AABB(center.add(-6, -6, -6), center.add(6, 6, 6)),
                    e -> e != player && e.isAlive()
                );

                for (LivingEntity mob : mobsInArea) {
                    mob.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 1, false, false));
                }

                // Partículas
                level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR,
                    center.x, center.y + 1, center.z,
                    15, 3.0, 2.0, 3.0, 0.02);
                
                level.sendParticles(ParticleTypes.ITEM_SLIME,
                    center.x, center.y + 1, center.z,
                    10, 3.0, 2.0, 3.0, 0.05);
            }
        }
    }

    // ===== Habilidade H: Spore Cloud =====

    @Override
    public boolean canUseAbility() { return true; }

    @Override
    public void executeAbility(ServerPlayer player) {
        long currentTick = player.serverLevel().getServer().getTickCount();
        Long readyAt = ABILITY_COOLDOWNS.get(player.getUUID());

        if (readyAt != null && currentTick < readyAt) {
            double seconds = (readyAt - currentTick) / 20.0;
            player.sendSystemMessage(Component.literal(String.format("§cSpore Cloud em recarga: §f%.1fs", seconds)));
            return;
        }

        ServerLevel level = player.serverLevel();
        BlockPos spawnPos = player.blockPosition();

        // Cria a área de esporos no local atual
        ACTIVE_SPORE_AREAS.put(player.getUUID(), new SporeArea(spawnPos, currentTick, player.getUUID()));

        player.sendSystemMessage(Component.literal("§aSpore Cloud criada!"));

        // Som
        level.playSound(null, spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(),
            SoundEvents.FUNGUS_PLACE, SoundSource.PLAYERS, 1.0f, 0.8f);

        // Partículas iniciais
        Vec3 center = Vec3.atCenterOf(spawnPos);
        level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR,
            center.x, center.y + 1, center.z,
            30, 4.0, 3.0, 4.0, 0.1);

        ABILITY_COOLDOWNS.put(player.getUUID(), currentTick + COOLDOWN_TICKS);
    }

    // ===== Fraqueza: +50% dano de fogo =====
    @Override
    public void onPlayerHurt(ServerPlayer player, LivingIncomingDamageEvent event) {
        RacialWeakness.applyFire(event, 1.5f);
    }
}
