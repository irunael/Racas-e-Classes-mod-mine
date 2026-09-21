package com.pedro.racasclasses.race.impl;

import com.pedro.racasclasses.race.Race;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.SwordItem;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HobgoblinRace implements Race {

    private static final Map<UUID, Long> ABILITY_COOLDOWNS = new HashMap<>();
    private static final int ABILITY_COOLDOWN_TICKS = 600;  // 30s
    private static final int ABILITY_DURATION_TICKS = 200;  // 10s

    @Override
    public String getId() { return "hobgoblin"; }

    @Override
    public String getDisplayName() { return "Hobgoblin"; }

    @Override
    public double getMaxHealth() { return 22.0; }

    @Override
    public double getAttackDamage() { return 1.1; }

    @Override
    public boolean hasNightVision() { return true; }

    // ===== +30% dano com espadas e machados =====

    @Override
    public void onAttackEntity(ServerPlayer player, LivingIncomingDamageEvent event) {
        var item = player.getMainHandItem().getItem();

        if (item instanceof SwordItem || item instanceof AxeItem) {
            event.setAmount(event.getAmount() * 1.30f);
        }
    }

    // ===== Saving Face (tecla H) =====

    @Override
    public boolean canUseAbility() { return true; }

    @Override
    public void executeAbility(ServerPlayer player) {
        long currentTick = player.serverLevel().getServer().getTickCount();
        Long readyAt = ABILITY_COOLDOWNS.get(player.getUUID());

        if (readyAt != null && currentTick < readyAt) {
            double seconds = (readyAt - currentTick) / 20.0;
            player.sendSystemMessage(Component.literal(
                    String.format("§cSaving Face em recarga: §f%.1fs", seconds)));
            return;
        }

        // Strength I por 10s
        player.addEffect(new MobEffectInstance(
                MobEffects.DAMAGE_BOOST, ABILITY_DURATION_TICKS, 0, false, false, true));

        ABILITY_COOLDOWNS.put(player.getUUID(), currentTick + ABILITY_COOLDOWN_TICKS);
        player.sendSystemMessage(Component.literal("§aSaving Face ativado!"));
    }
}