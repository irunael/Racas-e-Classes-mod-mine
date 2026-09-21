package com.pedro.racasclasses.race.impl;

import com.pedro.racasclasses.race.Race;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GoblinRace implements Race {

    // ===== Nimble Escape (tecla H) =====
    private static final Map<UUID, Long> ABILITY_COOLDOWNS = new HashMap<>();
    private static final int ABILITY_COOLDOWN_TICKS = 600;  // 30s
    private static final int ABILITY_DURATION_TICKS = 200;  // 10s

    private static final net.minecraft.resources.ResourceLocation ID_SNEAK_SPEED =
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("racasclasses", "goblin_sneak_speed");

    @Override
    public String getId() { return "goblin"; }

    @Override
    public String getDisplayName() { return "Goblin"; }

    @Override
    public double getMaxHealth() { return 18.0; }

    @Override
    public double getMovementSpeed() { return 0.12; }

    @Override
    public double getScale() { return 0.65; }

    @Override
    public boolean hasNightVision() { return true; }

    // ===== Nimble Escape =====

    @Override
    public boolean canUseAbility() { return true; }

    @Override
    public void executeAbility(ServerPlayer player) {
        long currentTick = player.serverLevel().getServer().getTickCount();
        Long readyAt = ABILITY_COOLDOWNS.get(player.getUUID());

        if (readyAt != null && currentTick < readyAt) {
            double seconds = (readyAt - currentTick) / 20.0;
            player.sendSystemMessage(Component.literal(
                    String.format("§cNimble Escape em recarga: §f%.1fs", seconds)));
            return;
        }

        player.addEffect(new MobEffectInstance(
                MobEffects.INVISIBILITY, ABILITY_DURATION_TICKS, 0, false, false, true));

        ABILITY_COOLDOWNS.put(player.getUUID(), currentTick + ABILITY_COOLDOWN_TICKS);
        player.sendSystemMessage(Component.literal("§aNimble Escape ativado!"));
    }

    // ===== Velocidade ao agachar =====

    @Override
    public void onPlayerTick(ServerPlayer player) {
        if (player.tickCount % 5 != 0) return;

        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) return;

        // Remove modificador antigo
        if (speed.getModifier(ID_SNEAK_SPEED) != null) {
            speed.removeModifier(ID_SNEAK_SPEED);
        }

        // Se agachado, +0.05
        if (player.isShiftKeyDown()) {
            speed.addTransientModifier(new AttributeModifier(
                    ID_SNEAK_SPEED, 0.05, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    @Override
    public void onRaceExit(ServerPlayer player) {
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null && speed.getModifier(ID_SNEAK_SPEED) != null) {
            speed.removeModifier(ID_SNEAK_SPEED);
        }
    }

    // ===== Resistências e fraquezas =====

    @Override
    public void onPlayerHurt(ServerPlayer player, LivingIncomingDamageEvent event) {
        DamageSource source = event.getSource();

        // --- Resistência: Magia (-30%) ---
        if (source.is(DamageTypes.MAGIC)
                || source.is(DamageTypes.INDIRECT_MAGIC)
                || source.is(DamageTypes.WITHER)) {
            event.setAmount(event.getAmount() * 0.7f);
            return;
        }

        // --- Fraqueza: Armas físicas (+20%) ---
        boolean isWeapon =
                source.is(DamageTypes.PLAYER_ATTACK)
                        || source.is(DamageTypes.MOB_ATTACK)
                        || source.is(DamageTypes.MOB_ATTACK_NO_AGGRO)
                        || source.is(DamageTypes.ARROW)
                        || source.is(DamageTypes.TRIDENT)
                        || source.is(DamageTypes.MOB_PROJECTILE)
                        || source.is(DamageTypes.THROWN);

        if (isWeapon) {
            event.setAmount(event.getAmount() * 1.2f);
        }
    }
}
