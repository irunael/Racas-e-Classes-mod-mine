package com.pedro.racasclasses.race;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.animal.horse.ZombieHorse;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Bogged;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Stray;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * Multiplicadores de fraqueza racial. Cada raça chama só o que precisa
 * em {@code onPlayerHurt} / {@code onPlayerTick}.
 */
public final class RacialWeakness {
    private RacialWeakness() {}

    public static boolean isFire(DamageSource source) {
        return source.is(DamageTypes.IN_FIRE)
                || source.is(DamageTypes.ON_FIRE)
                || source.is(DamageTypes.LAVA)
                || source.is(DamageTypes.HOT_FLOOR);
    }

    public static boolean isExplosion(DamageSource source) {
        return source.is(DamageTypes.EXPLOSION) || source.is(DamageTypes.PLAYER_EXPLOSION);
    }

    public static boolean isFreeze(DamageSource source) {
        return source.is(DamageTypes.FREEZE);
    }

    public static boolean isLightning(DamageSource source) {
        return source.is(DamageTypes.LIGHTNING_BOLT);
    }

    public static boolean isMagic(DamageSource source) {
        return source.is(DamageTypes.MAGIC)
                || source.is(DamageTypes.INDIRECT_MAGIC)
                || source.is(DamageTypes.WITHER)
                || source.is(DamageTypes.DRAGON_BREATH);
    }

    public static boolean isPoisonMagic(ServerPlayer player, DamageSource source) {
        return source.is(DamageTypes.MAGIC) && player.hasEffect(MobEffects.POISON);
    }

    public static boolean isPiercing(DamageSource source) {
        return source.is(DamageTypes.ARROW)
                || source.is(DamageTypes.TRIDENT)
                || source.is(DamageTypes.THROWN);
    }

    public static boolean isProjectile(DamageSource source) {
        return isPiercing(source) || source.is(DamageTypes.MOB_PROJECTILE);
    }

    public static boolean isPhysical(DamageSource source) {
        return source.is(DamageTypes.PLAYER_ATTACK)
                || source.is(DamageTypes.MOB_ATTACK)
                || source.is(DamageTypes.MOB_ATTACK_NO_AGGRO);
    }

    public static boolean isWeaponHit(DamageSource source) {
        if (!source.is(DamageTypes.PLAYER_ATTACK)) return false;
        if (!(source.getEntity() instanceof LivingEntity attacker)) return false;
        return !attacker.getMainHandItem().isEmpty();
    }

    public static boolean isIronWeapon(DamageSource source) {
        if (!(source.getEntity() instanceof LivingEntity attacker)) return false;
        ItemStack weapon = attacker.getMainHandItem();
        return weapon.is(Items.IRON_SWORD)
                || weapon.is(Items.IRON_AXE)
                || weapon.is(Items.IRON_PICKAXE)
                || weapon.is(Items.IRON_SHOVEL)
                || weapon.is(Items.IRON_HOE);
    }

    public static boolean isUndeadAttacker(DamageSource source) {
        if (!(source.getEntity() instanceof Mob mob)) return false;
        return mob instanceof Zombie
                || mob instanceof Skeleton
                || mob instanceof WitherSkeleton
                || mob instanceof Stray
                || mob instanceof Bogged
                || mob instanceof Drowned
                || mob instanceof Husk
                || mob instanceof Phantom
                || mob instanceof Zoglin
                || mob instanceof WitherBoss
                || mob instanceof SkeletonHorse
                || mob instanceof ZombieHorse
                || mob instanceof ZombieVillager;
    }

    public static boolean isInSunlight(ServerPlayer player) {
        return player.level().isDay() && player.level().canSeeSky(player.blockPosition());
    }

    public static void scale(LivingIncomingDamageEvent event, float multiplier) {
        event.setAmount(event.getAmount() * multiplier);
    }

    public static void applyFire(LivingIncomingDamageEvent event, float multiplier) {
        if (isFire(event.getSource())) scale(event, multiplier);
    }

    public static void applyExplosion(LivingIncomingDamageEvent event, float multiplier) {
        if (isExplosion(event.getSource())) scale(event, multiplier);
    }

    public static void applyFreeze(LivingIncomingDamageEvent event, float multiplier) {
        if (isFreeze(event.getSource())) scale(event, multiplier);
    }

    public static void applyLightning(LivingIncomingDamageEvent event, float multiplier) {
        if (isLightning(event.getSource())) scale(event, multiplier);
    }

    public static void applyMagic(LivingIncomingDamageEvent event, float multiplier) {
        if (isMagic(event.getSource())) scale(event, multiplier);
    }

    public static void applyPoisonMagic(ServerPlayer player, LivingIncomingDamageEvent event, float multiplier) {
        if (isPoisonMagic(player, event.getSource())) scale(event, multiplier);
    }

    public static void applyPiercing(LivingIncomingDamageEvent event, float multiplier) {
        if (isPiercing(event.getSource())) scale(event, multiplier);
    }

    public static void applyProjectile(LivingIncomingDamageEvent event, float multiplier) {
        if (isProjectile(event.getSource())) scale(event, multiplier);
    }

    public static void applyPhysical(LivingIncomingDamageEvent event, float multiplier) {
        if (isPhysical(event.getSource())) scale(event, multiplier);
    }

    public static void applyWeapon(LivingIncomingDamageEvent event, float multiplier) {
        if (isWeaponHit(event.getSource())) scale(event, multiplier);
    }

    public static void applyIronWeapon(LivingIncomingDamageEvent event, float multiplier) {
        if (isIronWeapon(event.getSource())) scale(event, multiplier);
    }

    public static void applyUndead(LivingIncomingDamageEvent event, float multiplier) {
        if (isUndeadAttacker(event.getSource())) scale(event, multiplier);
    }

    public static void applyFall(LivingIncomingDamageEvent event, float multiplier) {
        if (event.getSource().is(DamageTypes.FALL)) scale(event, multiplier);
    }

    public static void applyWhileInWater(ServerPlayer player, LivingIncomingDamageEvent event, float multiplier) {
        if (player.isInWater() || player.isUnderWater()) scale(event, multiplier);
    }

    /** +exhaustion uma vez por segundo. 0.15 ≈ 15% mais fome. */
    public static void fasterHunger(ServerPlayer player, float exhaustionPerSecond) {
        if (player.tickCount % 20 != 0) return;
        player.getFoodData().addExhaustion(exhaustionPerSecond);
    }

    /** A cada 5s recupera 1 ponto de fome (fome mais lenta). */
    public static void slowerHunger(ServerPlayer player) {
        if (player.tickCount % 100 != 0) return;
        player.getFoodData().setFoodLevel(Math.min(20, player.getFoodData().getFoodLevel() + 1));
    }

    public static void waterContactDamage(ServerPlayer player, float amount) {
        if (player.tickCount % 20 != 0) return;
        if (player.isInWater()) {
            player.hurt(player.damageSources().drown(), amount);
        }
    }

    public static void waterOrRainDamage(ServerPlayer player, float amount) {
        if (player.tickCount % 20 != 0) return;
        boolean inWater = player.isInWater();
        boolean inRain = player.level().isRainingAt(player.blockPosition());
        if (inWater || inRain) {
            player.hurt(player.damageSources().drown(), amount);
        }
    }
}
