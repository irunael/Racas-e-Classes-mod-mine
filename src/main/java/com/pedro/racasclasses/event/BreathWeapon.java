package com.pedro.racasclasses.event;

import com.pedro.racasclasses.RacasClasses;
import com.pedro.racasclasses.capability.ModAttachments;
import com.pedro.racasclasses.capability.PlayerRaceData;
import com.pedro.racasclasses.race.impl.DragonbornRace;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = RacasClasses.MODID)
public class BreathWeapon {

    private static final int BREATH_DURATION_TICKS = 100;
    private static final int DAMAGE_INTERVAL_TICKS = 20;
    private static final int COOLDOWN_TICKS = 600;
    private static final int COOLDOWN_TICKS_LIGHTNING = 900;

    private static final double CONE_ANGLE_COS = Math.cos(Math.toRadians(30));
    private static final double LINE_RANGE = 20.0;
    private static final double LINE_RADIUS = 1.0;

    private static final Map<UUID, ActiveBreath> ACTIVE_BREATHS = new HashMap<>();
    private static final Map<UUID, Long> COOLDOWNS = new HashMap<>();

    private static class ActiveBreath {
        final ServerPlayer player;
        final String element;
        final boolean isLine;
        final long startTick;
        long lastDamageTick;
        final boolean instant;

        ActiveBreath(ServerPlayer player, String element, boolean isLine, long startTick) {
            this.player = player;
            this.element = element;
            this.isLine = isLine;
            this.startTick = startTick;
            this.lastDamageTick = -1;
            this.instant = element.equals("lightning");
        }

        boolean isExpired(long currentTick) {
            if (instant) return currentTick > startTick;
            return currentTick - startTick >= BREATH_DURATION_TICKS;
        }

        boolean shouldDamage(long currentTick) {
            if (instant) return lastDamageTick == -1;
            if (lastDamageTick == -1) return true;
            return currentTick - lastDamageTick >= DAMAGE_INTERVAL_TICKS;
        }
    }

    private static double getConeRange(String element) {
        return switch (element) {
            case "fire" -> 6.0;
            case "ice" -> 5.0;
            case "acid" -> 6.0;
            case "poison" -> 4.0;
            default -> 4.0;
        };
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (ACTIVE_BREATHS.isEmpty()) return;

        long currentTick = event.getServer().getTickCount();
        var iterator = ACTIVE_BREATHS.entrySet().iterator();

        while (iterator.hasNext()) {
            var entry = iterator.next();
            ActiveBreath breath = entry.getValue();

            if (breath.player.isRemoved() || !breath.player.isAlive()) {
                iterator.remove();
                continue;
            }

            if (breath.shouldDamage(currentTick)) {
                applyDamageAndEffects(breath);
                breath.lastDamageTick = currentTick;
            }

            if (!breath.instant) {
                spawnParticles(breath);
            }

            if (breath.isExpired(currentTick)) {
                iterator.remove();
                long cd = breath.instant ? COOLDOWN_TICKS_LIGHTNING : COOLDOWN_TICKS;
                COOLDOWNS.put(breath.player.getUUID(), currentTick + cd);
            }
        }
    }

    public static boolean tryUseBreath(ServerPlayer player) {
        PlayerRaceData data = player.getData(ModAttachments.PLAYER_RACE);

        if (!data.getRaceId().equals("dragonborn")) {
            player.sendSystemMessage(Component.literal("§cApenas Draconatos podem usar a baforada."));
            return false;
        }

        if (ACTIVE_BREATHS.containsKey(player.getUUID())) {
            player.sendSystemMessage(Component.literal("§cVocê já está usando a baforada!"));
            return false;
        }

        long currentTick = player.serverLevel().getServer().getTickCount();
        Long readyAt = COOLDOWNS.get(player.getUUID());
        if (readyAt != null && currentTick < readyAt) {
            double seconds = (readyAt - currentTick) / 20.0;
            player.sendSystemMessage(Component.literal(String.format("§cBaforada em recarga: §f%.1fs", seconds)));
            return false;
        }

        String element = DragonbornRace.getElement(data.getDragonbornSubrace());
        boolean isLine = element.equals("lightning");

        ActiveBreath breath = new ActiveBreath(player, element, isLine, currentTick);
        ACTIVE_BREATHS.put(player.getUUID(), breath);

        playSound(player, element);

        if (breath.instant) {
            spawnParticles(breath);
            applyDamageAndEffects(breath);
            breath.lastDamageTick = currentTick;
            ACTIVE_BREATHS.remove(player.getUUID());
            COOLDOWNS.put(player.getUUID(), currentTick + COOLDOWN_TICKS_LIGHTNING);
            player.sendSystemMessage(Component.literal("§aRaio disparado!"));
        } else {
            player.sendSystemMessage(Component.literal("§aBaforada ativada!"));
        }
        return true;
    }

    private static void applyDamageAndEffects(ActiveBreath breath) {
        ServerPlayer player = breath.player;
        ServerLevel level = player.serverLevel();
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookDir = player.getLookAngle().normalize();

        List<LivingEntity> targets = level.getEntitiesOfClass(
                LivingEntity.class,
                player.getBoundingBox().inflate(getRange(breath) + 2),
                e -> e != player && e.isAlive()
        );

        DamageSource source = getDamageSource(player, breath.element);

        for (LivingEntity target : targets) {
            Vec3 toTarget = target.position().add(0, target.getBbHeight() / 2.0, 0).subtract(eyePos);
            double distance = toTarget.length();
            if (distance > getRange(breath)) continue;
            if (distance < 0.1) continue;

            Vec3 toTargetNorm = toTarget.normalize();
            boolean inArea;

            if (breath.isLine) {
                double dot = lookDir.dot(toTargetNorm);
                if (dot < 0) continue;
                Vec3 perpendicular = toTarget.subtract(lookDir.scale(toTarget.dot(lookDir)));
                inArea = perpendicular.length() <= LINE_RADIUS;
            } else {
                double dot = lookDir.dot(toTargetNorm);
                inArea = dot >= CONE_ANGLE_COS;
            }

            if (inArea) {
                applyDamageToTarget(breath, target, source);
            }
        }
    }

    private static void applyDamageToTarget(ActiveBreath breath, LivingEntity target, DamageSource source) {
        switch (breath.element) {
            case "fire" -> {
                target.hurt(source, 3.0f);
                target.setRemainingFireTicks(60);
            }
            case "ice" -> {
                target.hurt(source, 4.0f);
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 1, false, false, true));
                int freezeTicks = Math.min(target.getTicksFrozen() + 200, target.getTicksRequiredToFreeze() + 200);
                target.setTicksFrozen(freezeTicks);
            }
            case "poison", "acid" -> {
                target.hurt(source, 2.0f);
                target.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0, false, false, true));
            }
            case "lightning" -> target.hurt(source, 10.0f);
        }
    }

    private static DamageSource getDamageSource(ServerPlayer player, String element) {
        var registry = player.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
        return switch (element) {
            case "fire" -> new DamageSource(registry.getHolderOrThrow(DamageTypes.IN_FIRE), player);
            case "ice" -> new DamageSource(registry.getHolderOrThrow(DamageTypes.FREEZE), player);
            case "lightning" -> new DamageSource(registry.getHolderOrThrow(DamageTypes.LIGHTNING_BOLT), player);
            case "poison", "acid" -> new DamageSource(registry.getHolderOrThrow(DamageTypes.MAGIC), player);
            default -> new DamageSource(registry.getHolderOrThrow(DamageTypes.MAGIC), player);
        };
    }

    private static void spawnParticles(ActiveBreath breath) {
        ServerPlayer player = breath.player;
        ServerLevel level = player.serverLevel();
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookDir = player.getLookAngle().normalize();

        ParticleOptions particle = getParticle(breath.element);
        int count = breath.isLine ? 20 : 15;
        double range = getRange(breath);

        Vec3 right = lookDir.cross(new Vec3(0, 1, 0)).normalize();
        Vec3 up = right.cross(lookDir).normalize();

        for (int i = 0; i < count; i++) {
            double t = (i + 1) / (double) count;
            double dist = t * range;
            Vec3 base = eyePos.add(lookDir.scale(dist));
            double spread = breath.isLine ? LINE_RADIUS * 0.5 : Math.tan(Math.toRadians(30)) * dist;
            double r = (level.random.nextDouble() - 0.5) * spread;
            double u = (level.random.nextDouble() - 0.5) * spread;
            Vec3 pos = base.add(right.scale(r)).add(up.scale(u));
            level.sendParticles(particle, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
        }
    }

    private static ParticleOptions getParticle(String element) {
        return switch (element) {
            case "fire" -> ParticleTypes.FLAME;
            case "ice" -> ParticleTypes.SNOWFLAKE;
            case "lightning" -> ParticleTypes.ELECTRIC_SPARK;
            case "poison", "acid" -> ParticleTypes.ITEM_SLIME;
            default -> ParticleTypes.FLAME;
        };
    }

    private static void playSound(ServerPlayer player, String element) {
        SoundEvent sound = switch (element) {
            case "fire" -> SoundEvents.ENDER_DRAGON_GROWL;
            case "ice" -> SoundEvents.GLASS_BREAK;
            case "lightning" -> SoundEvents.LIGHTNING_BOLT_THUNDER;
            case "poison", "acid" -> SoundEvents.LLAMA_SPIT;
            default -> SoundEvents.ENDER_DRAGON_GROWL;
        };
        player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(), sound, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    private static double getRange(ActiveBreath breath) {
        return breath.isLine ? LINE_RANGE : getConeRange(breath.element);
    }
}