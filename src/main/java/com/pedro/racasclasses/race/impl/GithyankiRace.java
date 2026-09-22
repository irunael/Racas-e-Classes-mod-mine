package com.pedro.racasclasses.race.impl;

import com.pedro.racasclasses.race.Race;
import com.pedro.racasclasses.race.RacialWeakness;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GithyankiRace implements Race {

    private static final Map<UUID, Long> ABILITY_COOLDOWNS = new HashMap<>();
    private static final int ABILITY_COOLDOWN_TICKS = 200; // 10s

    @Override
    public String getId() { return "githyanki"; }

    @Override
    public String getDisplayName() { return "Githyanki"; }

    @Override
    public double getMaxHealth() { return 22.0; }

    @Override
    public double getAttackDamage() { return 1.2; }

    @Override
    public double getArmor() { return 1.0; }

    @Override
    public boolean hasNightVision() { return true; }

    // ===== Misty Step (tecla H) =====

    @Override
    public boolean canUseAbility() { return true; }

    @Override
    public void executeAbility(ServerPlayer player) {
        long currentTick = player.serverLevel().getServer().getTickCount();
        Long readyAt = ABILITY_COOLDOWNS.get(player.getUUID());

        if (readyAt != null && currentTick < readyAt) {
            double seconds = (readyAt - currentTick) / 20.0;
            player.sendSystemMessage(Component.literal(
                    String.format("§cMisty Step em recarga: §f%.1fs", seconds)));
            return;
        }

        ServerLevel level = player.serverLevel();

        ThrownEnderpearl pearl = new ThrownEnderpearl(level, player);

        Vec3 look = player.getLookAngle();
        Vec3 eyePos = player.getEyePosition();

        pearl.setPos(eyePos.x + look.x * 0.5, eyePos.y + look.y * 0.5, eyePos.z + look.z * 0.5);

        double speed = 2.0;
        pearl.setDeltaMovement(look.x * speed, look.y * speed, look.z * speed);

        level.addFreshEntity(pearl);

        // Marca que lançou a pearl (pra rastrear o teleporte)
        player.getPersistentData().putBoolean("githyanki_pearl_active", true);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENDER_PEARL_THROW, SoundSource.PLAYERS, 1.0f, 1.0f);

        player.sendSystemMessage(Component.literal("§aMisty Step lançado!"));

        ABILITY_COOLDOWNS.put(player.getUUID(), currentTick + ABILITY_COOLDOWN_TICKS);
    }

    // ===== Chamado pelo dispatcher quando o player teleporta =====

    public void onTeleport(ServerPlayer player, EntityTeleportEvent event) {
        // Só marca se a flag tiver ativa (ou seja, foi o Misty Step)
        if (!player.getPersistentData().getBoolean("githyanki_pearl_active")) return;

        // Marca imune por 1 segundo (20 ticks)
        long currentTick = player.serverLevel().getServer().getTickCount();
        player.getPersistentData().putInt("githyanki_immune_until", (int) (currentTick + 20));

        // Limpa a flag
        player.getPersistentData().putBoolean("githyanki_pearl_active", false);
    }

    // ===== Fraqueza mágica (+30%) + Imunidade a queda após teleporte =====

    @Override
    public void onPlayerHurt(ServerPlayer player, LivingIncomingDamageEvent event) {
        DamageSource source = event.getSource();

        // Imunidade a FALL se a flag tiver ativa (após teleporte)
        if (source.is(DamageTypes.FALL)) {
            long currentTick = player.serverLevel().getServer().getTickCount();
            int immuneUntil = player.getPersistentData().getInt("githyanki_immune_until");

            if (currentTick <= immuneUntil) {
                event.setAmount(0f);
                return;
            }
        }

        // Fraqueza: +30% dano mágico
        RacialWeakness.applyMagic(event, 1.3f);
    }
}