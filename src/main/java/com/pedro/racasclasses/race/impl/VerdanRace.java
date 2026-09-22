package com.pedro.racasclasses.race.impl;

import com.pedro.racasclasses.race.Race;
import com.pedro.racasclasses.race.RacialWeakness;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VerdanRace implements Race {

    // ===== Black Blood Healing =====
    private static final Map<UUID, Long> HEAL_COOLDOWNS = new HashMap<>();
    private static final int HEAL_COOLDOWN_TICKS = 600;  // 30s

    @Override
    public String getId() { return "verdan"; }

    @Override
    public String getDisplayName() { return "Verdan"; }

    @Override
    public double getMaxHealth() { return 22.0; }

    // ===== Telepathic Insight: -20% dano mágico =====

    @Override
    public void onPlayerHurt(ServerPlayer player, LivingIncomingDamageEvent event) {
        var source = event.getSource();

        // Resistência mágica (-20%)
        if (source.is(DamageTypes.MAGIC)
                || source.is(DamageTypes.INDIRECT_MAGIC)
                || source.is(DamageTypes.WITHER)) {
            event.setAmount(event.getAmount() * 0.8f);
        }

        // Fraqueza: +30% dano de magia (override da resistência)
        // Isso cria uma interação onde a fraqueza aumenta o dano, mas a resistência reduz
        // O resultado líquido é: dano * 0.8 * 1.3 = dano * 1.04 (4% de aumento)
        RacialWeakness.applyMagic(event, 1.3f);

        // Black Blood Healing: ao tomar dano, cura 1 ❤️
        long currentTick = player.serverLevel().getServer().getTickCount();
        Long readyAt = HEAL_COOLDOWNS.get(player.getUUID());

        if (readyAt == null || currentTick >= readyAt) {
            float newHealth = Math.min(player.getHealth() + 2.0f, player.getMaxHealth());
            player.setHealth(newHealth);
            HEAL_COOLDOWNS.put(player.getUUID(), currentTick + HEAL_COOLDOWN_TICKS);
        }
    }

    // ===== Regeneração lenta quando ≤3 ❤️ =====

    @Override
    public void onPlayerTick(ServerPlayer player) {
        if (player.tickCount % 20 != 0) return;

        if (player.getHealth() <= 6.0f) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.REGENERATION, 60, 0, false, false, false));
        }
    }
}