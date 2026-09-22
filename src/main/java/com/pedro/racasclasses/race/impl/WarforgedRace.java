package com.pedro.racasclasses.race.impl;

import com.pedro.racasclasses.race.Race;
import com.pedro.racasclasses.race.RacialWeakness;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

public class WarforgedRace implements Race {
    private static final Map<UUID, Long> ABILITY_COOLDOWNS = new HashMap<>();

    @Override public String getId() { return "warforged"; }
    @Override public String getDisplayName() { return "Warforged"; }
    @Override public double getMaxHealth() { return 24.0; }
    @Override public double getMovementSpeed() { return 0.09; }
    @Override public double getScale() { return 1.10; }
    @Override public double getArmor() { return 2.0; }

    // ===== Corpo construto =====
    @Override
    public void onPlayerTick(ServerPlayer player) {
        player.getFoodData().setFoodLevel(20);
        player.getFoodData().setSaturation(5.0f);
        if (player.tickCount % 100 == 0) {
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 120, 0, false, false, false));
        }
    }

    @Override public boolean canUseAbility() { return true; }

    @Override
    public void executeAbility(ServerPlayer player) {
        long tick = player.serverLevel().getServer().getTickCount();
        Long readyAt = ABILITY_COOLDOWNS.get(player.getUUID());
        if (readyAt != null && tick < readyAt) {
            player.sendSystemMessage(Component.literal(String.format("§cIntegrated Protection em recarga: §f%.1fs", (readyAt - tick) / 20.0)));
            return;
        }
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 160, 1));
        ABILITY_COOLDOWNS.put(player.getUUID(), tick + 1200);
        player.sendSystemMessage(Component.literal("§aIntegrated Protection ativado!"));
    }

    // ===== Fraqueza: +30% dano de raio =====
    @Override
    public void onPlayerHurt(ServerPlayer player, net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent event) {
        RacialWeakness.applyLightning(event, 1.3f);
    }
}
