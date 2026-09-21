package com.pedro.racasclasses.race.impl;

import com.pedro.racasclasses.RacasClasses;
import com.pedro.racasclasses.capability.ModAttachments;
import com.pedro.racasclasses.capability.PlayerRaceData;
import com.pedro.racasclasses.race.Race;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

public class HalflingRace implements Race {

    private static final float LUCKY_CHANCE = 0.10f;
    private static final float BACKSTAB_MULTIPLIER = 1.20f;
    private static final float STOUT_RESISTANCE = 0.30f;

    @Override
    public String getId() { return "halfling"; }

    @Override
    public String getDisplayName() { return "Halfling"; }

    @Override
    public double getMovementSpeed() { return 0.09; }

    @Override
    public double getScale() { return 0.55; }

    @Override
    public boolean hasSubrace() { return true; }

    @Override
    public String[] getSubraceIds() { return new String[]{"lightfoot", "stout"}; }

    @Override
    public void onSubraceChosen(ServerPlayer player, String subraceId) {
        PlayerRaceData data = player.getData(ModAttachments.PLAYER_RACE);
        data.setHalflingSubrace(subraceId);
    }

    @Override
    public String getSubraceDisplayName(ServerPlayer player) {
        PlayerRaceData data = player.getData(ModAttachments.PLAYER_RACE);
        return switch (data.getHalflingSubrace()) {
            case "lightfoot" -> "Pé Leve";
            case "stout" -> "Robusto";
            default -> data.getHalflingSubrace();
        };
    }

    // ===== Stout: +6 HP =====

    @Override
    public double getSubraceHealthBonus(ServerPlayer player) {
        PlayerRaceData data = player.getData(ModAttachments.PLAYER_RACE);
        if (data.getHalflingSubrace().equals("stout")) {
            return 6.0;
        }
        return 0.0;
    }

    // ===== Brave + Lightfoot invisibilidade =====

    @Override
    public void onPlayerTick(ServerPlayer player) {
        if (player.tickCount % 20 != 0) return;

        // Brave: imunidade a Weakness + Slowness
        player.removeEffect(MobEffects.WEAKNESS);
        player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);

        PlayerRaceData data = player.getData(ModAttachments.PLAYER_RACE);

        // Lightfoot: invisibilidade ao agachar
        if (data.getHalflingSubrace().equals("lightfoot")) {
            if (player.isShiftKeyDown()) {
                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 40, 0, false, false, false));
            }
        }
    }

    // ===== Lucky: 10% chance de drop extra =====

    @Override
    public void onBlockBreak(ServerPlayer player, BlockEvent.BreakEvent event) {
        if (player.getRandom().nextFloat() >= LUCKY_CHANCE) return;

        var level = player.serverLevel();
        var pos = event.getPos();
        var state = event.getState();
        var blockEntity = level.getBlockEntity(pos);

        // Pega os drops corretos (Raw Iron, não Iron Ore)
        var drops = Block.getDrops(state, level, pos, blockEntity);

        if (drops.isEmpty()) return;

        for (var stack : drops) {
            Block.popResource(level, pos, stack.copy());
        }

        RacasClasses.LOGGER.info("[HALFLING] Lucky! Drop extra de {}", state.getBlock());
    }

    // ===== Backstab (Lightfoot) =====

    @Override
    public void onAttackEntity(ServerPlayer player, LivingIncomingDamageEvent event) {
        PlayerRaceData data = player.getData(ModAttachments.PLAYER_RACE);
        if (!data.getHalflingSubrace().equals("lightfoot")) return;

        if (!(event.getEntity() instanceof LivingEntity target)) return;

        Vec3 targetLook = target.getLookAngle().normalize();
        Vec3 targetToPlayer = player.position().subtract(target.position()).normalize();
        double dot = targetLook.dot(targetToPlayer);

        if (dot < 0) {
            event.setAmount(event.getAmount() * BACKSTAB_MULTIPLIER);
            RacasClasses.LOGGER.info("[HALFLING] Backstab! Dano aumentado em 20%");
        }
    }

    // ===== Stout: 20% resistência a dano físico =====

    @Override
    public void onPlayerHurt(ServerPlayer player, LivingIncomingDamageEvent event) {
        PlayerRaceData data = player.getData(ModAttachments.PLAYER_RACE);
        var source = event.getSource();

        RacasClasses.LOGGER.info("[HALFLING-HURT] Subrace={}, damageType={}, amount={}, hp antes={}",
                data.getHalflingSubrace(),
                source.getMsgId(),
                event.getAmount(),
                player.getHealth());

        if (!data.getHalflingSubrace().equals("stout")) return;

        boolean isPhysical =
                source.is(net.minecraft.world.damagesource.DamageTypes.PLAYER_ATTACK)
                        || source.is(net.minecraft.world.damagesource.DamageTypes.MOB_ATTACK)
                        || source.is(net.minecraft.world.damagesource.DamageTypes.ARROW)
                        || source.is(net.minecraft.world.damagesource.DamageTypes.TRIDENT);

        RacasClasses.LOGGER.info("[HALFLING-STOUT] isPhysical={}, amount antes={}",
                isPhysical, event.getAmount());

        if (isPhysical) {
            float novoDano = event.getAmount() * 0.8f;
            event.setAmount(novoDano);
            RacasClasses.LOGGER.info("[HALFLING-STOUT] amount depois={}", novoDano);
        }
    }
}