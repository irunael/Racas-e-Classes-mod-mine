package com.pedro.racasclasses.race.impl;

import com.pedro.racasclasses.race.Race;
import com.pedro.racasclasses.race.RacialWeakness;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.ArrayList;

public class HarengonRace implements Race {

    @Override
    public String getId() { return "harengon"; }

    @Override
    public String getDisplayName() { return "Harengon"; }

    @Override
    public double getMaxHealth() { return 20.0; }

    @Override
    public double getMovementSpeed() { return 0.13; }

    @Override
    public double getScale() { return 0.90; }

    @Override
    public boolean hasNightVision() { return true; }

    // ===== Pulo mais alto (atributo JUMP_STRENGTH) =====

    @Override
    public double getJumpStrengthBonus() { return 0.15; }

    // ===== Resistência a queda (5 blocos) =====

    public void onFall(ServerPlayer player, LivingFallEvent event) {
        float distance = event.getDistance();

        if (distance <= 5.0f) {
            event.setCanceled(true);
        } else {
            event.setDistance(distance - 5.0f);
        }
    }

    // ===== Sorte: +30% chance de drop extra =====

    @Override
    public void onMobDrops(ServerPlayer player, LivingEntity killed, LivingDropsEvent event) {
        if (player.getRandom().nextFloat() >= 0.30f) return;

        var drops = event.getDrops();
        if (drops.isEmpty()) return;

        ArrayList<ItemEntity> dropList = new ArrayList<>(drops);
        ItemEntity original = dropList.get(player.getRandom().nextInt(dropList.size()));

        ItemStack copy = original.getItem().copy();
        copy.setCount(1);

        ServerLevel level = (ServerLevel) event.getEntity().level();
        ItemEntity extra = new ItemEntity(
                level,
                event.getEntity().getX(),
                event.getEntity().getY() + 0.5,
                event.getEntity().getZ(),
                copy
        );
        drops.add(extra);
    }

    // ===== Fraqueza: +50% dano de fogo =====

    @Override
    public void onPlayerHurt(ServerPlayer player, LivingIncomingDamageEvent event) {
        RacialWeakness.applyFire(event, 1.5f);
    }
}