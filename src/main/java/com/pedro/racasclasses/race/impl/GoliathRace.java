package com.pedro.racasclasses.race.impl;

import com.pedro.racasclasses.race.Race;
import com.pedro.racasclasses.race.RacialWeakness;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GoliathRace implements Race {

    // ===== Stone's Endurance (tecla H) =====
    private static final Map<UUID, Long> ABILITY_COOLDOWNS = new HashMap<>();
    private static final int ABILITY_COOLDOWN_TICKS = 1200; // 60s
    private static final int ABILITY_DURATION_TICKS = 200;  // 10s

    // ===== Resistência a knockback =====
    private static final net.minecraft.resources.ResourceLocation ID_KNOCKBACK_RESIST =
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("racasclasses", "goliath_knockback_resist");

    @Override
    public String getId() { return "goliath"; }

    @Override
    public String getDisplayName() { return "Goliath"; }

    @Override
    public double getMaxHealth() { return 28.0; }

    @Override
    public double getAttackDamage() { return 1.3; }

    @Override
    public double getMovementSpeed() { return 0.07; }

    @Override
    public double getScale() { return 1.50; }

    // ===== Resistência a knockback =====

    @Override
    public void onPlayerJoin(ServerPlayer player) {
        applyKnockbackResist(player);
    }

    @Override
    public void onRaceEnter(ServerPlayer player) {
        applyKnockbackResist(player);
    }

    @Override
    public void onRaceExit(ServerPlayer player) {
        AttributeInstance attr = player.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (attr != null && attr.getModifier(ID_KNOCKBACK_RESIST) != null) {
            attr.removeModifier(ID_KNOCKBACK_RESIST);
        }
    }

    @Override
    public void onPlayerRespawn(ServerPlayer player) {
        applyKnockbackResist(player);
    }

    private void applyKnockbackResist(ServerPlayer player) {
        AttributeInstance attr = player.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (attr != null) {
            if (attr.getModifier(ID_KNOCKBACK_RESIST) != null) {
                attr.removeModifier(ID_KNOCKBACK_RESIST);
            }
            attr.addPermanentModifier(new AttributeModifier(
                    ID_KNOCKBACK_RESIST, 0.5, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    // ===== Stone's Endurance (tecla H) =====

    @Override
    public boolean canUseAbility() { return true; }

    @Override
    public void executeAbility(ServerPlayer player) {
        long currentTick = player.serverLevel().getServer().getTickCount();
        Long readyAt = ABILITY_COOLDOWNS.get(player.getUUID());

        if (readyAt != null && currentTick < readyAt) {
            double seconds = (readyAt - currentTick) / 20.0;
            player.sendSystemMessage(Component.literal(
                    String.format("§cStone's Endurance em recarga: §f%.1fs", seconds)));
            return;
        }

        // Aplica Resistance IV por 10s (que é -80% de dano)
        player.addEffect(new MobEffectInstance(
                MobEffects.DAMAGE_RESISTANCE,
                ABILITY_DURATION_TICKS,
                3, // nível 4 = -80% dano (o mais próximo de 75%)
                false, false, true));

        ABILITY_COOLDOWNS.put(player.getUUID(), currentTick + ABILITY_COOLDOWN_TICKS);
        player.sendSystemMessage(Component.literal("§aStone's Endurance ativado!"));
    }

    // ===== Redução de dano de queda (-5) =====

    public void onFall(ServerPlayer player, net.neoforged.neoforge.event.entity.living.LivingFallEvent event) {
        float distance = event.getDistance();

        // Se cair até 5 blocos, cancela
        if (distance <= 5.0f) {
            event.setCanceled(true);
        } else {
            // Reduz a distância em 5 blocos
            event.setDistance(distance - 5.0f);
        }
    }

    // ===== Fraqueza: +30% dano de explosão =====
    @Override
    public void onPlayerHurt(ServerPlayer player, LivingIncomingDamageEvent event) {
        RacialWeakness.applyExplosion(event, 1.3f);
    }
}
