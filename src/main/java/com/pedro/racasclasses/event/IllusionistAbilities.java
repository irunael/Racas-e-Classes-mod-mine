package com.pedro.racasclasses.event;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.pedro.racasclasses.RacasClasses;
import com.pedro.racasclasses.capability.ModAttachments;
import com.pedro.racasclasses.capability.PlayerClassData;
import com.pedro.racasclasses.entity.IllusionCloneEntity;
import com.pedro.racasclasses.entity.ModEntities;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

@EventBusSubscriber(modid = RacasClasses.MODID)
public final class IllusionistAbilities {
    private static final int ILLUSION_COOLDOWN = 90 * 20;
    private static final int DAGGER_COOLDOWN = 120 * 20;
    private static final float DAGGER_DAMAGE = 4.0F;
    private static final double ATTACK_RADIUS = 5.0;

    private static final Map<UUID, Long> ILLUSION_READY_AT = new HashMap<>();
    private static final Map<UUID, Long> DAGGER_READY_AT = new HashMap<>();
    private static final Map<UUID, DaggerSwarm> ACTIVE_SWARMS = new HashMap<>();

    private IllusionistAbilities() {}

    public static void useIllusions(ServerPlayer player) {
        if (!isIllusionist(player)) return;
        long now = player.serverLevel().getGameTime();
        long ready = ILLUSION_READY_AT.getOrDefault(player.getUUID(), 0L);
        if (now < ready) {
            showCooldown(player, ready - now);
            return;
        }

        for (int i = 0; i < 3; i++) {
            double angle = Math.toRadians(player.getYRot()) + i * Math.PI * 2.0 / 3.0;
            IllusionCloneEntity clone = new IllusionCloneEntity(ModEntities.ILLUSION_CLONE.get(), player.level());
            clone.setOwner(player.getUUID());
            clone.setCustomName(player.getName());
            clone.setCustomNameVisible(true);
            clone.moveTo(player.getX() + Math.cos(angle) * 1.7, player.getY(),
                    player.getZ() + Math.sin(angle) * 1.7, player.getYRot(), player.getXRot());
            player.level().addFreshEntity(clone);
        }
        ILLUSION_READY_AT.put(player.getUUID(), now + ILLUSION_COOLDOWN);
        player.displayClientMessage(Component.literal("§dTrês cópias ilusórias foram criadas por 15 segundos."), true);
    }

    public static void useDaggers(ServerPlayer player) {
        if (!isIllusionist(player)) return;
        DaggerSwarm active = ACTIVE_SWARMS.get(player.getUUID());
        if (active != null && active.remainingDaggers() > 0) {
            player.displayClientMessage(Component.literal("§eVocê ainda possui §f" + active.remainingDaggers()
                    + " §eadaga(s) espectral(is) ativa(s)."), true);
            return;
        }
        long now = player.serverLevel().getGameTime();
        long ready = DAGGER_READY_AT.getOrDefault(player.getUUID(), 0L);
        if (now < ready) {
            showCooldown(player, ready - now);
            return;
        }

        List<ItemEntity> daggers = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            double angle = i * Math.PI * 2.0 / 3.0;
            ItemEntity dagger = new ItemEntity(player.level(),
                    player.getX() + Math.cos(angle) * 1.45, player.getY() + 1.3,
                    player.getZ() + Math.sin(angle) * 1.45,
                    new ItemStack(RacasClasses.SPECTRAL_DAGGER.get()));
            dagger.setNoGravity(true);
            dagger.setInvulnerable(true);
            dagger.setNeverPickUp();
            dagger.setUnlimitedLifetime();
            player.level().addFreshEntity(dagger);
            daggers.add(dagger);
        }
        ACTIVE_SWARMS.put(player.getUUID(), new DaggerSwarm(player, daggers));
        DAGGER_READY_AT.put(player.getUUID(), now + DAGGER_COOLDOWN);
        player.displayClientMessage(Component.literal("§dAdagas espectrais invocadas. Elas permanecem até os três golpes serem usados."), true);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        ACTIVE_SWARMS.entrySet().removeIf(entry -> !entry.getValue().tick());
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        ACTIVE_SWARMS.values().forEach(DaggerSwarm::remove);
        ACTIVE_SWARMS.clear();
        ILLUSION_READY_AT.clear();
        DAGGER_READY_AT.clear();
    }

    private static boolean isIllusionist(ServerPlayer player) {
        PlayerClassData data = player.getData(ModAttachments.PLAYER_CLASS);
        if (data.getClassId().equals("rogue") && data.getSubclassId().equals("illusionist")) return true;
        player.displayClientMessage(Component.literal("§cEsta habilidade exige Ladino — Ilusionista."), true);
        return false;
    }

    private static void showCooldown(ServerPlayer player, long ticks) {
        long seconds = (ticks + 19) / 20;
        player.displayClientMessage(Component.literal("§cHabilidade em recarga: §f" + seconds + "s"), true);
    }

    private static final class DaggerSwarm {
        private final UUID ownerId;
        private final ServerLevel level;
        private final List<ItemEntity> daggers;
        private long nextAttackAt;

        private DaggerSwarm(ServerPlayer owner, List<ItemEntity> daggers) {
            this.ownerId = owner.getUUID();
            this.level = owner.serverLevel();
            this.daggers = daggers;
        }

        private int remainingDaggers() { return daggers.size(); }

        private boolean tick() {
            ServerPlayer owner = level.getServer().getPlayerList().getPlayer(ownerId);
            if (owner == null || !owner.isAlive() || owner.serverLevel() != level) {
                remove();
                return false;
            }

            double phase = level.getGameTime() * 0.14;
            for (int i = 0; i < daggers.size(); i++) {
                ItemEntity dagger = daggers.get(i);
                if (dagger.isRemoved()) continue;
                double angle = phase + i * Math.PI * 2.0 / 3.0;
                dagger.setPos(owner.getX() + Math.cos(angle) * 1.45,
                        owner.getY() + 1.25 + Math.sin(phase * 0.7 + i) * 0.18,
                        owner.getZ() + Math.sin(angle) * 1.45);
                dagger.setDeltaMovement(0, 0, 0);
            }

            long now = level.getGameTime();
            if (!daggers.isEmpty() && now >= nextAttackAt) {
                AABB area = owner.getBoundingBox().inflate(ATTACK_RADIUS);
                List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
                        target -> target != owner && !(target instanceof IllusionCloneEntity)
                                && target.isAlive() && owner.distanceToSqr(target) <= ATTACK_RADIUS * ATTACK_RADIUS);
                targets.sort(Comparator.comparingDouble(owner::distanceToSqr));
                if (!targets.isEmpty()) {
                    LivingEntity target = targets.getFirst();
                    ItemEntity usedDagger = daggers.removeFirst();
                    target.hurt(owner.damageSources().playerAttack(owner), DAGGER_DAMAGE);
                    level.sendParticles(net.minecraft.core.particles.ParticleTypes.ENCHANTED_HIT,
                            target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                            10, 0.25, 0.35, 0.25, 0.1);
                    level.playSound(null, target.blockPosition(), net.minecraft.sounds.SoundEvents.TRIDENT_HIT,
                            net.minecraft.sounds.SoundSource.PLAYERS, 0.65F, 1.35F);
                    usedDagger.discard();
                    nextAttackAt = now + 20;
                    if (daggers.isEmpty()) return false;
                }
            }
            return true;
        }

        private void remove() { daggers.forEach(ItemEntity::discard); }
    }
}
