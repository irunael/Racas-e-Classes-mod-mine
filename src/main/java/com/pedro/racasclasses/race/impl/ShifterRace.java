package com.pedro.racasclasses.race.impl;

import com.pedro.racasclasses.race.Race;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ShifterRace implements Race {

    @Override
    public String getId() { return "shifter"; }

    @Override
    public String getDisplayName() { return "Shifter"; }

    @Override
    public double getMaxHealth() { return 22.0; }

    @Override
    public double getAttackDamage() { return 1.0; }

    @Override
    public double getMovementSpeed() { return 0.11; }

    @Override
    public boolean hasNightVision() { return true; }

    // ===== Sub-raça =====

    @Override
    public boolean hasSubrace() { return true; }

    @Override
    public String[] getSubraceIds() { 
        return new String[]{"longtooth", "razorclaw", "wildhunt"}; 
    }

    @Override
    public String getSubraceDisplayName(ServerPlayer player) {
        String subrace = getSubrace(player);
        if (subrace == null) return "";
        switch (subrace) {
            case "longtooth": return "Longtooth";
            case "razorclaw": return "Razorclaw";
            case "wildhunt": return "Wildhunt";
            default: return "";
        }
    }

    // ===== Estado do Shifter =====

    private static class ShifterState {
        boolean shifting = false;
        long shiftStartTick = 0;
        long lastSpecialTick = 0;
        long cooldownUntil = 0;
    }

    private static final Map<UUID, ShifterState> SHIFTER_STATES = new HashMap<>();
    private static final Map<UUID, String> SUBRACES = new HashMap<>();

    @Override
    public void onSubraceChosen(ServerPlayer player, String subraceId) {
        SUBRACES.put(player.getUUID(), subraceId);
    }

    private String getSubrace(ServerPlayer player) {
        return SUBRACES.get(player.getUUID());
    }

    private ShifterState getState(ServerPlayer player) {
        return SHIFTER_STATES.computeIfAbsent(player.getUUID(), k -> new ShifterState());
    }

    // ===== Tick: Wildhunt glowing + controle de duração =====

    @Override
    public void onPlayerTick(ServerPlayer player) {
        long currentTick = player.serverLevel().getServer().getTickCount();
        ShifterState state = getState(player);
        String subrace = getSubrace(player);

        // Wildhunt: glowing em hostis quando agacha
        if ("wildhunt".equals(subrace) && player.isCrouching()) {
            applyGlowingToHostiles(player, 16);
        }

        // Controle de duração do shifting
        if (state.shifting) {
            long elapsed = currentTick - state.shiftStartTick;
            if (elapsed >= 600) { // 30s
                endShifting(player, state, currentTick);
            }
        }
    }

    // ===== Habilidade H =====

    @Override
    public boolean canUseAbility() { return true; }

    @Override
    public void executeAbility(ServerPlayer player) {
        long currentTick = player.serverLevel().getServer().getTickCount();
        ShifterState state = getState(player);
        String subrace = getSubrace(player);

        if (subrace == null) {
            player.sendSystemMessage(Component.literal("§cEscolha uma sub-raça primeiro!"));
            return;
        }

        // Está em cooldown?
        if (currentTick < state.cooldownUntil) {
            double seconds = (state.cooldownUntil - currentTick) / 20.0;
            player.sendSystemMessage(Component.literal(String.format("§cShifting em recarga: §f%.1fs", seconds)));
            return;
        }

        // Estado 1 → 2: ativar shifting
        if (!state.shifting) {
            startShifting(player, state, currentTick, subrace);
        } 
        // Estado 2 → Especial: usar poder especial
        else {
            useSpecialPower(player, state, currentTick, subrace);
        }
    }

    // ===== Iniciar Shifting =====

    private void startShifting(ServerPlayer player, ShifterState state, long currentTick, String subrace) {
        state.shifting = true;
        state.shiftStartTick = currentTick;

        ServerLevel level = player.serverLevel();
        Vec3 pos = player.position();

        // Partículas
        level.sendParticles(ParticleTypes.CRIT, pos.x, pos.y + 1, pos.z, 20, 0.3, 0.5, 0.3, 0.1);

        // Buffs por sub-raça
        switch (subrace) {
            case "longtooth":
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 600, 0, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600, 0, false, false));
                player.sendSystemMessage(Component.literal("§cSuas presas crescem!"));
                break;
            case "razorclaw":
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 600, 0, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600, 0, false, false));
                player.sendSystemMessage(Component.literal("§cSuas garras se afiam!"));
                break;
            case "wildhunt":
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600, 1, false, false));
                applyGlowingToHostiles(player, 16);
                player.sendSystemMessage(Component.literal("§cSeus sentidos se aguçam!"));
                break;
        }

        level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.WOLF_GROWL, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    // ===== Terminar Shifting =====

    private void endShifting(ServerPlayer player, ShifterState state, long currentTick) {
        state.shifting = false;
        state.cooldownUntil = currentTick + 1200; // 60s
        player.sendSystemMessage(Component.literal("§7Shifting terminou. Recarga: 60s"));
    }

    // ===== Poder Especial =====

    private void useSpecialPower(ServerPlayer player, ShifterState state, long currentTick, String subrace) {
        // Cooldown interno do poder especial
        long internalCd = switch (subrace) {
            case "longtooth", "razorclaw" -> 100; // 5s
            case "wildhunt" -> 200; // 10s
            default -> 100;
        };

        if (currentTick - state.lastSpecialTick < internalCd) {
            double seconds = (internalCd - (currentTick - state.lastSpecialTick)) / 20.0;
            player.sendSystemMessage(Component.literal(String.format("§cPoder especial em recarga: §f%.1fs", seconds)));
            return;
        }

        state.lastSpecialTick = currentTick;

        switch (subrace) {
            case "longtooth":
                useLongtoothBite(player);
                break;
            case "razorclaw":
                useRazorclawDoubleStrike(player);
                break;
            case "wildhunt":
                useWildhuntTrack(player);
                break;
        }
    }

    // ===== Longtooth: Mordida =====

    private void useLongtoothBite(ServerPlayer player) {
        player.sendSystemMessage(Component.literal("§cMordida!"));

        ServerLevel level = player.serverLevel();
        Vec3 pos = player.position();
        Vec3 look = player.getLookAngle();

        // Cone 3 blocos à frente
        AABB box = new AABB(pos.add(-3, -1, -3), pos.add(3, 2, 3));
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, box, e -> 
            e != player && e.isAlive() && isInCone(player, e, 3.0, 60.0)
        );

        for (LivingEntity target : targets) {
            target.hurt(level.damageSources().playerAttack(player), 6.0f);
            target.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0));
        }

        level.sendParticles(ParticleTypes.DAMAGE_INDICATOR, 
            pos.x + look.x * 2, pos.y + 1, pos.z + look.z * 2, 
            15, 0.5, 0.5, 0.5, 0.1);
    }

    // ===== Razorclaw: Golpe Duplo =====

    private void useRazorclawDoubleStrike(ServerPlayer player) {
        player.sendSystemMessage(Component.literal("§cGolpe duplo!"));

        ServerLevel level = player.serverLevel();
        Vec3 pos = player.position();

        // Mob mais próximo
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, 
            new AABB(pos.add(-3, -1, -3), pos.add(3, 2, 3)), 
            e -> e != player && e.isAlive()
        );

        if (targets.isEmpty()) {
            player.sendSystemMessage(Component.literal("§cNenhum alvo próximo!"));
            return;
        }

        LivingEntity target = targets.stream()
            .min((a, b) -> Double.compare(a.distanceTo(player), b.distanceTo(player)))
            .orElse(null);

        if (target != null) {
            float damage = (float) getAttackDamage();
            target.hurt(level.damageSources().playerAttack(player), damage);
            target.hurt(level.damageSources().playerAttack(player), damage);
            
            level.sendParticles(ParticleTypes.SWEEP_ATTACK, 
                target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(), 
                10, 0.3, 0.3, 0.3, 0.1);
        }
    }

    // ===== Wildhunt: Rastrear =====

    private void useWildhuntTrack(ServerPlayer player) {
        player.sendSystemMessage(Component.literal("§cRastreando..."));

        ServerLevel level = player.serverLevel();
        Vec3 pos = player.position();

        // Mob mais próximo
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, 
            new AABB(pos.add(-50, -50, -50), pos.add(50, 50, 50)), 
            e -> e != player && e.isAlive()
        );

        if (targets.isEmpty()) {
            player.sendSystemMessage(Component.literal("§7Nenhuma criatura detectada."));
            return;
        }

        LivingEntity nearest = targets.stream()
            .min((a, b) -> Double.compare(a.distanceTo(player), b.distanceTo(player)))
            .orElse(null);

        if (nearest != null) {
            String name = nearest.getName().getString();
            int x = (int) nearest.getX();
            int y = (int) nearest.getY();
            int z = (int) nearest.getZ();
            double distance = nearest.distanceTo(player);

            player.sendSystemMessage(Component.literal(
                String.format("§a%s §7detectado em §f[%d, %d, %d] §7(%.1f blocos)", name, x, y, z, distance)
            ));

            nearest.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0));
        }
    }

    // ===== Helpers =====

    private void applyGlowingToHostiles(ServerPlayer player, double radius) {
        ServerLevel level = player.serverLevel();
        Vec3 pos = player.position();

        List<LivingEntity> hostiles = level.getEntitiesOfClass(LivingEntity.class, 
            new AABB(pos.add(-radius, -radius, -radius), pos.add(radius, radius, radius)), 
            e -> e != player && e.isAlive() && e instanceof net.minecraft.world.entity.monster.Monster
        );

        for (LivingEntity hostile : hostiles) {
            if (!hostile.hasEffect(MobEffects.GLOWING)) {
                hostile.addEffect(new MobEffectInstance(MobEffects.GLOWING, 20, 0, false, false));
            }
        }
    }

    private boolean isInCone(ServerPlayer player, LivingEntity target, double range, double angle) {
        Vec3 look = player.getLookAngle();
        Vec3 toTarget = target.position().subtract(player.position()).normalize();
        double dot = look.dot(toTarget);
        double distance = player.distanceTo(target);
        return distance <= range && dot >= Math.cos(Math.toRadians(angle));
    }
}
