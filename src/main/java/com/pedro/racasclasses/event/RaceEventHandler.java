package com.pedro.racasclasses.event;

import com.pedro.racasclasses.RacasClasses;
import com.pedro.racasclasses.capability.ModAttachments;
import com.pedro.racasclasses.capability.PlayerRaceData;
import com.pedro.racasclasses.race.Race;
import com.pedro.racasclasses.race.RaceRegistry;
import com.pedro.racasclasses.race.RaceElytra;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.minecraft.world.item.ItemStack;

@EventBusSubscriber(modid = RacasClasses.MODID)
public class RaceEventHandler {

    private static final ResourceLocation ID_MAX_HEALTH =
            ResourceLocation.fromNamespaceAndPath(RacasClasses.MODID, "race_max_health");
    private static final ResourceLocation ID_ATTACK_DAMAGE =
            ResourceLocation.fromNamespaceAndPath(RacasClasses.MODID, "race_attack_damage");
    private static final ResourceLocation ID_MOVEMENT_SPEED =
            ResourceLocation.fromNamespaceAndPath(RacasClasses.MODID, "race_movement_speed");
    private static final ResourceLocation ID_ARMOR =
            ResourceLocation.fromNamespaceAndPath(RacasClasses.MODID, "race_armor");
    private static final ResourceLocation ID_SCALE =
            ResourceLocation.fromNamespaceAndPath(RacasClasses.MODID, "race_scale");
    private static final ResourceLocation ID_MINING_SPEED =
            ResourceLocation.fromNamespaceAndPath(RacasClasses.MODID, "race_mining_speed");
    private static final ResourceLocation ID_SWIM_SPEED =
            ResourceLocation.fromNamespaceAndPath(RacasClasses.MODID, "race_swim_speed");

    // ============================================================
    //   ATRIBUTOS / LOGIN / RESPAWN / DIMENSÃO
    // ============================================================

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            applyRaceAttributes(player);

            PlayerRaceData data = player.getData(ModAttachments.PLAYER_RACE);
            Race race = RaceRegistry.get(data.getRaceId());
            if (race != null) {
                race.onPlayerJoin(player);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            applyRaceAttributes(player);

            PlayerRaceData data = player.getData(ModAttachments.PLAYER_RACE);
            Race race = RaceRegistry.get(data.getRaceId());
            if (race != null) {
                race.onPlayerRespawn(player);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            applyRaceAttributes(player);
            PlayerRaceData data = player.getData(ModAttachments.PLAYER_RACE);
            Race race = RaceRegistry.get(data.getRaceId());
            if (race != null) race.onPlayerJoin(player);
        }
    }

    public static void applyRaceAttributes(ServerPlayer player) {
        PlayerRaceData data = player.getData(ModAttachments.PLAYER_RACE);
        Race race = RaceRegistry.get(data.getRaceId());

        // Sem raça? Limpa modificadores raciais; CON/DEX da HUD continuam.
        if (race == null) {
            clearAllModifiers(player);
            player.removeEffect(MobEffects.NIGHT_VISION);
            com.pedro.racasclasses.attribute.AttributeBonus.applyVanillaModifiers(player);
            return;
        }

        // --- MAX HEALTH ---
        // --- MAX HEALTH ---
        AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            removeModifier(maxHealth, ID_MAX_HEALTH);
            double bonus = race.getMaxHealth() - 20.0;
            bonus += race.getSubraceHealthBonus(player);   // ← ADICIONA ESSA LINHA
            if (bonus != 0) {
                maxHealth.addPermanentModifier(new AttributeModifier(
                        ID_MAX_HEALTH, bonus, AttributeModifier.Operation.ADD_VALUE));
            }
        }

        // --- ATTACK DAMAGE ---
        AttributeInstance attackDamage = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage != null) {
            removeModifier(attackDamage, ID_ATTACK_DAMAGE);
            double bonus = race.getAttackDamage() - 1.0;
            if (bonus != 0) {
                attackDamage.addPermanentModifier(new AttributeModifier(
                        ID_ATTACK_DAMAGE, bonus, AttributeModifier.Operation.ADD_VALUE));
            }
        }

        // --- MOVEMENT SPEED ---
        AttributeInstance movementSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed != null) {
            removeModifier(movementSpeed, ID_MOVEMENT_SPEED);
            double bonus = race.getMovementSpeed() - 0.1;
            if (bonus != 0) {
                movementSpeed.addPermanentModifier(new AttributeModifier(
                        ID_MOVEMENT_SPEED, bonus, AttributeModifier.Operation.ADD_VALUE));
            }
        }

        // --- ARMOR ---
        AttributeInstance armor = player.getAttribute(Attributes.ARMOR);
        if (armor != null) {
            removeModifier(armor, ID_ARMOR);
            if (race.getArmor() != 0) {
                armor.addPermanentModifier(new AttributeModifier(
                        ID_ARMOR, race.getArmor(), AttributeModifier.Operation.ADD_VALUE));
            }
        }

        // --- SCALE ---
        AttributeInstance scale = player.getAttribute(Attributes.SCALE);
        if (scale != null) {
            removeModifier(scale, ID_SCALE);
            double bonus = race.getScale() - 1.0;
            if (bonus != 0) {
                scale.addPermanentModifier(new AttributeModifier(
                        ID_SCALE, bonus, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            }
        }

        // --- MINING SPEED ---
        AttributeInstance miningSpeed = player.getAttribute(Attributes.BLOCK_BREAK_SPEED);
        if (miningSpeed != null) {
            removeModifier(miningSpeed, ID_MINING_SPEED);
            if (race.getMiningSpeedBonus() != 0) {
                miningSpeed.addPermanentModifier(new AttributeModifier(
                        ID_MINING_SPEED, race.getMiningSpeedBonus(), AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            }
        }

        // --- SWIM SPEED ---
        AttributeInstance swimSpeed = player.getAttribute(Attributes.WATER_MOVEMENT_EFFICIENCY);
        if (swimSpeed != null) {
            removeModifier(swimSpeed, ID_SWIM_SPEED);
            if (race.getSwimSpeedBonus() != 0) {
                swimSpeed.addPermanentModifier(new AttributeModifier(
                        ID_SWIM_SPEED, race.getSwimSpeedBonus(), AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            }
        }

        // --- NIGHT VISION ---
        if (race.hasNightVision()) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.NIGHT_VISION,
                    Integer.MAX_VALUE,
                    0,
                    false, false, false));
        } else {
            player.removeEffect(MobEffects.NIGHT_VISION);
        }

        // HP/velocidade/armadura da HUD somam com a raça (modifiers com IDs diferentes).
        com.pedro.racasclasses.attribute.AttributeBonus.applyVanillaModifiers(player);
    }

    private static void clearAllModifiers(ServerPlayer player) {
        clearModifier(player, Attributes.MAX_HEALTH, ID_MAX_HEALTH);
        clearModifier(player, Attributes.ATTACK_DAMAGE, ID_ATTACK_DAMAGE);
        clearModifier(player, Attributes.MOVEMENT_SPEED, ID_MOVEMENT_SPEED);
        clearModifier(player, Attributes.ARMOR, ID_ARMOR);
        clearModifier(player, Attributes.SCALE, ID_SCALE);
        clearModifier(player, Attributes.BLOCK_BREAK_SPEED, ID_MINING_SPEED);
        clearModifier(player, Attributes.WATER_MOVEMENT_EFFICIENCY, ID_SWIM_SPEED);
    }

    private static void clearModifier(ServerPlayer player, net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attr, ResourceLocation id) {
        AttributeInstance instance = player.getAttribute(attr);
        if (instance != null) removeModifier(instance, id);
    }

    private static void removeModifier(AttributeInstance instance, ResourceLocation id) {
        if (instance.getModifier(id) != null) {
            instance.removeModifier(id);
        }
    }

    // ============================================================
    //   RESISTÊNCIA A DANO (genérico)
    // ============================================================

    @SubscribeEvent
    public static void onPlayerHurt(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        PlayerRaceData data = player.getData(ModAttachments.PLAYER_RACE);
        Race race = RaceRegistry.get(data.getRaceId());

        if (race == null) return;

        DamageSource source = event.getSource();

        // --- Resistência a poison (genérico) ---
        if (race.getPoisonResistance() > 0) {
            boolean isPoisonDamage =
                    (source.is(DamageTypes.MAGIC) && player.hasEffect(MobEffects.POISON))
                            || (source.getMsgId().equals("magic") && player.hasEffect(MobEffects.POISON))
                            || (player.hasEffect(MobEffects.POISON) && source.getEntity() == null);

            if (isPoisonDamage) {
                float resistMultiplier = (float) (1.0 - race.getPoisonResistance());
                event.setAmount(event.getAmount() * resistMultiplier);
            }
        }
    }

    // ============================================================
    //   BLOQUEIO DE ARMADURA DE PEITO (Aarakocra)
    // ============================================================

    @SubscribeEvent
    public static void onEquipmentChange(net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (event.getSlot() != net.minecraft.world.entity.EquipmentSlot.CHEST) return;

        PlayerRaceData data = player.getData(ModAttachments.PLAYER_RACE);
        String raceId = data.getRaceId();
        if (!raceId.equals("aarakocra") && !raceId.equals("aasimar")) return;

        // Se o que tá sendo equipado É elytra, deixa
        if (event.getTo().is(net.minecraft.world.item.Items.ELYTRA)) return;

        // Reverte no próximo tick
        player.server.execute(() -> {
            if (!player.isAlive()) return;

            PlayerRaceData d = player.getData(ModAttachments.PLAYER_RACE);
            String currentRaceId = d.getRaceId();
            if (!currentRaceId.equals("aarakocra") && !currentRaceId.equals("aasimar")) return;

            // Devolve o item pro inventário
            ItemStack tentouEquipar = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST);
            if (RaceElytra.isRaceElytra(tentouEquipar)) return;
            if (!tentouEquipar.isEmpty()) {
                player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST, ItemStack.EMPTY);
                if (!player.getInventory().add(tentouEquipar.copy())) {
                    player.drop(tentouEquipar.copy(), false);
                }
            }

            // Re-equipa a elytra da raça certa
            ItemStack elytra;
            if (currentRaceId.equals("aasimar")) {
                elytra = com.pedro.racasclasses.race.impl.AasimarRace.createElytra(player);
            } else {
                elytra = com.pedro.racasclasses.race.impl.AarakocraRace.createElytra(player);
            }
            player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST, elytra);

            RacasClasses.LOGGER.info("[{}] Elytra re-equipada!", currentRaceId.toUpperCase());
        });
    }

    // ============================================================
    //   COPIAR DADOS AO MORRER/RESPAWNAR
    // ============================================================

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;

        if (!(event.getOriginal() instanceof ServerPlayer oldPlayer)) return;
        if (!(event.getEntity() instanceof ServerPlayer newPlayer)) return;

        PlayerRaceData oldData = oldPlayer.getData(ModAttachments.PLAYER_RACE);
        PlayerRaceData newData = newPlayer.getData(ModAttachments.PLAYER_RACE);

        // Copia os dados
        newData.setRaceId(oldData.getRaceId());
        newData.setDragonbornSubrace(oldData.getDragonbornSubrace());
        newData.setElfSubrace(oldData.getElfSubrace());
        newData.setGnomeSubrace(oldData.getGnomeSubrace());
        newData.setHalflingSubrace(oldData.getHalflingSubrace());
        newData.setTieflingSubrace(oldData.getTieflingSubrace());
        newPlayer.syncData(ModAttachments.PLAYER_RACE);

        String raceId = oldData.getRaceId();

        // Remove elytras do inventário do player ANTIGO (antes do drop)
        if (raceId.equals("aarakocra") || raceId.equals("aasimar")) {
            // Limpa o slot do peito
            oldPlayer.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST, ItemStack.EMPTY);

            // Remove elytras do inventário
            for (int i = 0; i < oldPlayer.getInventory().getContainerSize(); i++) {
                ItemStack stack = oldPlayer.getInventory().getItem(i);
                if (RaceElytra.isRaceElytra(stack)) {
                    oldPlayer.getInventory().setItem(i, ItemStack.EMPTY);
                }
            }
        }

        RacasClasses.LOGGER.info("[CLONE] Copiado raceId={}", oldData.getRaceId());
    }
    @SubscribeEvent
    public static void onLivingDrops(net.neoforged.neoforge.event.entity.living.LivingDropsEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        PlayerRaceData data = player.getData(ModAttachments.PLAYER_RACE);
        String raceId = data.getRaceId();

        if (!raceId.equals("aarakocra") && !raceId.equals("aasimar")) return;

        // Remove elytras dos drops
        event.getDrops().removeIf(itemEntity ->
                RaceElytra.isRaceElytra(itemEntity.getItem())
        );
    }
}
