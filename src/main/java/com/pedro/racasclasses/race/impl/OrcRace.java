package com.pedro.racasclasses.race.impl;

import com.pedro.racasclasses.race.Race;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Stray;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class OrcRace implements Race {

    // ===== Adrenaline Rush (tecla H) =====
    private static final Map<UUID, Long> ABILITY_COOLDOWNS = new HashMap<>();
    private static final int ABILITY_COOLDOWN_TICKS = 900;  // 45s
    private static final int ABILITY_DURATION_TICKS = 200;  // 10s

    @Override
    public String getId() { return "orc"; }

    @Override
    public String getDisplayName() { return "Orc"; }

    @Override
    public double getMaxHealth() { return 24.0; }

    @Override
    public double getScale() { return 1.30; }

    @Override
    public boolean hasNightVision() { return true; }

    // ===== +10% dano corpo a corpo em mobs =====

    @Override
    public void onAttackEntity(ServerPlayer player, LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Mob)) return;
        event.setAmount(event.getAmount() * 1.10f);
    }

    // ===== Adrenaline Rush =====

    @Override
    public boolean canUseAbility() { return true; }

    @Override
    public void executeAbility(ServerPlayer player) {
        long currentTick = player.serverLevel().getServer().getTickCount();
        Long readyAt = ABILITY_COOLDOWNS.get(player.getUUID());

        if (readyAt != null && currentTick < readyAt) {
            double seconds = (readyAt - currentTick) / 20.0;
            player.sendSystemMessage(Component.literal(
                    String.format("§cAdrenaline Rush em recarga: §f%.1fs", seconds)));
            return;
        }

        // Cura 3 corações (6 HP)
        float newHealth = Math.min(player.getHealth() + 6.0f, player.getMaxHealth());
        player.setHealth(newHealth);

        // Speed I + Strength II por 10s
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, ABILITY_DURATION_TICKS, 0, false, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, ABILITY_DURATION_TICKS, 1, false, false, true));

        ABILITY_COOLDOWNS.put(player.getUUID(), currentTick + ABILITY_COOLDOWN_TICKS);
        player.sendSystemMessage(Component.literal("§aAdrenaline Rush ativado!"));
    }

    // ===== Resistências e fraquezas =====

    @Override
    public void onPlayerHurt(ServerPlayer player, LivingIncomingDamageEvent event) {
        DamageSource source = event.getSource();

        // --- Fraqueza: Fogo/Raio/Magia (+50%) ---
        boolean isElemental =
                source.is(DamageTypes.IN_FIRE)
                        || source.is(DamageTypes.ON_FIRE)
                        || source.is(DamageTypes.LAVA)
                        || source.is(DamageTypes.HOT_FLOOR)
                        || source.is(DamageTypes.LIGHTNING_BOLT)
                        || source.is(DamageTypes.MAGIC)
                        || source.is(DamageTypes.INDIRECT_MAGIC)
                        || source.is(DamageTypes.WITHER);

        if (isElemental) {
            event.setAmount(event.getAmount() * 1.5f);
            return;
        }

        // --- Resistência: Corpo a corpo (-15%) ---
        boolean isMelee =
                source.is(DamageTypes.PLAYER_ATTACK)
                        || source.is(DamageTypes.MOB_ATTACK)
                        || source.is(DamageTypes.MOB_ATTACK_NO_AGGRO);

        if (isMelee) {
            event.setAmount(event.getAmount() * 0.85f);
        }
    }

    // ===== Zumbis e esqueletos não atacam =====

    @Override
    public void onMobTarget(ServerPlayer player, LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) return;

        Entity target = event.getNewAboutToBeSetTarget();
        if (target != player) return;

        boolean isUndead =
                mob instanceof Zombie
                        || mob instanceof Skeleton
                        || mob instanceof Stray
                        || mob instanceof WitherSkeleton
                        || mob instanceof Husk
                        || mob instanceof Drowned
                        || mob instanceof ZombifiedPiglin;

        if (isUndead) {
            event.setCanceled(true);
        }
    }
}