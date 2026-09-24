package com.pedro.racasclasses.race.impl;

import com.pedro.racasclasses.race.Race;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LeoninRace implements Race {

    private static final Map<UUID, Long> ABILITY_COOLDOWNS = new HashMap<>();
    private static final int ABILITY_COOLDOWN_TICKS = 900;  // 45s
    private static final int ABILITY_DURATION_TICKS = 200;  // 10s

    @Override
    public String getId() { return "leonin"; }

    @Override
    public String getDisplayName() { return "Leonin"; }

    @Override
    public double getMaxHealth() { return 22.0; }

    @Override
    public double getMovementSpeed() { return 0.11; }

    @Override
    public double getAttackDamage() { return 1.1; }

    @Override
    public double getScale() { return 1.10; }

    @Override
    public boolean hasNightVision() { return true; }

    // ===== +20% dano melee em mobs =====

    @Override
    public void onAttackEntity(ServerPlayer player, LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Mob)) return;
        event.setAmount(event.getAmount() * 1.20f);
    }

    // ===== Daunting Roar (H) =====

    @Override
    public boolean canUseAbility() { return true; }

    @Override
    public void executeAbility(ServerPlayer player) {
        long currentTick = player.serverLevel().getServer().getTickCount();
        Long readyAt = ABILITY_COOLDOWNS.get(player.getUUID());

        if (readyAt != null && currentTick < readyAt) {
            double seconds = (readyAt - currentTick) / 20.0;
            player.sendSystemMessage(Component.literal(
                    String.format("§cDaunting Roar em recarga: §f%.1fs", seconds)));
            return;
        }

        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, ABILITY_DURATION_TICKS, 2, false, false, true));

        ABILITY_COOLDOWNS.put(player.getUUID(), currentTick + ABILITY_COOLDOWN_TICKS);
        player.sendSystemMessage(Component.literal("§aDaunting Roar ativado!"));
    }

    // ===== Fome 50% mais rápida =====

    @Override
    public void onPlayerTick(ServerPlayer player) {
        if (player.tickCount % 20 != 0) return;

        FoodData food = player.getFoodData();
        food.addExhaustion(0.5f);
    }

    // ===== Carnívoro (só come carne) =====

    public void onItemUse(ServerPlayer player, PlayerInteractEvent.RightClickItem event) {
        ItemStack item = event.getItemStack();
        if (item.isEmpty()) return;

        if (isVegetable(item)) {
            event.setCanceled(true);
            player.sendSystemMessage(Component.literal("§cLeonin só come carne!"));
        }
    }

    private boolean isVegetable(ItemStack item) {
        var i = item.getItem();
        return i == Items.CARROT
                || i == Items.GOLDEN_CARROT
                || i == Items.POTATO
                || i == Items.BAKED_POTATO
                || i == Items.POISONOUS_POTATO
                || i == Items.BEETROOT
                || i == Items.BEETROOT_SOUP
                || i == Items.BREAD
                || i == Items.COOKIE
                || i == Items.CAKE
                || i == Items.MELON_SLICE
                || i == Items.APPLE
                || i == Items.GOLDEN_APPLE
                || i == Items.ENCHANTED_GOLDEN_APPLE
                || i == Items.SWEET_BERRIES
                || i == Items.GLOW_BERRIES
                || i == Items.PUMPKIN_PIE
                || i == Items.MUSHROOM_STEW
                || i == Items.SUSPICIOUS_STEW
                || i == Items.DRIED_KELP
                || i == Items.WHEAT
                || i == Items.WHEAT_SEEDS
                || i == Items.MELON_SEEDS
                || i == Items.PUMPKIN_SEEDS
                || i == Items.BEETROOT_SEEDS
                || i == Items.SUGAR_CANE;
    }
}