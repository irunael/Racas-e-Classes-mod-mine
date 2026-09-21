package com.pedro.racasclasses.race.impl;

import com.pedro.racasclasses.race.Race;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RebornRace implements Race {

    @Override
    public String getId() { return "reborn"; }

    @Override
    public String getDisplayName() { return "Reborn"; }

    @Override
    public double getMaxHealth() { return 22.0; }

    @Override
    public double getAttackDamage() { return 1.0; }

    @Override
    public double getMovementSpeed() { return 0.10; }

    @Override
    public boolean hasNightVision() { return true; }

    // ===== Passiva: Respira na água =====

    @Override
    public void onPlayerTick(ServerPlayer player) {
        if (!player.hasEffect(MobEffects.WATER_BREATHING)) {
            player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 400, 0, false, false));
        }
    }

    // ===== Passiva: 50% resistência a poison =====

    @Override
    public void onPlayerHurt(ServerPlayer player, LivingIncomingDamageEvent event) {
        DamageSource source = event.getSource();
        String type = source.typeHolder().getRegisteredName();

        if (type.contains("poison")) {
            event.setAmount(event.getAmount() * 0.5f);
        }
    }

    // ===== Passiva: +10% XP =====

    @Override
    public void onXpDrop(ServerPlayer player, LivingExperienceDropEvent event) {
        int xp = event.getDroppedExperience();
        event.setDroppedExperience((int) (xp * 1.1));
    }

    // ===== Habilidade H: Deathly Resilience =====

    private static final Map<UUID, Long> ABILITY_COOLDOWNS = new HashMap<>();
    private static final int COOLDOWN_TICKS = 1200; // 60s

    @Override
    public boolean canUseAbility() { return true; }

    @Override
    public void executeAbility(ServerPlayer player) {
        long currentTick = player.serverLevel().getServer().getTickCount();
        Long readyAt = ABILITY_COOLDOWNS.get(player.getUUID());

        if (readyAt != null && currentTick < readyAt) {
            double seconds = (readyAt - currentTick) / 20.0;
            player.sendSystemMessage(Component.literal(String.format("§cDeathly Resilience em recarga: §f%.1fs", seconds)));
            return;
        }

        // Só ativa se HP < 6 (3 corações)
        if (player.getHealth() >= 6.0f) {
            player.sendSystemMessage(Component.literal("§cVocê precisa estar com pouca vida! (< 3 corações)"));
            return;
        }

        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 1)); // 5s, Resistance II
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1)); // 5s, Regeneration II

        player.sendSystemMessage(Component.literal("§aDeathly Resilience!"));
        ABILITY_COOLDOWNS.put(player.getUUID(), currentTick + COOLDOWN_TICKS);
    }
}
