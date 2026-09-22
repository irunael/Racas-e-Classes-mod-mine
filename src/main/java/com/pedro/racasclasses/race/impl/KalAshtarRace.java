package com.pedro.racasclasses.race.impl;

import com.pedro.racasclasses.race.Race;
import com.pedro.racasclasses.race.RacialWeakness;
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

public class KalAshtarRace implements Race {

    @Override
    public String getId() { return "kalashtar"; }

    @Override
    public String getDisplayName() { return "Kalashtar"; }

    @Override
    public double getMaxHealth() { return 22.0; }

    @Override
    public double getAttackDamage() { return 1.0; }

    @Override
    public double getMovementSpeed() { return 0.10; }

    @Override
    public boolean hasNightVision() { return true; }

    // ===== Fraqueza: +30% dano físico + Resistência mágica =====

    @Override
    public void onPlayerHurt(ServerPlayer player, LivingIncomingDamageEvent event) {
        DamageSource source = event.getSource();
        String type = source.typeHolder().getRegisteredName();

        // Verifica se é dano mágico
        boolean isMagic = type.contains("magic") || type.contains("wither") ||
            type.contains("indirect_magic") || type.contains("dragon_breath");

        if (isMagic) {
            // Resistência mágica (-50%)
            event.setAmount(event.getAmount() * 0.5f);
        } else {
            // Fraqueza: +30% dano físico (só aplica se não for mágico)
            RacialWeakness.applyPhysical(event, 1.3f);
        }
    }

    // ===== Passiva: Imune a Nausea =====

    @Override
    public void onPlayerTick(ServerPlayer player) {
        // Remove Nausea se aplicado
        if (player.hasEffect(MobEffects.CONFUSION)) {
            player.removeEffect(MobEffects.CONFUSION);
        }

        // Telepatia: mostra players próximos quando agachado (a cada 1s = 20 ticks)
        long currentTick = player.serverLevel().getServer().getTickCount();
        Long lastTeleCheck = TELEPATHY_CHECKS.get(player.getUUID());

        if (player.isCrouching() && (lastTeleCheck == null || currentTick - lastTeleCheck >= 20)) {
            showNearbyPlayers(player);
            TELEPATHY_CHECKS.put(player.getUUID(), currentTick);
        }
    }

    private static final Map<UUID, Long> TELEPATHY_CHECKS = new HashMap<>();

    private void showNearbyPlayers(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 pos = player.position();

        List<ServerPlayer> nearbyPlayers = level.getEntitiesOfClass(ServerPlayer.class, 
            new AABB(pos.add(-20, -20, -20), pos.add(20, 20, 20)), 
            p -> p != player && p.isAlive()
        );

        if (!nearbyPlayers.isEmpty()) {
            StringBuilder message = new StringBuilder("§d[Telepatia] §7Detectados: ");
            for (int i = 0; i < nearbyPlayers.size(); i++) {
                message.append("§f").append(nearbyPlayers.get(i).getName().getString());
                if (i < nearbyPlayers.size() - 1) {
                    message.append("§7, ");
                }
            }
            player.sendSystemMessage(Component.literal(message.toString()));
        }
    }

    // ===== Habilidade H: Mind Link =====

    private static final Map<UUID, Long> ABILITY_COOLDOWNS = new HashMap<>();
    private static final int COOLDOWN_TICKS = 600; // 30s

    @Override
    public boolean canUseAbility() { return true; }

    @Override
    public void executeAbility(ServerPlayer player) {
        long currentTick = player.serverLevel().getServer().getTickCount();
        Long readyAt = ABILITY_COOLDOWNS.get(player.getUUID());

        if (readyAt != null && currentTick < readyAt) {
            double seconds = (readyAt - currentTick) / 20.0;
            player.sendSystemMessage(Component.literal(String.format("§cMind Link em recarga: §f%.1fs", seconds)));
            return;
        }

        ServerLevel level = player.serverLevel();
        Vec3 pos = player.position();

        // Aplica Glowing em todos os mobs hostis num raio de 16 blocos
        List<LivingEntity> hostiles = level.getEntitiesOfClass(LivingEntity.class, 
            new AABB(pos.add(-16, -16, -16), pos.add(16, 16, 16)), 
            e -> e != player && e.isAlive() && e instanceof net.minecraft.world.entity.monster.Monster
        );

        if (hostiles.isEmpty()) {
            player.sendSystemMessage(Component.literal("§7Nenhum inimigo detectado."));
        } else {
            for (LivingEntity hostile : hostiles) {
                hostile.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0)); // 10s
            }
            player.sendSystemMessage(Component.literal("§aMind Link!"));
        }

        ABILITY_COOLDOWNS.put(player.getUUID(), currentTick + COOLDOWN_TICKS);
    }
}
