package com.pedro.racasclasses.race.impl;

import com.pedro.racasclasses.race.Race;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SimicHybridRace implements Race {

    @Override
    public String getId() { return "simichybrid"; }

    @Override
    public String getDisplayName() { return "Simic Hybrid"; }

    @Override
    public double getMaxHealth() { return 22.0; }

    @Override
    public double getAttackDamage() { return 1.0; }

    @Override
    public double getMovementSpeed() { return 0.10; }

    @Override
    public boolean hasNightVision() { return true; }

    // ===== Sub-raça =====

    @Override
    public boolean hasSubrace() { return true; }

    @Override
    public String[] getSubraceIds() { 
        return new String[]{"manta", "climber", "grappling"}; 
    }

    @Override
    public String getSubraceDisplayName(ServerPlayer player) {
        String subrace = getSubrace(player);
        if (subrace == null) return "";
        switch (subrace) {
            case "manta": return "Manta Glide";
            case "climber": return "Climbing";
            case "grappling": return "Grappling";
            default: return "";
        }
    }

    private static final Map<UUID, String> SUBRACES = new HashMap<>();

    @Override
    public void onSubraceChosen(ServerPlayer player, String subraceId) {
        SUBRACES.put(player.getUUID(), subraceId);
    }

    private String getSubrace(ServerPlayer player) {
        return SUBRACES.get(player.getUUID());
    }

    // ===== Passivas por sub-raça =====

    @Override
    public void onPlayerTick(ServerPlayer player) {
        String subrace = getSubrace(player);
        if (subrace == null) return;

        switch (subrace) {
            case "manta":
                // Planador: Slow Falling permanente
                if (!player.hasEffect(MobEffects.SLOW_FALLING)) {
                    player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 400, 0, false, false));
                }
                // Move horizontal no ar (simplified: permite movimento no ar)
                break;

            case "climber":
                // Escalada: sobe paredes quando agachado encostado em bloco
                if (player.isCrouching() && player.horizontalCollision) {
                    player.setDeltaMovement(player.getDeltaMovement().x, 0.2, player.getDeltaMovement().z);
                }
                break;

            case "grappling":
                // Passiva aplicada em onAttackEntity
                break;
        }
    }

    @Override
    public void onAttackEntity(ServerPlayer player, LivingIncomingDamageEvent event) {
        String subrace = getSubrace(player);
        if (!"grappling".equals(subrace)) return;

        // Grappling: +2 dano desarmado
        if (!player.getMainHandItem().isEmpty()) return;
        event.setAmount(event.getAmount() + 2.0f);
    }

    // ===== Habilidade H: Adaptive Shield =====

    private static final Map<UUID, Long> ABILITY_COOLDOWNS = new HashMap<>();
    private static final int COOLDOWN_TICKS = 900; // 45s

    @Override
    public boolean canUseAbility() { return true; }

    @Override
    public void executeAbility(ServerPlayer player) {
        String subrace = getSubrace(player);
        if (subrace == null) {
            player.sendSystemMessage(Component.literal("§cEscolha uma sub-raça primeiro!"));
            return;
        }

        long currentTick = player.serverLevel().getServer().getTickCount();
        Long readyAt = ABILITY_COOLDOWNS.get(player.getUUID());

        if (readyAt != null && currentTick < readyAt) {
            double seconds = (readyAt - currentTick) / 20.0;
            player.sendSystemMessage(Component.literal(String.format("§cAdaptive Shield em recarga: §f%.1fs", seconds)));
            return;
        }

        // Resistance I por 10s
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 0));

        player.sendSystemMessage(Component.literal("§aAdaptive Shield!"));
        ABILITY_COOLDOWNS.put(player.getUUID(), currentTick + COOLDOWN_TICKS);
    }
}
