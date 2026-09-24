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
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.List;

public class ElfRace implements Race {

    private static final int DARK_LIGHT_THRESHOLD = 7;
    private static final double GLOWING_RADIUS = 16.0;
    private static final int SEARCH_RADIUS = 16;
    private static final int WOOD_MIN_LEAVES = 8;

    @Override
    public String getId() { return "elf"; }

    @Override
    public String getDisplayName() { return "Elfo"; }

    @Override public int getRacialDexterity() { return 2; }
    @Override public int getFreeAttributePoints() { return 0; }

    @Override
    public boolean hasNightVision() { return true; }

    // ===== Sub-raça =====

    @Override
    public boolean hasSubrace() { return true; }

    @Override
    public String[] getSubraceIds() { return new String[]{"wood", "drow"}; }

    @Override
    public void onSubraceChosen(ServerPlayer player, String subraceId) {
        PlayerRaceData data = player.getData(ModAttachments.PLAYER_RACE);
        data.setElfSubrace(subraceId);

        // Base já deu +2 DEX; a linhagem soma 1 ponto na trilha correspondente.
        var attr = player.getData(ModAttachments.PLAYER_ATTRIBUTES);
        switch (subraceId) {
            case "wood" -> attr.addWisdom(1);
            case "drow" -> attr.addLuck(1);
        }
    }

    @Override
    public String getSubraceDisplayName(ServerPlayer player) {
        PlayerRaceData data = player.getData(ModAttachments.PLAYER_RACE);
        String id = data.getElfSubrace();
        return switch (id) {
            case "wood" -> "Elfo da Floresta";
            case "drow" -> "Elfo Negro";
            default -> id;
        };
    }

    // ===== Armadura +1.5 (Drow) =====

    @Override
    public double getArmor() {
        return 0.0; // base
    }

    @Override
    public double getSubraceArmorBonus(ServerPlayer player) {
        PlayerRaceData data = player.getData(ModAttachments.PLAYER_RACE);
        if (data.getElfSubrace().equals("drow")) return 1.5;
        return 0.0;
    }

    // ===== Bônus de arco (+25%) =====

    @Override
    public void onRangedDamage(ServerPlayer player, LivingIncomingDamageEvent event) {
        var directEntity = event.getSource().getDirectEntity();
        if (!(directEntity instanceof net.minecraft.world.entity.projectile.AbstractArrow)) return;
        event.setAmount(event.getAmount() * 1.25f);
    }

    // Wood: +50% fogo. Drow: +50% de qualquer dano no sol.
    @Override
    public void onPlayerHurt(ServerPlayer player, LivingIncomingDamageEvent event) {
        PlayerRaceData data = player.getData(ModAttachments.PLAYER_RACE);
        if (data.getElfSubrace().equals("wood")) {
            RacialWeakness.applyFire(event, 1.5f);
        } else if (data.getElfSubrace().equals("drow") && isInSunlight(player.serverLevel(), player)) {
            RacialWeakness.scale(event, 1.5f);
        }
    }

    // ===== Tick (buffs/debuffs por sub-raça) =====

    @Override
    public void onPlayerTick(ServerPlayer player) {
        if (player.tickCount % 20 != 0) return;

        PlayerRaceData data = player.getData(ModAttachments.PLAYER_RACE);

        if (data.getElfSubrace().equals("wood")) {
            handleWoodElf(player);
        } else if (data.getElfSubrace().equals("drow")) {
            handleDrow(player);
        }
    }

    // ===== Wood Elf =====

    private void handleWoodElf(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        boolean hasLeaves = hasNearbyLeaves(level, player.blockPosition());
        int light = getLightLevel(level, player.blockPosition());
        boolean isDark = light < DARK_LIGHT_THRESHOLD;

        if (hasLeaves) {
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 0, false, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, 1, false, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 40, 0, false, false, false));
            player.removeEffect(MobEffects.WEAKNESS);
            player.removeEffect(MobEffects.DARKNESS);
        } else if (isDark) {
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 0, false, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 200, 0, false, false, false));
        } else {
            player.removeEffect(MobEffects.WEAKNESS);
            player.removeEffect(MobEffects.DARKNESS);
        }
    }

    private boolean hasNearbyLeaves(ServerLevel level, BlockPos center) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int count = 0;
        for (int x = -SEARCH_RADIUS; x <= SEARCH_RADIUS; x += 2) {
            for (int z = -SEARCH_RADIUS; z <= SEARCH_RADIUS; z += 2) {
                for (int y = -8; y <= 16; y += 2) {
                    pos.set(center.getX() + x, center.getY() + y, center.getZ() + z);
                    if (pos.getY() < level.getMinBuildHeight() || pos.getY() >= level.getMaxBuildHeight()) continue;
                    BlockState state = level.getBlockState(pos);
                    if (state.is(BlockTags.LEAVES)) {
                        count++;
                        if (count >= WOOD_MIN_LEAVES) return true;
                    }
                }
            }
        }
        return false;
    }

    // ===== Drow =====

    private void handleDrow(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        int light = getLightLevel(level, player.blockPosition());
        boolean isDark = light < DARK_LIGHT_THRESHOLD;
        boolean inSun = isInSunlight(level, player);

        if (isDark) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, 1, false, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 40, 1, false, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 0, false, false, false));
            applyGlowing(player, level);
        } else {
            player.removeEffect(MobEffects.MOVEMENT_SPEED);
            player.removeEffect(MobEffects.DAMAGE_BOOST);
            player.removeEffect(MobEffects.DAMAGE_RESISTANCE);
        }

        if (inSun) {
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 0, false, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 200, 0, false, false, false));
        }
    }

    // ===== Helpers =====

    private void applyGlowing(ServerPlayer player, ServerLevel level) {
        AABB area = player.getBoundingBox().inflate(GLOWING_RADIUS);
        List<Monster> mobs = level.getEntitiesOfClass(Monster.class, area, m -> m.isAlive() && !m.isRemoved());
        for (Monster m : mobs) {
            m.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0, false, false, false));
        }
    }

    private int getLightLevel(ServerLevel level, BlockPos pos) {
        int block = level.getBrightness(LightLayer.BLOCK, pos);
        int sky = level.getBrightness(LightLayer.SKY, pos);
        return Math.max(block, sky);
    }

    private boolean isInSunlight(ServerLevel level, ServerPlayer player) {
        if (!level.isDay()) return false;
        int skyLight = level.getBrightness(LightLayer.SKY, player.blockPosition());
        return skyLight >= 15;
    }
}