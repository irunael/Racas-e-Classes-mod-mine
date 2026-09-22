package com.pedro.racasclasses.race.impl;

import com.pedro.racasclasses.race.Race;
import com.pedro.racasclasses.race.RacialWeakness;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HarengonRace implements Race {

    // ===== Cooldown do Super Jump =====
    private static final Map<UUID, Long> SUPER_JUMP_COOLDOWNS = new HashMap<>();
    private static final int SUPER_JUMP_COOLDOWN_TICKS = 160; // 8s

    @Override
    public String getId() { return "harengon"; }

    @Override
    public String getDisplayName() { return "Harengon"; }

    @Override
    public double getMaxHealth() { return 20.0; }

    @Override
    public double getMovementSpeed() { return 0.11; }

    @Override
    public double getScale() { return 0.85; }

    @Override
    public boolean hasNightVision() { return true; }

    // ===== Super Jump (tecla R) =====

    @Override
    public boolean canSuperJump() { return true; }

    @Override
    public void executeSuperJump(ServerPlayer player) {
        // Só pula no chão (igual Aarakocra)
        if (!player.onGround()) return;

        long currentTick = player.serverLevel().getServer().getTickCount();
        Long readyAt = SUPER_JUMP_COOLDOWNS.get(player.getUUID());

        // Tá em cooldown?
        if (readyAt != null && currentTick < readyAt) {
            double seconds = (readyAt - currentTick) / 20.0;
            player.sendSystemMessage(Component.literal(
                    String.format("§cSuper pulo em recarga: §f%.1fs", seconds)));
            return;
        }

        // Aplica o pulo (mesmo da Aarakocra: motionY 1.0)
        player.setDeltaMovement(0, 1.0, 0);
        player.hurtMarked = true;

        // Marca cooldown
        SUPER_JUMP_COOLDOWNS.put(player.getUUID(), currentTick + SUPER_JUMP_COOLDOWN_TICKS);
    }

    // ===== Imunidade a queda (mesma do Goliath) =====

    public void onFall(ServerPlayer player, LivingFallEvent event) {
        float distance = event.getDistance();

        // Se cair até 5 blocos, cancela
        if (distance <= 5.0f) {
            event.setCanceled(true);
        } else {
            // Reduz a distância em 5 blocos
            event.setDistance(distance - 5.0f);
        }
    }

    // ===== Sorte: +30% chance de drop extra =====

    @Override
    public void onMobDrops(ServerPlayer player, LivingEntity killed, LivingDropsEvent event) {
        if (player.getRandom().nextFloat() >= 0.30f) return;

        var drops = event.getDrops();
        if (drops.isEmpty()) return;

        ArrayList<ItemEntity> dropList = new ArrayList<>(drops);
        ItemEntity original = dropList.get(player.getRandom().nextInt(dropList.size()));

        ItemStack copy = original.getItem().copy();
        copy.setCount(1);

        ServerLevel level = (ServerLevel) event.getEntity().level();
        ItemEntity extra = new ItemEntity(
                level,
                event.getEntity().getX(),
                event.getEntity().getY() + 0.5,
                event.getEntity().getZ(),
                copy
        );
        drops.add(extra);
    }

    // ===== Fraqueza: +50% dano de fogo =====
    @Override
    public void onPlayerHurt(ServerPlayer player, LivingIncomingDamageEvent event) {
        RacialWeakness.applyFire(event, 1.5f);
    }
}