package com.pedro.racasclasses.race.impl;

import com.pedro.racasclasses.race.Race;
import com.pedro.racasclasses.race.RacialWeakness;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TortleRace implements Race {

    // ===== Shell Defense (H) =====
    private static final Map<UUID, Long> ABILITY_COOLDOWNS = new HashMap<>();
    private static final int ABILITY_COOLDOWN_TICKS = 600;  // 30s
    private static final int ABILITY_DURATION_TICKS = 200;  // 10s

    @Override
    public String getId() { return "tortle"; }

    @Override
    public String getDisplayName() { return "Tortle"; }

    @Override
    public double getMaxHealth() { return 26.0; }

    @Override
    public double getMovementSpeed() { return 0.06; }

    @Override
    public double getScale() { return 1.10; }

    @Override
    public double getSwimSpeedBonus() { return 0.67; }

    // ===== 60% de redução de dano (armadura natural) =====
    // Não uso o atributo ARMOR (que é 0-20) porque 60% é muita coisa.
    // Uso -60% de dano recebido direto no onPlayerHurt.

    @Override
    public void onPlayerHurt(ServerPlayer player, LivingIncomingDamageEvent event) {
        // Resistência natural (-60%)
        event.setAmount(event.getAmount() * 0.40f);

        // Fraqueza: +30% dano de fogo
        RacialWeakness.applyFire(event, 1.3f);
    }

    // ===== Shell Defense (H) =====

    @Override
    public boolean canUseAbility() { return true; }

    @Override
    public void executeAbility(ServerPlayer player) {
        long currentTick = player.serverLevel().getServer().getTickCount();
        Long readyAt = ABILITY_COOLDOWNS.get(player.getUUID());

        if (readyAt != null && currentTick < readyAt) {
            double seconds = (readyAt - currentTick) / 20.0;
            player.sendSystemMessage(Component.literal(
                    String.format("§cShell Defense em recarga: §f%.1fs", seconds)));
            return;
        }

        player.addEffect(new MobEffectInstance(
                MobEffects.DAMAGE_RESISTANCE, ABILITY_DURATION_TICKS, 2, false, false, true));

        ABILITY_COOLDOWNS.put(player.getUUID(), currentTick + ABILITY_COOLDOWN_TICKS);
        player.sendSystemMessage(Component.literal("§aShell Defense ativado!"));
    }

    // ===== Respira na água infinitamente =====

    @Override
    public void onPlayerTick(ServerPlayer player) {
        if (player.tickCount % 20 != 0) return;

        if (player.isInWater() || player.isUnderWater()) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.WATER_BREATHING, 300, 0, false, false, false));
        }
    }

    // ===== Bloqueia armadura em todos os slots =====

    public void onEquipmentChange(ServerPlayer player, LivingEquipmentChangeEvent event) {
        ItemStack to = event.getTo();

        // Se é armadura, bloqueia
        if (to.getItem() instanceof ArmorItem) {
            // Devolve pro inventário no próximo tick
            player.server.execute(() -> {
                ItemStack tentouEquipar = player.getItemBySlot(event.getSlot());
                if (!tentouEquipar.isEmpty()) {
                    player.setItemSlot(event.getSlot(), ItemStack.EMPTY);
                    if (!player.getInventory().add(tentouEquipar.copy())) {
                        player.drop(tentouEquipar.copy(), false);
                    }
                    player.sendSystemMessage(Component.literal("§cTortle não pode usar armadura!"));
                }
            });
        }
    }
}