package com.pedro.racasclasses.race.impl;

import com.pedro.racasclasses.race.Race;
import com.pedro.racasclasses.race.RacialWeakness;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
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

public class GenasiRace implements Race {

    @Override
    public String getId() { return "genasi"; }

    @Override
    public String getDisplayName() { return "Genasi"; }

    // ===== Sub-raça =====

    @Override
    public boolean hasSubrace() { return true; }

    @Override
    public String[] getSubraceIds() { 
        return new String[]{"ar", "fogo", "terra", "agua"}; 
    }

    @Override
    public String getSubraceDisplayName(ServerPlayer player) {
        String subrace = getSubrace(player);
        if (subrace == null) return "";
        switch (subrace) {
            case "ar": return "Ar";
            case "fogo": return "Fogo";
            case "terra": return "Terra";
            case "agua": return "Água";
            default: return "";
        }
    }

    private static final Map<UUID, String> SUBRACES = new HashMap<>();

    @Override
    public void onSubraceChosen(ServerPlayer player, String subraceId) {
        SUBRACES.put(player.getUUID(), subraceId);
    }

    private String getSubrace(ServerPlayer player) {
        return SUBRACES.get(player.getUUID());
    }

    // ===== Atributos base (dependem da sub-raça) =====

    @Override
    public double getMaxHealth() { 
        return 22.0; // Terra: 26 via getSubraceHealthBonus
    }

    @Override
    public double getSubraceHealthBonus(ServerPlayer player) {
        String subrace = getSubrace(player);
        if ("terra".equals(subrace)) {
            return 4.0; // 22 + 4 = 26
        }
        return 0.0;
    }

    @Override
    public double getAttackDamage() { 
        return 1.0; // Fogo e Terra: 1.1
    }

    @Override
    public double getMovementSpeed() { 
        return 0.10; // Ar: 0.11, Terra: 0.08
    }

    @Override
    public double getArmor() { 
        return 0.0; // Terra: 4.0
    }

    @Override
    public double getScale() { 
        return 1.0; // Terra: 1.10
    }

    @Override
    public boolean hasNightVision() { 
        return true; // Ar não tem
    }

    @Override
    public double getSwimSpeedBonus() {
        return 0.0; // Água: +50%
    }

    // ===== Passivas =====

    @Override
    public void onPlayerTick(ServerPlayer player) {
        String subrace = getSubrace(player);
        if (subrace == null) return;

        switch (subrace) {
            case "ar":
                // Slow Falling permanente
                if (!player.hasEffect(MobEffects.SLOW_FALLING)) {
                    player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 400, 0, false, false));
                }
                break;
            case "fogo":
                // Fraqueza: dano na água E na chuva
                RacialWeakness.waterOrRainDamage(player, 1.0f);
                break;
            case "agua":
                // Respira na água
                if (!player.hasEffect(MobEffects.WATER_BREATHING)) {
                    player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 400, 0, false, false));
                }
                // Regeneration I quando na água
                if (player.isInWater() && !player.hasEffect(MobEffects.REGENERATION)) {
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0, false, false));
                }
                break;
        }
    }

    @Override
    public void onPlayerHurt(ServerPlayer player, LivingIncomingDamageEvent event) {
        String subrace = getSubrace(player);
        if (subrace == null) return;

        // Fraquezas por sub-raça
        switch (subrace) {
            case "ar":
                // Fraqueza: +30% dano de explosão
                RacialWeakness.applyExplosion(event, 1.3f);
                break;
            case "fogo":
                // Genasi Fogo: Imune a fogo
                DamageSource source = event.getSource();
                String type = source.typeHolder().getRegisteredName();

                if (type.contains("in_fire") || type.contains("on_fire") ||
                    type.contains("lava") || type.contains("hot_floor")) {
                    event.setCanceled(true);
                    player.clearFire();
                }
                break;
            case "terra":
                // Fraqueza: +30% dano de explosão
                RacialWeakness.applyExplosion(event, 1.3f);
                break;
            case "agua":
                // Fraqueza: +50% dano de fogo
                RacialWeakness.applyFire(event, 1.5f);
                break;
        }
    }

    @Override
    public void onAttackEntity(ServerPlayer player, LivingIncomingDamageEvent event) {
        String subrace = getSubrace(player);
        if (!"fogo".equals(subrace)) return;

        // Genasi Fogo: ataque mão vazia aplica fogo
        if (!player.getMainHandItem().isEmpty()) return;

        if (event.getEntity() instanceof LivingEntity target) {
            target.setRemainingFireTicks(60); // 3s
        }
    }

    @Override
    public void onFall(ServerPlayer player, LivingFallEvent event) {
        String subrace = getSubrace(player);
        if (!"terra".equals(subrace)) return;

        // Genasi Terra: Imunidade a queda (5 blocos)
        if (event.getDistance() <= 5.0f) {
            event.setDamageMultiplier(0.0f);
        }
    }

    // ===== Super Jump (apenas Genasi Ar) =====

    private static final Map<UUID, Long> SUPER_JUMP_COOLDOWNS = new HashMap<>();
    private static final int SUPER_JUMP_COOLDOWN_TICKS = 200; // 10s

    @Override
    public boolean canSuperJump() { return true; }

    @Override
    public void executeSuperJump(ServerPlayer player) {
        String subrace = getSubrace(player);
        if (!"ar".equals(subrace)) {
            player.sendSystemMessage(Component.literal("§cApenas Genasi (Ar) pode usar super pulo!"));
            return;
        }

        if (!player.onGround()) return;

        long currentTick = player.serverLevel().getServer().getTickCount();
        Long readyAt = SUPER_JUMP_COOLDOWNS.get(player.getUUID());

        if (readyAt != null && currentTick < readyAt) {
            double seconds = (readyAt - currentTick) / 20.0;
            player.sendSystemMessage(Component.literal(String.format("§cSuper pulo em recarga: §f%.1fs", seconds)));
            return;
        }

        player.setDeltaMovement(0, 1.0, 0);
        player.hurtMarked = true;

        SUPER_JUMP_COOLDOWNS.put(player.getUUID(), currentTick + SUPER_JUMP_COOLDOWN_TICKS);
    }

    // ===== Habilidade H (depende da sub-raça) =====

    private static final Map<UUID, Long> ABILITY_COOLDOWNS = new HashMap<>();

    @Override
    public boolean canUseAbility() { return true; }

    @Override
    public void executeAbility(ServerPlayer player) {
        String subrace = getSubrace(player);
        if (subrace == null) {
            player.sendSystemMessage(Component.literal("§cEscolha uma sub-raça primeiro!"));
            return;
        }

        switch (subrace) {
            case "ar":
                executeGust(player);
                break;
            case "fogo":
                executeFlameBurst(player);
                break;
            case "terra":
                executeEarthShield(player);
                break;
            case "agua":
                executeWaterWhip(player);
                break;
        }
    }

    // ===== Gust (Ar) =====

    private void executeGust(ServerPlayer player) {
        long currentTick = player.serverLevel().getServer().getTickCount();
        Long readyAt = ABILITY_COOLDOWNS.get(player.getUUID());

        if (readyAt != null && currentTick < readyAt) {
            double seconds = (readyAt - currentTick) / 20.0;
            player.sendSystemMessage(Component.literal(String.format("§cGust em recarga: §f%.1fs", seconds)));
            return;
        }

        ServerLevel level = player.serverLevel();
        Vec3 pos = player.position();
        Vec3 look = player.getLookAngle();

        AABB box = new AABB(pos.add(-5, -2, -5), pos.add(5, 3, 5));
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, box, e -> 
            e != player && e.isAlive() && isInCone(player, e, 5.0, 60.0)
        );

        if (targets.isEmpty()) {
            player.sendSystemMessage(Component.literal("§7Nenhum alvo atingido."));
        } else {
            for (LivingEntity target : targets) {
                Vec3 knockback = look.scale(1.5);
                target.setDeltaMovement(target.getDeltaMovement().add(knockback.x, 0.5, knockback.z));
                target.hurtMarked = true;
                target.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 20, 0));
            }

            player.sendSystemMessage(Component.literal("§aGust!"));
            level.sendParticles(ParticleTypes.CLOUD, pos.x + look.x * 2, pos.y + 1, pos.z + look.z * 2, 
                30, 1.0, 1.0, 1.0, 0.1);
            level.playSound(null, pos.x, pos.y, pos.z, 
                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, 1.5f);
        }

        ABILITY_COOLDOWNS.put(player.getUUID(), currentTick + 600); // 30s
    }

    // ===== Flame Burst (Fogo) =====

    private void executeFlameBurst(ServerPlayer player) {
        long currentTick = player.serverLevel().getServer().getTickCount();
        Long readyAt = ABILITY_COOLDOWNS.get(player.getUUID());

        if (readyAt != null && currentTick < readyAt) {
            double seconds = (readyAt - currentTick) / 20.0;
            player.sendSystemMessage(Component.literal(String.format("§cFlame Burst em recarga: §f%.1fs", seconds)));
            return;
        }

        ServerLevel level = player.serverLevel();
        Vec3 pos = player.position();

        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, 
            new AABB(pos.add(-3, -3, -3), pos.add(3, 3, 3)), 
            e -> e != player && e.isAlive()
        );

        for (LivingEntity target : targets) {
            target.hurt(level.damageSources().playerAttack(player), 6.0f);
            target.setRemainingFireTicks(100); // 5s
        }

        level.sendParticles(ParticleTypes.FLAME, pos.x, pos.y + 1, pos.z, 50, 2.0, 2.0, 2.0, 0.1);
        level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.0f, 1.0f);

        player.sendSystemMessage(Component.literal("§aFlame Burst!"));
        ABILITY_COOLDOWNS.put(player.getUUID(), currentTick + 600); // 30s
    }

    // ===== Earth Shield (Terra) =====

    private void executeEarthShield(ServerPlayer player) {
        long currentTick = player.serverLevel().getServer().getTickCount();
        Long readyAt = ABILITY_COOLDOWNS.get(player.getUUID());

        if (readyAt != null && currentTick < readyAt) {
            double seconds = (readyAt - currentTick) / 20.0;
            player.sendSystemMessage(Component.literal(String.format("§cEarth Shield em recarga: §f%.1fs", seconds)));
            return;
        }

        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 4));
        player.sendSystemMessage(Component.literal("§aEarth Shield!"));
        ABILITY_COOLDOWNS.put(player.getUUID(), currentTick + 1200); // 60s
    }

    // ===== Water Whip (Água) =====

    private void executeWaterWhip(ServerPlayer player) {
        long currentTick = player.serverLevel().getServer().getTickCount();
        Long readyAt = ABILITY_COOLDOWNS.get(player.getUUID());

        if (readyAt != null && currentTick < readyAt) {
            double seconds = (readyAt - currentTick) / 20.0;
            player.sendSystemMessage(Component.literal(String.format("§cWater Whip em recarga: §f%.1fs", seconds)));
            return;
        }

        ServerLevel level = player.serverLevel();
        Vec3 pos = player.position();

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
            // Calcula posição na frente do player
            Vec3 lookDir = player.getLookAngle();
            Vec3 targetPos = pos.add(lookDir.scale(2.0)); // 2 blocos na frente
            
            // Puxa o mob para a posição calculada
            Vec3 direction = targetPos.subtract(target.position()).normalize();
            double distance = target.position().distanceTo(targetPos);
            double strength = Math.min(distance * 0.5, 2.0); // Força baseada na distância
            
            target.setDeltaMovement(direction.x * strength, direction.y * 0.3 + 0.3, direction.z * strength);
            target.hurtMarked = true;

            // Dano + Slowness
            target.hurt(level.damageSources().playerAttack(player), 4.0f);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1)); // 3s, Slowness II

            player.sendSystemMessage(Component.literal("§aWater Whip!"));
            level.playSound(null, pos.x, pos.y, pos.z, 
                SoundEvents.PLAYER_SPLASH_HIGH_SPEED, SoundSource.PLAYERS, 1.0f, 1.0f);
        }

        ABILITY_COOLDOWNS.put(player.getUUID(), currentTick + 400); // 20s
    }

    // ===== Helper =====

    private boolean isInCone(ServerPlayer player, LivingEntity target, double range, double angle) {
        Vec3 look = player.getLookAngle();
        Vec3 toTarget = target.position().subtract(player.position()).normalize();
        double dot = look.dot(toTarget);
        double distance = player.distanceTo(target);
        return distance <= range && dot >= Math.cos(Math.toRadians(angle));
    }
}
