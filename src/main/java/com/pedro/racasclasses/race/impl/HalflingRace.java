package com.pedro.racasclasses.race.impl;

import com.pedro.racasclasses.RacasClasses;
import com.pedro.racasclasses.capability.ModAttachments;
import com.pedro.racasclasses.capability.PlayerRaceData;
import com.pedro.racasclasses.race.Race;
import com.pedro.racasclasses.race.RacialWeakness;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HalflingRace implements Race {

    private static final float LUCKY_CHANCE = 0.10f;
    private static final float BACKSTAB_MULTIPLIER = 1.20f;

    // Cooldown da invisibilidade do Lightfoot (H)
    private static final Map<UUID, Long> ABILITY_COOLDOWNS = new HashMap<>();
    private static final int ABILITY_COOLDOWN_TICKS = 600; // 30s
    private static final int ABILITY_DURATION_TICKS = 100; // 5s

    @Override
    public String getId() { return "halfling"; }

    @Override
    public String getDisplayName() { return "Halfling"; }

    @Override public int getRacialDexterity() { return 2; }
    @Override public int getFreeAttributePoints() { return 0; }

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

        var attr = player.getData(ModAttachments.PLAYER_ATTRIBUTES);
        switch (subraceId) {
            case "lightfoot" -> attr.addLuck(1);
            case "stout" -> attr.addConstitution(1);
        }
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

    // ===== Brave + fome do Lightfoot =====

    @Override
    public void onPlayerTick(ServerPlayer player) {
        if (player.tickCount % 20 != 0) return;

        // Brave: imunidade a Weakness + Slowness
        player.removeEffect(MobEffects.WEAKNESS);
        player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);

        // Lightfoot: fome 15% mais rápida
        PlayerRaceData data = player.getData(ModAttachments.PLAYER_RACE);
        if (data.getHalflingSubrace().equals("lightfoot")) {
            RacialWeakness.fasterHunger(player, 0.15f);
        }
    }

    // ===== Habilidade H: Invisibilidade (Lightfoot) =====

    @Override
    public boolean canUseAbility() {
        return true;
    }

    @Override
    public void executeAbility(ServerPlayer player) {
        PlayerRaceData data = player.getData(ModAttachments.PLAYER_RACE);
        if (!data.getHalflingSubrace().equals("lightfoot")) {
            player.sendSystemMessage(Component.literal("§cApenas o Halfling Pé Leve tem essa habilidade."));
            return;
        }

        long currentTick = player.serverLevel().getServer().getTickCount();
        Long readyAt = ABILITY_COOLDOWNS.get(player.getUUID());

        if (readyAt != null && currentTick < readyAt) {
            double seconds = (readyAt - currentTick) / 20.0;
            player.sendSystemMessage(Component.literal(
                    String.format("§cInvisibilidade em recarga: §f%.1fs", seconds)));
            return;
        }

        // Aplica invisibilidade por 5s
        player.addEffect(new MobEffectInstance(
                MobEffects.INVISIBILITY,
                ABILITY_DURATION_TICKS,
                0,
                false, false, false
        ));

        player.sendSystemMessage(Component.literal("§aInvisibilidade ativada!"));
        ABILITY_COOLDOWNS.put(player.getUUID(), currentTick + ABILITY_COOLDOWN_TICKS);
    }

    // ===== Lucky: 10% chance de drop extra =====

    @Override
    public void onBlockBreak(ServerPlayer player, BlockEvent.BreakEvent event) {
        if (player.getRandom().nextFloat() >= LUCKY_CHANCE) return;

        var level = player.serverLevel();
        var pos = event.getPos();
        var state = event.getState();
        var blockEntity = level.getBlockEntity(pos);

        // Pega os drops corretos
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

        if (!data.getHalflingSubrace().equals("stout")) return;

        boolean isPhysical =
                source.is(net.minecraft.world.damagesource.DamageTypes.PLAYER_ATTACK)
                        || source.is(net.minecraft.world.damagesource.DamageTypes.MOB_ATTACK)
                        || source.is(net.minecraft.world.damagesource.DamageTypes.ARROW)
                        || source.is(net.minecraft.world.damagesource.DamageTypes.TRIDENT);

        if (isPhysical) {
            event.setAmount(event.getAmount() * 0.8f); // 20% redução
        }

        // Stout: +25% dano de veneno
        RacialWeakness.applyPoisonMagic(player, event, 1.25f);
    }
}