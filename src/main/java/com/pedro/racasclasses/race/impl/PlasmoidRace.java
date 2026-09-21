package com.pedro.racasclasses.race.impl;

import com.pedro.racasclasses.race.Race;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlasmoidRace implements Race {

    @Override
    public String getId() { return "plasmoid"; }

    @Override
    public String getDisplayName() { return "Plasmoid"; }

    @Override
    public double getMaxHealth() { return 22.0; }

    @Override
    public double getAttackDamage() { return 1.0; }

    @Override
    public double getMovementSpeed() { return 0.10; }

    @Override
    public double getScale() { return 0.70; }

    @Override
    public boolean hasNightVision() { return true; }

    // ===== Passivas =====

    @Override
    public void onPlayerTick(ServerPlayer player) {
        // Respira na água permanente
        if (!player.hasEffect(MobEffects.WATER_BREATHING)) {
            player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 400, 0, false, false));
        }
    }

    @Override
    public void onPlayerHurt(ServerPlayer player, LivingIncomingDamageEvent event) {
        // Imune a poison
        DamageSource source = event.getSource();
        String type = source.typeHolder().getRegisteredName();

        if (type.contains("poison")) {
            event.setCanceled(true);
        }
    }

    // ===== Habilidade H: Amorphous =====

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
            player.sendSystemMessage(Component.literal(String.format("§cAmorphous em recarga: §f%.1fs", seconds)));
            return;
        }

        // Invisibilidade + Resistance I por 3s
        player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 60, 0));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 0));

        player.sendSystemMessage(Component.literal("§aAmorphous!"));
        ABILITY_COOLDOWNS.put(player.getUUID(), currentTick + COOLDOWN_TICKS);
    }
}
