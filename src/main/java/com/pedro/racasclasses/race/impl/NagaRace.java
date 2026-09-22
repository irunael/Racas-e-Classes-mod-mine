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
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class NagaRace implements Race {

    @Override
    public String getId() { return "naga"; }

    @Override
    public String getDisplayName() { return "Naga"; }

    @Override
    public double getMaxHealth() { return 22.0; }

    @Override
    public double getAttackDamage() { return 1.0; }

    @Override
    public double getMovementSpeed() { return 0.10; }

    @Override
    public double getArmor() { return 3.0; }

    @Override
    public double getSwimSpeedBonus() { return 0.50; }

    @Override
    public boolean hasNightVision() { return true; }

    // ===== Passiva: Respira na água =====

    @Override
    public void onPlayerTick(ServerPlayer player) {
        if (!player.hasEffect(MobEffects.WATER_BREATHING)) {
            player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 400, 0, false, false));
        }

        // Mantém alvos constricted travados
        long currentTick = player.serverLevel().getServer().getTickCount();
        CONSTRICTED_TARGETS.entrySet().removeIf(entry -> {
            UUID targetId = entry.getKey();
            long endTick = entry.getValue();
            
            if (currentTick >= endTick) {
                return true; // Remove da lista
            }
            
            // Trava o alvo
            ServerLevel level = player.serverLevel();
            LivingEntity target = (LivingEntity) level.getEntity(targetId);
            if (target != null && target.isAlive()) {
                target.setDeltaMovement(0, target.getDeltaMovement().y, 0);
                target.hurtMarked = true;
            }
            
            return false;
        });
    }

    // ===== Passiva: Ataque mão vazia aplica Poison I por 3s =====

    @Override
    public void onAttackEntity(ServerPlayer player, LivingIncomingDamageEvent event) {
        if (!player.getMainHandItem().isEmpty()) return;

        if (event.getEntity() instanceof LivingEntity target) {
            target.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0)); // 3s, Poison I
        }
    }

    // ===== Fraqueza: +30% dano de mobs mortos-vivos =====
    @Override
    public void onPlayerHurt(ServerPlayer player, LivingIncomingDamageEvent event) {
        RacialWeakness.applyUndead(event, 1.3f);
    }

    // ===== Habilidade H: Constrict =====

    private static final Map<UUID, Long> ABILITY_COOLDOWNS = new HashMap<>();
    private static final Map<UUID, Long> CONSTRICTED_TARGETS = new HashMap<>(); // target -> endTick
    private static final int COOLDOWN_TICKS = 600; // 30s
    private static final int CONSTRICT_DURATION_TICKS = 100; // 5s

    @Override
    public boolean canUseAbility() { return true; }

    @Override
    public void executeAbility(ServerPlayer player) {
        long currentTick = player.serverLevel().getServer().getTickCount();
        Long readyAt = ABILITY_COOLDOWNS.get(player.getUUID());

        if (readyAt != null && currentTick < readyAt) {
            double seconds = (readyAt - currentTick) / 20.0;
            player.sendSystemMessage(Component.literal(String.format("§cConstrict em recarga: §f%.1fs", seconds)));
            return;
        }

        ServerLevel level = player.serverLevel();
        Vec3 pos = player.position();

        // Encontra mob mais próximo num raio de 8 blocos
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, 
            new AABB(pos.add(-8, -8, -8), pos.add(8, 8, 8)), 
            e -> e != player && e.isAlive()
        );

        if (targets.isEmpty()) {
            player.sendSystemMessage(Component.literal("§7Nenhum alvo próximo."));
            return;
        }

        LivingEntity target = targets.stream()
            .min((a, b) -> Double.compare(a.distanceTo(player), b.distanceTo(player)))
            .orElse(null);

        if (target != null) {
            // Marca alvo como constricted
            CONSTRICTED_TARGETS.put(target.getUUID(), currentTick + CONSTRICT_DURATION_TICKS);
            
            // Dano + Poison II por 5s + Slowness IV
            target.hurt(level.damageSources().playerAttack(player), 4.0f);
            target.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1)); // 5s, Poison II
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 3)); // 5s, Slowness IV

            player.sendSystemMessage(Component.literal("§aConstrict!"));

            // Partículas ITEM_SLIME
            level.sendParticles(ParticleTypes.ITEM_SLIME, 
                target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(), 
                20, 0.3, 0.3, 0.3, 0.1);

            // Som
            level.playSound(null, pos.x, pos.y, pos.z, 
                SoundEvents.SLIME_SQUISH, SoundSource.PLAYERS, 1.0f, 0.8f);
        }

        ABILITY_COOLDOWNS.put(player.getUUID(), currentTick + COOLDOWN_TICKS);
    }
}
