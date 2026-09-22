package com.pedro.racasclasses.race.impl;

import com.pedro.racasclasses.race.Race;
import com.pedro.racasclasses.race.RacialWeakness;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

public class YuanTiRace implements Race {
    private static final Map<UUID, Long> ABILITY_COOLDOWNS = new HashMap<>();
    private static final Map<UUID, PoisonSpray> ACTIVE_SPRAYS = new HashMap<>();
    
    private static class PoisonSpray {
        final Vec3 start;
        final Vec3 direction;
        final long startTick;
        double distance = 0;
        
        PoisonSpray(Vec3 start, Vec3 direction, long startTick) {
            this.start = start;
            this.direction = direction;
            this.startTick = startTick;
        }
    }
    
    @Override public String getId() { return "yuanti"; }
    @Override public String getDisplayName() { return "Yuan-Ti"; }
    @Override public double getMaxHealth() { return 22; }
    @Override public boolean hasNightVision() { return true; }
    @Override public double getPoisonResistance() { return 1.0; }

    @Override public void onPlayerHurt(ServerPlayer player, LivingIncomingDamageEvent event) {
        // Resistência mágica (-30%)
        if (event.getSource().is(DamageTypes.MAGIC) || event.getSource().is(DamageTypes.WITHER)
                || event.getSource().is(DamageTypes.INDIRECT_MAGIC) || event.getSource().is(DamageTypes.DRAGON_BREATH))
            event.setAmount(event.getAmount() * .7f);

        // Fraqueza: +30% dano de mobs mortos-vivos
        RacialWeakness.applyUndead(event, 1.3f);
    }
    
    @Override public boolean canUseAbility() { return true; }
    
    @Override public void executeAbility(ServerPlayer player) {
        long tick = player.serverLevel().getServer().getTickCount();
        Long ready = ABILITY_COOLDOWNS.get(player.getUUID());
        if (ready != null && tick < ready) { 
            player.sendSystemMessage(Component.literal(String.format("§cPoison Spray em recarga: §f%.1fs", (ready - tick) / 20.0))); 
            return; 
        }
        
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        
        ACTIVE_SPRAYS.put(player.getUUID(), new PoisonSpray(eye, look, tick));
        
        player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.LLAMA_SPIT, SoundSource.PLAYERS, 1.0f, 1.0f);
        
        ABILITY_COOLDOWNS.put(player.getUUID(), tick + 600);
        player.sendSystemMessage(Component.literal("§aPoison Spray!"));
    }
    
    @Override
    public void onPlayerTick(ServerPlayer player) {
        PoisonSpray spray = ACTIVE_SPRAYS.get(player.getUUID());
        if (spray == null) return;
        
        long tick = player.serverLevel().getServer().getTickCount();
        
        // Avança o projétil
        spray.distance += 0.8;
        Vec3 pos = spray.start.add(spray.direction.scale(spray.distance));
        
        // Partículas verdes
        player.serverLevel().sendParticles(ParticleTypes.ITEM_SLIME, 
            pos.x, pos.y, pos.z, 3, 0.2, 0.2, 0.2, 0.01);
        
        // Detecta colisão com mobs em 1.5 blocos de raio
        AABB hitbox = new AABB(pos.subtract(1.5, 1.5, 1.5), pos.add(1.5, 1.5, 1.5));
        for (LivingEntity target : player.level().getEntitiesOfClass(LivingEntity.class, hitbox, e -> e != player)) {
            target.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
            ACTIVE_SPRAYS.remove(player.getUUID());
            
            // Partículas de impacto
            player.serverLevel().sendParticles(ParticleTypes.ITEM_SLIME,
                target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                15, 0.3, 0.3, 0.3, 0.05);
            return;
        }
        
        // Remove após 15 blocos
        if (spray.distance >= 15.0) {
            ACTIVE_SPRAYS.remove(player.getUUID());
        }
    }
}
