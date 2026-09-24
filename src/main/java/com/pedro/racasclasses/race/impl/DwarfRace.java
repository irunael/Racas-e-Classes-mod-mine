package com.pedro.racasclasses.race.impl;

import com.pedro.racasclasses.RacasClasses;
import com.pedro.racasclasses.race.Race;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.event.level.BlockEvent;

public class DwarfRace implements Race {

    private static final float SMELT_CHANCE = 0.03f;

    @Override
    public String getId() { return "dwarf"; }

    @Override
    public String getDisplayName() { return "Anão"; }

    // PHB: +2 CON
    @Override public int getRacialConstitution() { return 2; }
    @Override public int getFreeAttributePoints() { return 0; }

    @Override
    public double getMaxHealth() { return 24.0; }

    @Override
    public double getMovementSpeed() { return 0.09; }

    @Override
    public boolean hasNightVision() { return true; }

    @Override
    public double getPoisonResistance() { return 0.50; }

    @Override
    public double getMiningSpeedBonus() { return 0.20; }

    @Override
    public double getScale() { return 0.60; }

    @Override
    public double getSwimSpeedBonus() { return 0.0; }

    // ===== Anão não sabe nadar =====

    @Override
    public void onPlayerTick(ServerPlayer player) {
        if (player.tickCount % 2 != 0) return;

        if (player.isInWater() || player.isUnderWater()) {
            // Bloqueia sprint (impede o nado)
            player.setSprinting(false);

            // Bloqueia swimming (caso o cliente tente ativar)
            player.setSwimming(false);

            // Aplica Slowness IV
            player.addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN,
                    20,
                    3,
                    false, false, false
            ));
        }
    }

    // ===== Minério esquentado (3% de chance) =====

    @Override
    public void onBlockBreak(ServerPlayer player, BlockEvent.BreakEvent event) {
        Block block = event.getState().getBlock();
        Item smelted = getSmeltedItem(block);
        if (smelted == null) return;

        if (player.getRandom().nextFloat() >= SMELT_CHANCE) return;

        event.setCanceled(true);
        player.level().destroyBlock(event.getPos(), false, player);
        Block.popResource(player.level(), event.getPos(), new ItemStack(smelted, 1));

        RacasClasses.LOGGER.info("[DWARF] Minério esquentado: {} em {}",
                smelted, event.getPos());
    }

    private Item getSmeltedItem(Block block) {
        if (block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE) {
            return Items.IRON_INGOT;
        }
        if (block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE || block == Blocks.NETHER_GOLD_ORE) {
            return Items.GOLD_INGOT;
        }
        if (block == Blocks.COPPER_ORE || block == Blocks.DEEPSLATE_COPPER_ORE) {
            return Items.COPPER_INGOT;
        }
        return null;
    }
}