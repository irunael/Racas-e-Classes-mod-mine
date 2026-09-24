package com.pedro.racasclasses.race.impl;

import com.pedro.racasclasses.capability.ModAttachments;
import com.pedro.racasclasses.capability.PlayerRaceData;
import com.pedro.racasclasses.race.Race;
import com.pedro.racasclasses.race.RacialWeakness;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.state.BlockState;

public class GnomeRace implements Race {

    private static final int SEARCH_RADIUS = 8;
    private static final int FOREST_MIN_LEAVES = 4;

    @Override
    public String getId() { return "gnome"; }

    @Override
    public String getDisplayName() { return "Gnomo"; }

    @Override public int getRacialIntelligence() { return 2; }
    @Override public int getFreeAttributePoints() { return 0; }

    @Override
    public double getMaxHealth() { return 20.0; }

    @Override
    public double getMovementSpeed() { return 0.09; }

    @Override
    public double getScale() { return 0.60; }

    @Override
    public boolean hasNightVision() { return true; }

    // ===== Sub-raça =====

    @Override
    public boolean hasSubrace() { return true; }

    @Override
    public String[] getSubraceIds() {
        return new String[]{"forest", "rock"};
    }

    @Override
    public void onSubraceChosen(ServerPlayer player, String subraceId) {
        PlayerRaceData data = player.getData(ModAttachments.PLAYER_RACE);
        data.setGnomeSubrace(subraceId);

        var attr = player.getData(ModAttachments.PLAYER_ATTRIBUTES);
        switch (subraceId) {
            case "forest" -> attr.addDexterity(1);
            case "rock" -> attr.addConstitution(1);
        }
    }

    @Override
    public String getSubraceDisplayName(ServerPlayer player) {
        PlayerRaceData data = player.getData(ModAttachments.PLAYER_RACE);
        String id = data.getGnomeSubrace();
        return switch (id) {
            case "forest" -> "Gnomo da Floresta";
            case "rock" -> "Gnomo da Rocha";
            default -> id;
        };
    }

    // ===== HP extra do Rock (+2) =====

    @Override
    public double getSubraceHealthBonus(ServerPlayer player) {
        PlayerRaceData data = player.getData(ModAttachments.PLAYER_RACE);
        if (data.getGnomeSubrace().equals("rock")) return 2.0;
        return 0.0;
    }

    // ===== Fraquezas =====

    @Override
    public void onPlayerHurt(ServerPlayer player, net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent event) {
        PlayerRaceData data = player.getData(ModAttachments.PLAYER_RACE);
        if (data.getGnomeSubrace().equals("forest")) {
            RacialWeakness.applyFire(event, 1.5f);
        } else if (data.getGnomeSubrace().equals("rock")) {
            RacialWeakness.applyExplosion(event, 1.3f);
        }
    }

    // ===== Tick (Forest: Speed perto de folhagem) =====

    @Override
    public void onPlayerTick(ServerPlayer player) {
        if (player.tickCount % 20 != 0) return;

        PlayerRaceData data = player.getData(ModAttachments.PLAYER_RACE);
        if (!data.getGnomeSubrace().equals("forest")) return;

        if (hasNearbyLeaves(player.serverLevel(), player.blockPosition())) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SPEED, 40, 0, false, false, false));
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
                        if (count >= FOREST_MIN_LEAVES) return true;
                    }
                }
            }
        }
        return false;
    }

    // ===== Imunidade a queda do Rock (3 blocos) =====

    public void onFall(ServerPlayer player, net.neoforged.neoforge.event.entity.living.LivingFallEvent event) {
        PlayerRaceData data = player.getData(ModAttachments.PLAYER_RACE);
        if (!data.getGnomeSubrace().equals("rock")) return;

        float distance = event.getDistance();
        if (distance <= 3.0f) {
            event.setCanceled(true);
        } else {
            event.setDistance(distance - 3.0f);
        }
    }

    // ===== Mobs passivos/lobos/aranhas não atacam Forest =====

    @Override
    public void onMobTarget(ServerPlayer player, net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent event) {
        PlayerRaceData data = player.getData(ModAttachments.PLAYER_RACE);
        if (!data.getGnomeSubrace().equals("forest")) return;

        if (!(event.getEntity() instanceof net.minecraft.world.entity.Mob mob)) return;
        net.minecraft.world.entity.Entity target = event.getNewAboutToBeSetTarget();
        if (!(target instanceof ServerPlayer p)) return;
        if (p != player) return;

        if (mob instanceof net.minecraft.world.entity.animal.Animal
                || mob instanceof net.minecraft.world.entity.animal.Wolf
                || mob instanceof net.minecraft.world.entity.monster.Spider) {
            event.setCanceled(true);
        }
    }
    // ===== XP extra em minérios (Rock) =====

    @Override
    public void onBlockBreak(ServerPlayer player, net.neoforged.neoforge.event.level.BlockEvent.BreakEvent event) {
        PlayerRaceData data = player.getData(ModAttachments.PLAYER_RACE);
        if (!data.getGnomeSubrace().equals("rock")) return;

        // Verifica se é minério
        net.minecraft.world.level.block.Block block = event.getState().getBlock();
        if (!isOre(block)) return;

        // Spawna XP extra (50% do XP normal)
        int extraXp = getOreXp(block);
        if (extraXp > 0) {
            net.minecraft.world.entity.ExperienceOrb.award(
                    (ServerLevel) player.level(),
                    net.minecraft.world.phys.Vec3.atCenterOf(event.getPos()),
                    extraXp / 2 // 50% extra
            );
        }
    }

    private boolean isOre(net.minecraft.world.level.block.Block block) {
        return block == net.minecraft.world.level.block.Blocks.COAL_ORE
                || block == net.minecraft.world.level.block.Blocks.DEEPSLATE_COAL_ORE
                || block == net.minecraft.world.level.block.Blocks.IRON_ORE
                || block == net.minecraft.world.level.block.Blocks.DEEPSLATE_IRON_ORE
                || block == net.minecraft.world.level.block.Blocks.COPPER_ORE
                || block == net.minecraft.world.level.block.Blocks.DEEPSLATE_COPPER_ORE
                || block == net.minecraft.world.level.block.Blocks.GOLD_ORE
                || block == net.minecraft.world.level.block.Blocks.DEEPSLATE_GOLD_ORE
                || block == net.minecraft.world.level.block.Blocks.NETHER_GOLD_ORE
                || block == net.minecraft.world.level.block.Blocks.REDSTONE_ORE
                || block == net.minecraft.world.level.block.Blocks.DEEPSLATE_REDSTONE_ORE
                || block == net.minecraft.world.level.block.Blocks.LAPIS_ORE
                || block == net.minecraft.world.level.block.Blocks.DEEPSLATE_LAPIS_ORE
                || block == net.minecraft.world.level.block.Blocks.DIAMOND_ORE
                || block == net.minecraft.world.level.block.Blocks.DEEPSLATE_DIAMOND_ORE
                || block == net.minecraft.world.level.block.Blocks.EMERALD_ORE
                || block == net.minecraft.world.level.block.Blocks.DEEPSLATE_EMERALD_ORE
                || block == net.minecraft.world.level.block.Blocks.NETHER_QUARTZ_ORE
                || block == net.minecraft.world.level.block.Blocks.ANCIENT_DEBRIS;
    }

    private int getOreXp(net.minecraft.world.level.block.Block block) {
        if (block == net.minecraft.world.level.block.Blocks.COAL_ORE
                || block == net.minecraft.world.level.block.Blocks.DEEPSLATE_COAL_ORE) return 1;
        if (block == net.minecraft.world.level.block.Blocks.DIAMOND_ORE
                || block == net.minecraft.world.level.block.Blocks.DEEPSLATE_DIAMOND_ORE
                || block == net.minecraft.world.level.block.Blocks.EMERALD_ORE
                || block == net.minecraft.world.level.block.Blocks.DEEPSLATE_EMERALD_ORE) return 7;
        if (block == net.minecraft.world.level.block.Blocks.LAPIS_ORE
                || block == net.minecraft.world.level.block.Blocks.DEEPSLATE_LAPIS_ORE
                || block == net.minecraft.world.level.block.Blocks.NETHER_QUARTZ_ORE) return 5;
        if (block == net.minecraft.world.level.block.Blocks.REDSTONE_ORE
                || block == net.minecraft.world.level.block.Blocks.DEEPSLATE_REDSTONE_ORE) return 5;
        return 3; // ferro, cobre, ouro, etc
    }
}