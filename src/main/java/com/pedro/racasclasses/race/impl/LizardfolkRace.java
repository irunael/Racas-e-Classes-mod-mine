package com.pedro.racasclasses.race.impl;

import com.pedro.racasclasses.race.Race;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

public class LizardfolkRace implements Race {

    @Override
    public String getId() { return "lizardfolk"; }

    @Override
    public String getDisplayName() { return "Lizardfolk"; }

    @Override
    public double getMaxHealth() { return 22.0; }

    @Override
    public double getArmor() { return 4.0; }

    @Override
    public double getSwimSpeedBonus() { return 0.50; }

    // ===== Mordida: +3 dano desarmado =====

    @Override
    public void onAttackEntity(ServerPlayer player, LivingIncomingDamageEvent event) {
        if (!player.getMainHandItem().isEmpty()) return;
        event.setAmount(event.getAmount() + 3.0f);
    }

    // ===== Ao matar mob: cura 1 ❤️ =====

    @Override
    public void onXpDrop(ServerPlayer player, net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent event) {
        float newHealth = Math.min(player.getHealth() + 2.0f, player.getMaxHealth());
        player.setHealth(newHealth);
    }

    // ===== Tick: respiração aquática + fraqueza lava =====

    @Override
    public void onPlayerTick(ServerPlayer player) {
        if (player.tickCount % 20 != 0) return;

        // Respiração aquática (efeito capacete de tartaruga)
        if (player.isInWater() || player.isUnderWater()) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.WATER_BREATHING,
                    300, 0, false, false, false));
        }
    }

    // ===== Fraqueza: +75% dano na lava =====

    @Override
    public void onPlayerHurt(ServerPlayer player, LivingIncomingDamageEvent event) {
        if (event.getSource().is(DamageTypes.LAVA)) {
            event.setAmount(event.getAmount() * 1.75f);
        }
    }
}