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
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class VedalkenRace implements Race {

    @Override
    public String getId() { return "vedalken"; }

    @Override
    public String getDisplayName() { return "Vedalken"; }

    @Override
    public double getMaxHealth() { return 20.0; }

    @Override
    public double getAttackDamage() { return 1.0; }

    @Override
    public double getMovementSpeed() { return 0.10; }

    @Override
    public boolean hasNightVision() { return true; }

    // ===== Passiva: -30% dano mágico =====

    @Override
    public void onPlayerHurt(ServerPlayer player, LivingIncomingDamageEvent event) {
        DamageSource source = event.getSource();
        String type = source.typeHolder().getRegisteredName();

        if (type.contains("magic") || type.contains("wither") || 
            type.contains("indirect_magic") || type.contains("dragon_breath")) {
            event.setAmount(event.getAmount() * 0.7f);
        }
    }

    // ===== Passiva: +10% XP =====

    @Override
    public void onXpDrop(ServerPlayer player, LivingExperienceDropEvent event) {
        int xp = event.getDroppedExperience();
        event.setDroppedExperience((int) (xp * 1.1));
    }

    // ===== Habilidade H: Arcane Insight =====

    private static final Map<UUID, Long> ABILITY_COOLDOWNS = new HashMap<>();
    private static final int COOLDOWN_TICKS = 1200; // 60s

    @Override
    public boolean canUseAbility() { return true; }

    @Override
    public void executeAbility(ServerPlayer player) {
        long currentTick = player.serverLevel().getServer().getTickCount();
        Long readyAt = ABILITY_COOLDOWNS.get(player.getUUID());

        if (readyAt != null && currentTick < readyAt) {
            double seconds = (readyAt - currentTick) / 20.0;
            player.sendSystemMessage(Component.literal(String.format("§cArcane Insight em recarga: §f%.1fs", seconds)));
            return;
        }

        ServerLevel level = player.serverLevel();
        Vec3 pos = player.position();

        // Aplica Glowing em todos os monstros num raio de 10 blocos
        List<LivingEntity> monsters = level.getEntitiesOfClass(LivingEntity.class, 
            new AABB(pos.add(-10, -10, -10), pos.add(10, 10, 10)), 
            e -> e != player && e.isAlive() && e instanceof net.minecraft.world.entity.monster.Monster
        );

        if (monsters.isEmpty()) {
            player.sendSystemMessage(Component.literal("§7Nenhum inimigo detectado."));
        } else {
            for (LivingEntity monster : monsters) {
                monster.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0));
            }
            player.sendSystemMessage(Component.literal("§aArcane Insight!"));
        }

        ABILITY_COOLDOWNS.put(player.getUUID(), currentTick + COOLDOWN_TICKS);
    }
}
