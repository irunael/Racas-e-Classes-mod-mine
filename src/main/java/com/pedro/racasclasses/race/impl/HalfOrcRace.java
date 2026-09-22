package com.pedro.racasclasses.race.impl;

import com.pedro.racasclasses.race.Race;
import com.pedro.racasclasses.race.RacialWeakness;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Stray;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.Bogged;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;

import java.util.List;

public class HalfOrcRace implements Race {

    private static final double MENACING_RADIUS = 8.0;

    @Override
    public String getId() { return "halforc"; }

    @Override
    public String getDisplayName() { return "Meio-Orc"; }

    @Override public int getRacialStrength() { return 2; }
    @Override public int getRacialConstitution() { return 1; }
    @Override public int getFreeAttributePoints() { return 0; }

    @Override
    public double getMaxHealth() { return 24.0; }

    @Override
    public double getAttackDamage() { return 1.2; }

    @Override
    public double getScale() { return 1.10; }

    @Override
    public boolean hasNightVision() { return true; }

    @Override
    public void onPlayerHurt(ServerPlayer player, net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent event) {
        RacialWeakness.applyMagic(event, 1.3f);
    }

    // ===== Savage Attacks (+20% em críticos) =====

    @Override
    public void onCriticalHit(ServerPlayer player, CriticalHitEvent event) {
        float current = event.getDamageMultiplier();
        event.setDamageMultiplier(current * 1.20f);
    }

    // ===== Menacing (esqueletos fogem) =====

    @Override
    public void onPlayerTick(ServerPlayer player) {
        if (player.tickCount % 5 != 0) return;   // ← 5 em vez de 10

        ServerLevel level = player.serverLevel();
        AABB area = player.getBoundingBox().inflate(MENACING_RADIUS);

        List<Mob> skeletons = level.getEntitiesOfClass(
                Mob.class,
                area,
                mob -> isSkeleton(mob) && mob.isAlive() && !mob.isRemoved()
        );

        Vec3 playerPos = player.position();

        for (Mob skeleton : skeletons) {
            if (skeleton.getTarget() == player) {
                skeleton.setTarget(null);
            }

            skeleton.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 0, false, false, false));

            Vec3 skeletonPos = skeleton.position();
            Vec3 away = skeletonPos.subtract(playerPos);
            double distance = away.length();

            if (distance > 0.5 && distance < MENACING_RADIUS) {
                Vec3 push = away.normalize().scale(0.4);   // ← 0.4 em vez de 0.15
                skeleton.push(push.x, 0.2, push.z);        // ← Y = 0.2 em vez de 0.05
            }
        }
    }

    /** Impede esqueletos de mirar no player */
    @Override
    public void onMobTarget(ServerPlayer player, LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) return;
        if (!isSkeleton(mob)) return;

        Entity target = event.getNewAboutToBeSetTarget();
        if (target == player) {
            event.setCanceled(true);
        }
    }

    private boolean isSkeleton(Mob mob) {
        return mob instanceof Skeleton
                || mob instanceof Stray
                || mob instanceof WitherSkeleton
                || mob instanceof Bogged;
    }
}