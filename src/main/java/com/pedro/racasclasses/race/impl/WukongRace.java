package com.pedro.racasclasses.race.impl;

import com.pedro.racasclasses.RacasClasses;
import com.pedro.racasclasses.race.Race;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WukongRace implements Race {

    @Override
    public String getId() { return "wukong"; }

    @Override
    public String getDisplayName() { return "Wukong"; }

    @Override
    public double getMaxHealth() { return 22.0; }

    @Override
    public double getAttackDamage() { return 1.2; }

    @Override
    public double getMovementSpeed() { return 0.11; }

    @Override
    public boolean hasNightVision() { return true; }

    // ===== Passiva: Salto +1 bloco =====
    
    private static final ResourceLocation JUMP_BOOST_ID = 
        ResourceLocation.fromNamespaceAndPath(RacasClasses.MODID, "wukong_jump_boost");
    
    @Override
    public void onRaceEnter(ServerPlayer player) {
        // Adiciona +0.1 ao Jump Strength (aproximadamente +1 bloco)
        player.getAttribute(Attributes.JUMP_STRENGTH).addPermanentModifier(
            new AttributeModifier(
                JUMP_BOOST_ID,
                0.1,
                AttributeModifier.Operation.ADD_VALUE
            )
        );
    }

    @Override
    public void onRaceExit(ServerPlayer player) {
        // Remove o modificador ao sair da raça
        player.getAttribute(Attributes.JUMP_STRENGTH).removeModifier(JUMP_BOOST_ID);
    }

    // ===== Passiva: Escalada (igual Hadozee) =====

    @Override
    public void onPlayerTick(ServerPlayer player) {
        // Reseta double jump quando toca no chão
        checkGroundReset(player);
        
        // Escalada melhorada: SHIFT + olhando para bloco sólido
        if(player.isShiftKeyDown() && !player.onGround()) {
            // Pega direção que o player está olhando
            Vec3 lookVec = player.getLookAngle();
            net.minecraft.core.Direction lookDir = net.minecraft.core.Direction.getNearest(lookVec.x, 0, lookVec.z);
            
            // Checa bloco na frente (1 bloco de distância)
            net.minecraft.core.BlockPos frontPos = player.blockPosition().relative(lookDir);
            net.minecraft.world.level.block.state.BlockState frontBlock = player.level().getBlockState(frontPos);
            
            // Se tiver bloco sólido na frente E olhando para ele
            if (!frontBlock.isAir() && frontBlock.isSolid()) {
                // Verifica se está realmente olhando para frente (ângulo < 60°)
                double lookAngle = Math.toDegrees(Math.acos(lookVec.y));
                if (lookAngle > 30 && lookAngle < 150) { // Não tá olhando muito pra cima ou pra baixo
                    // Sobe como uma escada
                    Vec3 motion = player.getDeltaMovement();
                    player.setDeltaMovement(motion.x, 0.3, motion.z); // Velocidade de escalada
                    player.hurtMarked = true;
                    player.fallDistance = 0; // Reseta dano de queda
                }
            }
        }

        // Hidrofobia: 1 de dano por tick na água (não chuva)
        if (player.isInWater()) {
            // Checa se não é só chuva
            ServerLevel level = player.serverLevel();
            if (!level.isRainingAt(player.blockPosition())) {
                player.hurt(level.damageSources().drown(), 1.0f);
            }
        }
    }

    // ===== Pulo Duplo =====

    public void tryDoubleJump(ServerPlayer player) {
        // Não pode usar no chão
        if (player.onGround()) return;

        long currentTick = player.serverLevel().getServer().getTickCount();
        
        // Pega o último tick que usou double jump
        long lastJump = player.getPersistentData().getLong("wukong_last_double_jump");

        // Checa se já está no chão desde o último pulo
        if (player.getPersistentData().getBoolean("wukong_used_double_jump")) {
            return; // Já usou o double jump neste ar
        }

        // Aplica o impulso vertical
        player.setDeltaMovement(player.getDeltaMovement().x, 0.6, player.getDeltaMovement().z);
        player.hurtMarked = true;

        // Marca que usou
        player.getPersistentData().putBoolean("wukong_used_double_jump", true);
        player.getPersistentData().putLong("wukong_last_double_jump", currentTick);

        // Som e partículas
        player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.5f, 1.5f);
    }

    // Reseta o double jump quando toca no chão
    private void checkGroundReset(ServerPlayer player) {
        if (player.onGround() && player.getPersistentData().getBoolean("wukong_used_double_jump")) {
            player.getPersistentData().putBoolean("wukong_used_double_jump", false);
        }
    }

    // ===== Habilidade H: Rugido =====

    private static final Map<UUID, Long> ABILITY_COOLDOWNS = new HashMap<>();
    private static final int COOLDOWN_TICKS = 1800; // 90s

    @Override
    public boolean canUseAbility() { return true; }

    @Override
    public void executeAbility(ServerPlayer player) {
        long currentTick = player.serverLevel().getServer().getTickCount();
        Long readyAt = ABILITY_COOLDOWNS.get(player.getUUID());

        if (readyAt != null && currentTick < readyAt) {
            double seconds = (readyAt - currentTick) / 20.0;
            player.sendSystemMessage(Component.literal(String.format("§cRugido em recarga: §f%.1fs", seconds)));
            return;
        }

        ServerLevel level = player.serverLevel();
        Vec3 pos = player.position();

        // Aplica Weakness I + Nausea I em mobs e players num raio de 6 blocos
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class,
            new AABB(pos.add(-6, -6, -6), pos.add(6, 6, 6)),
            e -> e != player && e.isAlive()
        );

        if (targets.isEmpty()) {
            player.sendSystemMessage(Component.literal("§7Nenhum alvo por perto."));
        } else {
            for (LivingEntity target : targets) {
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 1, 0, false, false)); // Instantâneo
                target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 1, 0, false, false)); // Instantâneo
            }

            player.sendSystemMessage(Component.literal("§aRugido!"));
        }

        // Som
        level.playSound(null, pos.x, pos.y, pos.z,
            SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 2.0f, 0.8f);

        ABILITY_COOLDOWNS.put(player.getUUID(), currentTick + COOLDOWN_TICKS);
    }
}
