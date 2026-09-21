package com.pedro.racasclasses.race.impl;

import com.pedro.racasclasses.race.Race;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TabaxiRace implements Race {

    // ===== Feline Agility (tecla H) =====
    private static final Map<UUID, Long> ABILITY_COOLDOWNS = new HashMap<>();
    private static final int ABILITY_COOLDOWN_TICKS = 600;  // 30s
    private static final int ABILITY_DURATION_TICKS = 100;  // 5s

    @Override
    public String getId() { return "tabaxi"; }

    @Override
    public String getDisplayName() { return "Tabaxi"; }

    @Override
    public double getMovementSpeed() { return 0.12; }

    @Override
    public boolean hasNightVision() { return true; }

    // ===== Garras: +2 dano desarmado =====

    @Override
    public void onAttackEntity(ServerPlayer player, LivingIncomingDamageEvent event) {
        if (!player.getMainHandItem().isEmpty()) return;
        event.setAmount(event.getAmount() + 2.0f);
    }

    // ===== Feline Agility (tecla H) =====

    @Override
    public boolean canUseAbility() { return true; }

    @Override
    public void executeAbility(ServerPlayer player) {
        long currentTick = player.serverLevel().getServer().getTickCount();
        Long readyAt = ABILITY_COOLDOWNS.get(player.getUUID());

        if (readyAt != null && currentTick < readyAt) {
            double seconds = (readyAt - currentTick) / 20.0;
            player.sendSystemMessage(Component.literal(
                    String.format("§cFeline Agility em recarga: §f%.1fs", seconds)));
            return;
        }

        // Speed III por 5s
        player.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SPEED,
                ABILITY_DURATION_TICKS,
                2, // nível 3 (Speed III)
                false, false, true));

        ABILITY_COOLDOWNS.put(player.getUUID(), currentTick + ABILITY_COOLDOWN_TICKS);
        player.sendSystemMessage(Component.literal("§aFeline Agility ativado!"));
    }

    // ===== Tick: dano na água + coelhos/galinhas fogem =====

    @Override
    public void onPlayerTick(ServerPlayer player) {
        // --- Dano na água (tipo Enderman, sem chuva) ---
        if (player.isInWater()) {
            player.hurt(player.damageSources().drown(), 1.0f);
        }

        // --- Coelhos e galinhas fogem ---
        if (player.tickCount % 20 != 0) return;

        var area = player.getBoundingBox().inflate(8.0);
        var mobs = player.serverLevel().getEntitiesOfClass(Mob.class, area, m -> {
            if (!(m instanceof Rabbit) && !(m instanceof Chicken)) return false;
            return m.isAlive() && !m.isRemoved();
        });

        for (Mob mob : mobs) {
            // Aplica Slowness (fica "nervoso") + afasta
            mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 0, false, false, false));

            var away = mob.position().subtract(player.position());
            double dist = away.length();
            if (dist > 0.5 && dist < 8.0) {
                var push = away.normalize().scale(0.3);
                mob.push(push.x, 0.1, push.z);
            }
        }
    }

    // ===== Imunidade total a queda =====

    public void onFall(ServerPlayer player, net.neoforged.neoforge.event.entity.living.LivingFallEvent event) {
        event.setCanceled(true);
    }
}