package com.pedro.racasclasses.race.impl;

import com.pedro.racasclasses.race.Race;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

public class QuachoRace implements Race {

    @Override
    public String getId() { return "quacho"; }

    @Override
    public String getDisplayName() { return "Quacho"; }

    @Override
    public double getMaxHealth() { return 18.0; }

    @Override
    public double getMovementSpeed() { return 0.11; }

    @Override
    public double getScale() { return 0.65; }

    // ===== Tick: respira na água infinitamente =====

    @Override
    public void onPlayerTick(ServerPlayer player) {
        if (player.tickCount % 20 != 0) return;

        if (player.isInWater() || player.isUnderWater()) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.WATER_BREATHING, 300, 0, false, false, false));
        }
    }

    // ===== Resistência a poison + Poisonous Skin =====

    @Override
    public void onPlayerHurt(ServerPlayer player, LivingIncomingDamageEvent event) {
        var source = event.getSource();

        // --- Imunidade a poison (dano do efeito) ---
        if (source.is(DamageTypes.MAGIC) && player.hasEffect(MobEffects.POISON)) {
            event.setAmount(0f);
            return;
        }
        if (source.getMsgId().equals("magic") && player.hasEffect(MobEffects.POISON)) {
            event.setAmount(0f);
            return;
        }

        // --- Poisonous Skin ---
        Entity attacker = source.getEntity();
        if (attacker == null) attacker = source.getDirectEntity();

        if (attacker instanceof LivingEntity living && living != player) {
            living.addEffect(new MobEffectInstance(
                    MobEffects.POISON, 60, 0, false, false, true));
        }

        // --- Fraqueza: Fogo/Lava/Queimadura (+30%) ---
        boolean isFire =
                source.is(DamageTypes.IN_FIRE)
                        || source.is(DamageTypes.ON_FIRE)
                        || source.is(DamageTypes.LAVA)
                        || source.is(DamageTypes.HOT_FLOOR);

        if (isFire) {
            event.setAmount(event.getAmount() * 1.3f);
            return;
        }

        // --- Resistência geral: -10% ---
        event.setAmount(event.getAmount() * 0.9f);
    }
    /** Bloqueia o efeito Poison de ser aplicado */
    public void onEffectApplicable(ServerPlayer player, net.neoforged.neoforge.event.entity.living.MobEffectEvent.Applicable event) {
        if (event.getEffectInstance().getEffect() == MobEffects.POISON) {
            event.setResult(net.neoforged.neoforge.event.entity.living.MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
        }
    }
}