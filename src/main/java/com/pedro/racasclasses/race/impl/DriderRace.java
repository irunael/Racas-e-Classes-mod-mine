package com.pedro.racasclasses.race.impl;

import com.pedro.racasclasses.race.Race;
import com.pedro.racasclasses.race.RacialWeakness;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DriderRace implements Race {

    @Override
    public String getId() { return "drider"; }

    @Override
    public String getDisplayName() { return "Drider"; }

    @Override
    public double getMaxHealth() { return 22.0; }

    @Override
    public double getAttackDamage() { return 1.2; }

    @Override
    public double getMovementSpeed() { return 0.10; }

    @Override
    public boolean hasNightVision() { return true; }

    // ===== Passiva: Escalada (igual Hadozee) + Fome mais lenta =====

    @Override
    public void onPlayerTick(ServerPlayer player) {
        // Fome 20% mais lenta
        RacialWeakness.slowerHunger(player);

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
    }

    // ===== Passiva: Aranhas aliadas =====

    @Override
    public void onMobTarget(ServerPlayer player, LivingChangeTargetEvent event) {
        // Se uma aranha tentar atacar o player, cancela
        if (event.getEntity() instanceof Spider && event.getNewAboutToBeSetTarget() == player) {
            event.setNewAboutToBeSetTarget(null);
        }
    }

    // ===== Passiva: Carnívoro (só come carne) =====

    @Override
    public void onItemUse(ServerPlayer player, PlayerInteractEvent.RightClickItem event) {
        ItemStack stack = event.getItemStack();
        
        if (!stack.has(net.minecraft.core.component.DataComponents.FOOD)) return;

        FoodProperties food = stack.get(net.minecraft.core.component.DataComponents.FOOD);
        if (food == null) return;

        // Verifica se NÃO é carne
        String itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        
        boolean isMeat = itemId.contains("beef") || 
                        itemId.contains("pork") || 
                        itemId.contains("chicken") || 
                        itemId.contains("mutton") || 
                        itemId.contains("rabbit") || 
                        itemId.contains("cod") || 
                        itemId.contains("salmon") || 
                        itemId.contains("tropical_fish") || 
                        itemId.contains("rotten_flesh") ||
                        itemId.contains("spider_eye");

        if (!isMeat) {
            event.setCanceled(true);
            player.sendSystemMessage(Component.literal("§cDriders só comem carne!"));
        }
    }

    // ===== Habilidade H: Teia =====

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
            player.sendSystemMessage(Component.literal(String.format("§cTeia em recarga: §f%.1fs", seconds)));
            return;
        }

        ServerLevel level = player.serverLevel();
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = start.add(look.scale(10)); // 10 blocos

        // Raycast para entidades
        EntityHitResult entityHit = raycastEntity(player, start, end);
        
        if (entityHit != null && entityHit.getEntity() instanceof LivingEntity target) {
            // Acertou um LivingEntity - encapsula por 5s
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 255, false, false)); // Slowness 255
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 255, false, false)); // Weakness 255
            target.addEffect(new MobEffectInstance(MobEffects.JUMP, 100, -128, false, false)); // Jump Boost -128 (não pula)

            player.sendSystemMessage(Component.literal("§aTeia disparada!"));

            // Partículas no alvo
            level.sendParticles(ParticleTypes.ITEM_SNOWBALL,
                target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                30, 0.3, 0.5, 0.3, 0.1);

            // Som
            level.playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.SPIDER_AMBIENT, SoundSource.PLAYERS, 1.0f, 1.2f);
        } else {
            // Não acertou nada
            player.sendSystemMessage(Component.literal("§7A teia não acertou nada."));
        }

        // Som de disparo
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 0.8f, 1.5f);

        ABILITY_COOLDOWNS.put(player.getUUID(), currentTick + COOLDOWN_TICKS);
    }

    // ===== Helper: Raycast para entidades =====

    private EntityHitResult raycastEntity(ServerPlayer player, Vec3 start, Vec3 end) {
        ServerLevel level = player.serverLevel();
        Vec3 direction = end.subtract(start).normalize();
        double distance = start.distanceTo(end);

        // Lista todas as entidades no caminho
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class,
            player.getBoundingBox().expandTowards(direction.scale(distance)).inflate(2.0),
            e -> e != player && e.isAlive()
        );

        LivingEntity closest = null;
        double closestDistance = distance;

        for (LivingEntity entity : entities) {
            // Checa se a entidade está no caminho do raycast
            Vec3 entityPos = entity.position().add(0, entity.getBbHeight() / 2, 0);
            Vec3 toEntity = entityPos.subtract(start);
            double projection = toEntity.dot(direction);

            if (projection < 0 || projection > distance) continue;

            Vec3 closestPoint = start.add(direction.scale(projection));
            double distanceToRay = closestPoint.distanceTo(entityPos);

            if (distanceToRay <= entity.getBbWidth() / 2 + 0.5) {
                double dist = start.distanceTo(entityPos);
                if (dist < closestDistance) {
                    closest = entity;
                    closestDistance = dist;
                }
            }
        }

        if (closest != null) {
            return new EntityHitResult(closest);
        }

        return null;
    }

    // ===== Fraqueza: +50% dano de fogo =====
    @Override
    public void onPlayerHurt(ServerPlayer player, LivingIncomingDamageEvent event) {
        RacialWeakness.applyFire(event, 1.5f);
    }
}
