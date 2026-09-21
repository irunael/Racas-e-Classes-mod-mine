package com.pedro.racasclasses.race.impl;

import com.pedro.racasclasses.RacasClasses;
import com.pedro.racasclasses.race.Race;

import net.minecraft.server.level.ServerPlayer;
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

    // ===== Minério esquentado (3% de chance) =====

    @Override
    public void onBlockBreak(ServerPlayer player, BlockEvent.BreakEvent event) {
        Block block = event.getState().getBlock();
        Item smelted = getSmeltedItem(block);
        if (smelted == null) return;

        if (player.getRandom().nextFloat() >= SMELT_CHANCE) return;

        // Cancela o drop normal
        event.setCanceled(true);

        // Quebra o bloco sem dropar o normal
        player.level().destroyBlock(event.getPos(), false, player);

        // Dropa o item processado
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