package com.pedro.racasclasses.race.impl;

import com.pedro.racasclasses.race.Race;
import com.pedro.racasclasses.race.RacialWeakness;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FirbolgRace implements Race {

    private static final Map<UUID, Long> ABILITY_COOLDOWNS = new HashMap<>();
    private static final int ABILITY_COOLDOWN_TICKS = 600;  // 30s
    private static final int ABILITY_DURATION_TICKS = 100;  // 5s
    private static final int SEARCH_RADIUS = 8;

    @Override
    public String getId() { return "firbolg"; }

    @Override
    public String getDisplayName() { return "Firbolg"; }

    @Override
    public double getMaxHealth() { return 22.0; }

    @Override
    public double getScale() { return 1.20; }

    @Override
    public boolean hasNightVision() { return true; }

    // ===== Speed I + Regeneration I perto de folhagem =====

    @Override
    public void onPlayerTick(ServerPlayer player) {
        if (player.tickCount % 20 != 0) return;

        if (hasNearbyLeaves(player.serverLevel(), player.blockPosition())) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, 0, false, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 0, false, false, false));
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
                        if (count >= 4) return true;
                    }
                }
            }
        }
        return false;
    }

    // ===== Hidden Step (H) =====

    @Override
    public boolean canUseAbility() { return true; }

    @Override
    public void executeAbility(ServerPlayer player) {
        long currentTick = player.serverLevel().getServer().getTickCount();
        Long readyAt = ABILITY_COOLDOWNS.get(player.getUUID());

        if (readyAt != null && currentTick < readyAt) {
            double seconds = (readyAt - currentTick) / 20.0;
            player.sendSystemMessage(Component.literal(
                    String.format("§cHidden Step em recarga: §f%.1fs", seconds)));
            return;
        }

        player.addEffect(new MobEffectInstance(
                MobEffects.INVISIBILITY, ABILITY_DURATION_TICKS, 0, false, false, true));

        ABILITY_COOLDOWNS.put(player.getUUID(), currentTick + ABILITY_COOLDOWN_TICKS);
        player.sendSystemMessage(Component.literal("§aHidden Step ativado!"));
    }

    // ===== Domar qualquer animal com mão vazia =====

    public void onEntityInteract(ServerPlayer player, PlayerInteractEvent.EntityInteract event) {
        // Só com mão vazia
        if (!player.getMainHandItem().isEmpty()) return;

        Entity target = event.getTarget();
        if (!(target instanceof TamableAnimal tamable)) return;

        // Se já tá domado, ignora
        if (tamable.isTame()) return;

        // Doma na hora
        tamable.tame(player);
        tamable.setOrderedToSit(true);
        player.sendSystemMessage(Component.literal("§aAnimal domado!"));

        event.setCanceled(true);
    }

    // ===== Fraqueza: +50% dano de fogo =====
    @Override
    public void onPlayerHurt(ServerPlayer player, LivingIncomingDamageEvent event) {
        RacialWeakness.applyFire(event, 1.5f);
    }
}