package com.pedro.racasclasses.race.impl;

import com.pedro.racasclasses.race.Race;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

public class TritonRace implements Race {

    @Override
    public String getId() { return "triton"; }

    @Override
    public String getDisplayName() { return "Triton"; }

    @Override
    public double getMaxHealth() { return 22.0; }

    // ⚠️ Night Vision só quando submerso → gerenciado no onPlayerTick
    @Override
    public boolean hasNightVision() { return false; }

    // ⚠️ Removido +50% nado → gerenciado por Dolphin's Grace
    // ⚠️ Removido +30% mineração → gerenciado por Haste quando submerso

    // ===== Tick: buffs quando submerso =====

    // ===== Tick: buffs quando submerso (cabeça debaixo d'água) =====

    @Override
    public void onPlayerTick(ServerPlayer player) {
        if (player.tickCount % 20 != 0) return;

        if (player.isUnderWater()) {
            // Respiração infinita
            player.addEffect(new MobEffectInstance(
                    MobEffects.WATER_BREATHING,
                    300, 0, false, false, false));

            // Night Vision enquanto submerso
            player.addEffect(new MobEffectInstance(
                    MobEffects.NIGHT_VISION,
                    300, 0, false, false, false));

            // Dolphin's Grace (nada rápido)
            player.addEffect(new MobEffectInstance(
                    MobEffects.DOLPHINS_GRACE,
                    300, 0, false, false, false));

            // Mining Speed III
            player.addEffect(new MobEffectInstance(
                    MobEffects.DIG_SPEED,
                    40, 2, false, false, false));

            // Strength II
            player.addEffect(new MobEffectInstance(
                    MobEffects.DAMAGE_BOOST,
                    40, 1, false, false, false));
        }
    }

    // ===== +20% dano com tridente =====

    @Override
    public void onAttackEntity(ServerPlayer player, LivingIncomingDamageEvent event) {
        if (!player.getMainHandItem().is(net.minecraft.world.item.Items.TRIDENT)) return;
        event.setAmount(event.getAmount() * 1.20f);
    }

    // ===== Resistência a freeze (50%) =====

    @Override
    public void onPlayerHurt(ServerPlayer player, LivingIncomingDamageEvent event) {
        if (event.getSource().is(DamageTypes.FREEZE)) {
            event.setAmount(event.getAmount() * 0.5f);
        }
    }
}