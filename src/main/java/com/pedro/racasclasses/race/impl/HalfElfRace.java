package com.pedro.racasclasses.race.impl;

import com.pedro.racasclasses.race.Race;
import com.pedro.racasclasses.race.RacialWeakness;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;

public class HalfElfRace implements Race {

    private static final int SEARCH_RADIUS = 8;
    private static final int MIN_LEAVES = 4;

    @Override
    public String getId() { return "halfelf"; }

    @Override
    public String getDisplayName() { return "Meio-Elfo"; }

    // Sem sub-raça no mod: +2 LUCK (CHA) e 2 pontos livres na HUD.
    @Override public int getRacialLuck() { return 2; }
    @Override public int getFreeAttributePoints() { return 2; }

    @Override
    public double getMaxHealth() { return 22.0; }

    @Override
    public boolean hasNightVision() { return true; }

    @Override
    public double getSwimSpeedBonus() { return 0.15; }

    // ===== Invisibilidade em folhagem =====

    @Override
    public void onPlayerTick(ServerPlayer player) {
        if (player.tickCount % 20 != 0) return;

        RacialWeakness.fasterHunger(player, 0.15f);

        if (hasNearbyLeaves(player.serverLevel(), player.blockPosition())) {
            player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 40, 0, false, false, false));
        }
    }

    private boolean hasNearbyLeaves(ServerLevel level, BlockPos center) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int count = 0;
        for (int x = -SEARCH_RADIUS; x <= SEARCH_RADIUS; x += 2) {
            for (int z = -SEARCH_RADIUS; z <= SEARCH_RADIUS; z += 2) {
                for (int y = -4; y <= 8; y += 2) {
                    pos.set(center.getX() + x, center.getY() + y, center.getZ() + z);
                    if (pos.getY() < level.getMinBuildHeight() || pos.getY() >= level.getMaxBuildHeight()) continue;
                    BlockState state = level.getBlockState(pos);
                    if (state.is(BlockTags.LEAVES)) {
                        count++;
                        if (count >= MIN_LEAVES) return true;
                    }
                }
            }
        }
        return false;
    }

    // ===== Bônus de +10% XP =====

    @Override
    public void onXpDrop(ServerPlayer player, LivingExperienceDropEvent event) {
        int originalXp = event.getDroppedExperience();
        int newXp = Math.round(originalXp * 1.10f);
        event.setDroppedExperience(newXp);
    }
}