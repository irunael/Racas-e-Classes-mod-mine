package com.pedro.racasclasses.race.impl;

import com.pedro.racasclasses.race.Race;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LoxodonRace implements Race {

    @Override
    public String getId() { return "loxodon"; }

    @Override
    public String getDisplayName() { return "Loxodon"; }

    @Override
    public double getMaxHealth() { return 28.0; }

    @Override
    public double getAttackDamage() { return 1.1; }

    @Override
    public double getMovementSpeed() { return 0.08; }

    @Override
    public double getScale() { return 1.30; }

    @Override
    public double getArmor() { return 4.0; }

    @Override
    public boolean hasNightVision() { return true; }

    // ===== Passiva: Resistência a knockback =====
    // Nota: knockback resistance é geralmente aplicado via atributo no RaceManager

    // ===== Passiva: +2 dano desarmado (tromba) =====

    @Override
    public void onAttackEntity(ServerPlayer player, LivingIncomingDamageEvent event) {
        if (!player.getMainHandItem().isEmpty()) return;
        event.setAmount(event.getAmount() + 2.0f);
    }

    // ===== Passiva: Imunidade a queda (3 blocos) =====

    @Override
    public void onFall(ServerPlayer player, LivingFallEvent event) {
        if (event.getDistance() <= 3.0f) {
            event.setDamageMultiplier(0.0f);
        }
    }

    // ===== Habilidade H: Trunk Slam =====

    private static final Map<UUID, Long> ABILITY_COOLDOWNS = new HashMap<>();
    private static final int COOLDOWN_TICKS = 600; // 30s

    @Override
    public boolean canUseAbility() { return true; }

    @Override
    public void executeAbility(ServerPlayer player) {
        long currentTick = player.serverLevel().getServer().getTickCount();
        Long readyAt = ABILITY_COOLDOWNS.get(player.getUUID());

        if (readyAt != null && currentTick < readyAt) {
            double seconds = (readyAt - currentTick) / 20.0;
            player.sendSystemMessage(Component.literal(String.format("§cTrunk Slam em recarga: §f%.1fs", seconds)));
            return;
        }

        ServerLevel level = player.serverLevel();
        Vec3 pos = player.position();

        // Ataque em área: 6 de dano em mobs num raio de 3 blocos + knockback forte
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, 
            new AABB(pos.add(-3, -1, -3), pos.add(3, 2, 3)), 
            e -> e != player && e.isAlive()
        );

        if (targets.isEmpty()) {
            player.sendSystemMessage(Component.literal("§7Nenhum alvo próximo."));
        } else {
            for (LivingEntity target : targets) {
                // Dano
                target.hurt(level.damageSources().playerAttack(player), 6.0f);

                // Knockback forte (empurra para longe do player)
                Vec3 direction = target.position().subtract(pos).normalize();
                target.setDeltaMovement(direction.x * 2.0, 0.5, direction.z * 2.0);
                target.hurtMarked = true;
            }

            player.sendSystemMessage(Component.literal("§aTrunk Slam!"));

            // Partículas EXPLOSION
            level.sendParticles(ParticleTypes.EXPLOSION, pos.x, pos.y, pos.z, 5, 2.0, 0.5, 2.0, 0.1);

            // Som GENERIC_EXPLODE
            level.playSound(null, pos.x, pos.y, pos.z, 
                SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.0f, 0.8f);
        }

        ABILITY_COOLDOWNS.put(player.getUUID(), currentTick + COOLDOWN_TICKS);
    }
}
