package com.pedro.racasclasses.race.impl;

import com.pedro.racasclasses.race.Race;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
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

public class HexbloodRace implements Race {

    @Override
    public String getId() { return "hexblood"; }

    @Override
    public String getDisplayName() { return "Hexblood"; }

    @Override
    public double getMaxHealth() { return 20.0; }

    @Override
    public double getAttackDamage() { return 1.0; }

    @Override
    public double getMovementSpeed() { return 0.10; }

    @Override
    public boolean hasNightVision() { return true; }

    // ===== Passiva: -20% dano mágico =====

    @Override
    public void onPlayerHurt(ServerPlayer player, LivingIncomingDamageEvent event) {
        DamageSource source = event.getSource();
        String type = source.typeHolder().getRegisteredName();

        if (type.contains("magic") || type.contains("wither") || 
            type.contains("indirect_magic") || type.contains("dragon_breath")) {
            event.setAmount(event.getAmount() * 0.8f);
        }

        // Passiva: atacante corpo a corpo recebe nausea
        if (source.getEntity() instanceof LivingEntity attacker && source.getDirectEntity() == attacker) {
            attacker.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 60, 0));
        }
    }

    // ===== Habilidade H: Hex =====

    private static final Map<UUID, Long> ABILITY_COOLDOWNS = new HashMap<>();
    private static final Map<UUID, UUID> HEXED_TARGETS = new HashMap<>(); // player -> target
    private static final Map<UUID, Long> HEX_DURATIONS = new HashMap<>(); // target -> endTick
    private static final int COOLDOWN_TICKS = 900; // 45s
    private static final int HEX_DURATION_TICKS = 600; // 30s

    @Override
    public boolean canUseAbility() { return true; }

    @Override
    public void executeAbility(ServerPlayer player) {
        long currentTick = player.serverLevel().getServer().getTickCount();
        Long readyAt = ABILITY_COOLDOWNS.get(player.getUUID());

        if (readyAt != null && currentTick < readyAt) {
            double seconds = (readyAt - currentTick) / 20.0;
            player.sendSystemMessage(Component.literal(String.format("§cHex em recarga: §f%.1fs", seconds)));
            return;
        }

        ServerLevel level = player.serverLevel();
        Vec3 pos = player.position();

        // Encontra mob mais próximo num raio de 10 blocos
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, 
            new AABB(pos.add(-10, -10, -10), pos.add(10, 10, 10)), 
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
            HEXED_TARGETS.put(player.getUUID(), target.getUUID());
            HEX_DURATIONS.put(target.getUUID(), currentTick + HEX_DURATION_TICKS);
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, HEX_DURATION_TICKS, 0));
            
            player.sendSystemMessage(Component.literal("§aHex marcado!"));
            ABILITY_COOLDOWNS.put(player.getUUID(), currentTick + COOLDOWN_TICKS);
        }
    }

    // ===== Dano aumentado no alvo hexado =====

    @Override
    public void onAttackEntity(ServerPlayer player, LivingIncomingDamageEvent event) {
        long currentTick = player.serverLevel().getServer().getTickCount();
        
        if (event.getEntity() instanceof LivingEntity target) {
            UUID hexedTargetId = HEXED_TARGETS.get(player.getUUID());
            
            if (hexedTargetId != null && hexedTargetId.equals(target.getUUID())) {
                Long hexEnd = HEX_DURATIONS.get(target.getUUID());
                
                if (hexEnd != null && currentTick < hexEnd) {
                    event.setAmount(event.getAmount() * 1.2f);
                } else {
                    // Hex expirou
                    HEXED_TARGETS.remove(player.getUUID());
                    HEX_DURATIONS.remove(target.getUUID());
                }
            }
        }
    }
}
