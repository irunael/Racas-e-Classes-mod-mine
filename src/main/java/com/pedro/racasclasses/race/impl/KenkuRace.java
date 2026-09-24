package com.pedro.racasclasses.race.impl;

import com.pedro.racasclasses.race.Race;
import com.pedro.racasclasses.race.RacialWeakness;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.monster.Vindicator;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

public class KenkuRace implements Race {

    @Override
    public String getId() { return "kenku"; }

    @Override
    public String getDisplayName() { return "Kenku"; }

    @Override
    public double getMaxHealth() { return 20.0; }

    @Override
    public double getMovementSpeed() { return 0.11; }

    @Override
    public boolean hasNightVision() { return true; }

    @Override
    public double getScale() { return 0.85; }

    // ===== Invisibilidade ao agachar (sem cooldown) =====

    @Override
    public void onPlayerTick(ServerPlayer player) {
        if (player.tickCount % 10 != 0) return;

        if (player.isShiftKeyDown()) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.INVISIBILITY, 40, 0, false, false, false));
        }
    }

    // ===== Mimicry (tecla H) — som aleatório =====

    @Override
    public boolean canUseAbility() {
        return true;
    }

    @Override
    public void executeAbility(ServerPlayer player) {
        ServerLevel level = player.serverLevel();

        SoundEvent[] sons = {
                SoundEvents.ZOMBIE_AMBIENT,
                SoundEvents.SKELETON_AMBIENT,
                SoundEvents.CREEPER_PRIMED,
                SoundEvents.SPIDER_AMBIENT,
                SoundEvents.ENDERMAN_AMBIENT,
                SoundEvents.WITCH_AMBIENT,
                SoundEvents.PILLAGER_AMBIENT,
                SoundEvents.WITHER_SKELETON_AMBIENT
        };

        SoundEvent som = sons[player.getRandom().nextInt(sons.length)];

        level.playSound(null,
                player.getX(), player.getY(), player.getZ(),
                som, SoundSource.PLAYERS,
                1.0f, 1.0f);

        player.sendSystemMessage(Component.literal("§7Você imita um som..."));
    }

    // ===== Villagers recusam troca =====

    @Override
    public boolean blockVillagerTrade(ServerPlayer player) {
        return true;
    }

    // ===== Illagers (pillagers, etc) não atacam =====

    @Override
    public void onMobTarget(ServerPlayer player, LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) return;
        if (event.getNewAboutToBeSetTarget() != player) return;

        if (mob instanceof Pillager
                || mob instanceof Vindicator
                || mob instanceof Evoker
                || mob instanceof Ravager
                || mob instanceof Vex) {
            event.setCanceled(true);
        }
    }

    // ===== Fraqueza: +30% dano de projéteis =====
    @Override
    public void onPlayerHurt(ServerPlayer player, LivingIncomingDamageEvent event) {
        RacialWeakness.applyProjectile(event, 1.3f);
    }
}