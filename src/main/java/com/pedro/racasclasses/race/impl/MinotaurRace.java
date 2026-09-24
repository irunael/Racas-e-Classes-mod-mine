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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MinotaurRace implements Race {

    // ===== Goring Rush (tecla H) =====
    private static final Map<UUID, Long> ABILITY_COOLDOWNS = new HashMap<>();
    private static final int ABILITY_COOLDOWN_TICKS = 600;  // 30s
    private static final int ABILITY_DURATION_TICKS = 100;  // 5s

    private static final net.minecraft.resources.ResourceLocation ID_KNOCKBACK =
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("racasclasses", "minotaur_knockback");

    @Override
    public String getId() { return "minotaur"; }

    @Override
    public String getDisplayName() { return "Minotaur"; }

    @Override
    public double getMaxHealth() { return 24.0; }

    @Override
    public double getAttackDamage() { return 1.2; }

    @Override
    public double getScale() { return 1.20; }

    // ===== Aplicar knockback aumentado ao entrar =====

    @Override
    public void onPlayerJoin(ServerPlayer player) {
        applyKnockback(player);
    }

    @Override
    public void onRaceEnter(ServerPlayer player) {
        applyKnockback(player);
    }

    @Override
    public void onRaceExit(ServerPlayer player) {
        AttributeInstance attr = player.getAttribute(Attributes.ATTACK_KNOCKBACK);
        if (attr != null && attr.getModifier(ID_KNOCKBACK) != null) {
            attr.removeModifier(ID_KNOCKBACK);
        }
    }

    @Override
    public void onPlayerRespawn(ServerPlayer player) {
        applyKnockback(player);
    }

    private void applyKnockback(ServerPlayer player) {
        AttributeInstance attr = player.getAttribute(Attributes.ATTACK_KNOCKBACK);
        if (attr != null) {
            if (attr.getModifier(ID_KNOCKBACK) != null) {
                attr.removeModifier(ID_KNOCKBACK);
            }
            attr.addPermanentModifier(new AttributeModifier(
                    ID_KNOCKBACK, 2.0, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    // ===== +30% dano se correndo =====

    @Override
    public void onAttackEntity(ServerPlayer player, LivingIncomingDamageEvent event) {
        if (player.isSprinting()) {
            event.setAmount(event.getAmount() * 1.30f);
        }
    }

    // ===== Goring Rush (H) =====

    @Override
    public boolean canUseAbility() { return true; }

    @Override
    public void executeAbility(ServerPlayer player) {
        long currentTick = player.serverLevel().getServer().getTickCount();
        Long readyAt = ABILITY_COOLDOWNS.get(player.getUUID());

        if (readyAt != null && currentTick < readyAt) {
            double seconds = (readyAt - currentTick) / 20.0;
            player.sendSystemMessage(Component.literal(
                    String.format("§cGoring Rush em recarga: §f%.1fs", seconds)));
            return;
        }

        player.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SPEED, ABILITY_DURATION_TICKS, 1, false, false, true));

        ABILITY_COOLDOWNS.put(player.getUUID(), currentTick + ABILITY_COOLDOWN_TICKS);
        player.sendSystemMessage(Component.literal("§aGoring Rush ativado!"));
    }

    // ===== -15% dano recebido =====

    @Override
    public void onPlayerHurt(ServerPlayer player, LivingIncomingDamageEvent event) {
        DamageSource source = event.getSource();

        // Fraqueza: +30% dano perfurante (primeiro, substitui a resistência)
        boolean isPiercing =
                source.is(DamageTypes.ARROW)
                        || source.is(DamageTypes.TRIDENT)
                        || source.is(DamageTypes.THROWN);

        if (isPiercing) {
            event.setAmount(event.getAmount() * 1.3f);
            return;
        }

        // Resistência geral (-15%)
        event.setAmount(event.getAmount() * 0.85f);
    }
}
