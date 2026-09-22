package com.pedro.racasclasses.event;

import com.pedro.racasclasses.RacasClasses;
import com.pedro.racasclasses.attribute.AttributeBonus;
import com.pedro.racasclasses.attribute.AttributeData;
import com.pedro.racasclasses.attribute.AttributeRegistry;
import com.pedro.racasclasses.capability.ModAttachments;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.TradeWithVillagerEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * XP da barra própria + bônus de combate/drops/cura/efeitos.
 */
@EventBusSubscriber(modid = RacasClasses.MODID)
public final class AttributeEventHandler {
    private static final String WIS_ABS_UNTIL = "racasclasses_wis_abs_until";

    private AttributeEventHandler() {}

    // ===== XP própria (não mexe na barra vanilla) =====

    @SubscribeEvent
    public static void onMobKilled(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if (event.getEntity() instanceof ServerPlayer) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;
        grantXp(player, AttributeRegistry.XP_KILL);
    }

    @SubscribeEvent
    public static void onOreBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        if (player.isCreative() || player.isSpectator()) return;
        if (!isOre(event.getState())) return;
        grantXp(player, AttributeRegistry.XP_ORE);
    }

    @SubscribeEvent
    public static void onTrade(TradeWithVillagerEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            grantXp(player, AttributeRegistry.XP_TRADE);
            rebateVillagerPrice(player, event);
        }
    }

    @SubscribeEvent
    public static void onCraft(PlayerEvent.ItemCraftedEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            grantXp(player, AttributeRegistry.XP_CRAFT);
        }
    }

    @SubscribeEvent
    public static void onFish(ItemFishedEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            grantXp(player, AttributeRegistry.XP_FISH);
        }
    }

    // ===== INT: mais XP vanilla (orbs) =====

    @SubscribeEvent
    public static void onVanillaMobXp(LivingExperienceDropEvent event) {
        if (!(event.getAttackingPlayer() instanceof ServerPlayer player)) return;
        double bonus = AttributeBonus.vanillaXpBonus(AttributeBonus.data(player));
        if (bonus <= 0) return;
        event.setDroppedExperience((int) Math.round(event.getDroppedExperience() * (1.0 + bonus)));
    }

    @SubscribeEvent
    public static void onVanillaBlockXp(BlockDropsEvent event) {
        // BreakEvent no 1.21.1 não expõe mais XP; o valor final está em BlockDropsEvent.
        if (!(event.getBreaker() instanceof ServerPlayer player)) return;
        double bonus = AttributeBonus.vanillaXpBonus(AttributeBonus.data(player));
        if (bonus <= 0 || event.getDroppedExperience() <= 0) return;
        event.setDroppedExperience((int) Math.round(event.getDroppedExperience() * (1.0 + bonus)));
    }

    // ===== STR / DEX combate =====

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onDodge(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (isUndodgeable(event)) return;
        double chance = AttributeBonus.dodgeChance(AttributeBonus.data(player));
        if (chance > 0 && player.getRandom().nextDouble() < chance) {
            event.setCanceled(true);
            player.displayClientMessage(Component.literal("§bEsquiva!"), true);
        }
    }

    @SubscribeEvent
    public static void onOutgoingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if (event.getEntity() == player) return;
        AttributeData data = AttributeBonus.data(player);
        if (isRanged(event)) {
            double bonus = AttributeBonus.rangedDamageBonus(data);
            if (bonus > 0) event.setAmount((float) (event.getAmount() * (1.0 + bonus)));
            return;
        }
        if (isMelee(event)) {
            double bonus = AttributeBonus.meleeDamageBonus(data);
            if (bonus > 0) event.setAmount((float) (event.getAmount() * (1.0 + bonus)));
        }
    }

    @SubscribeEvent
    public static void onKnockback(LivingKnockBackEvent event) {
        if (!(event.getEntity().getLastHurtByMob() instanceof ServerPlayer player)) return;
        double bonus = AttributeBonus.knockbackBonus(AttributeBonus.data(player));
        if (bonus <= 0) return;
        event.setStrength(event.getStrength() * (1.0f + (float) bonus));
    }

    // ===== WIS cura / absorção =====

    @SubscribeEvent
    public static void onHeal(LivingHealEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        AttributeData data = AttributeBonus.data(player);
        // Sem mixin não dá para separar tick de fome vs poção; regen extra
        // só entra se o efeito REGENERATION estiver ativo.
        double bonus = AttributeBonus.healBonus(data);
        if (player.hasEffect(net.minecraft.world.effect.MobEffects.REGENERATION)) {
            bonus += AttributeBonus.regenBonus(data);
        }
        if (bonus <= 0) return;
        event.setAmount((float) (event.getAmount() * (1.0 + bonus)));
    }

    @SubscribeEvent
    public static void onFinishUsingItem(net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!event.getItem().has(DataComponents.FOOD)) return;
        float extraHp = AttributeBonus.absorptionOnEatHp(AttributeBonus.data(player));
        if (extraHp <= 0) return;
        long until = player.serverLevel().getGameTime() + 20L * 20L;
        player.getPersistentData().putLong(WIS_ABS_UNTIL, until);
        player.setAbsorptionAmount(Math.max(player.getAbsorptionAmount(), extraHp));
    }

    @SubscribeEvent
    public static void onTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.tickCount % 20 != 0) return;
        if (!player.getPersistentData().contains(WIS_ABS_UNTIL)) return;
        long until = player.getPersistentData().getLong(WIS_ABS_UNTIL);
        if (player.serverLevel().getGameTime() < until) return;
        player.getPersistentData().remove(WIS_ABS_UNTIL);
        // Não zera absorção de maçã dourada se o efeito ainda estiver ativo.
        if (!player.hasEffect(net.minecraft.world.effect.MobEffects.ABSORPTION)) {
            player.setAbsorptionAmount(0);
        }
    }

    // ===== INT duração de efeitos (pula visão noturna racial infinita) =====

    @SubscribeEvent
    public static void onEffectAdded(MobEffectEvent.Added event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        var instance = event.getEffectInstance();
        if (instance == null || instance.isInfiniteDuration()) return;
        int duration = instance.getDuration();
        // Pula ticks de 1 e visões raciais "infinitas" (Integer.MAX_VALUE).
        if (duration < 20 || duration > 20 * 60 * 30) return;
        double bonus = AttributeBonus.effectDurationBonus(AttributeBonus.data(player));
        if (bonus <= 0) return;
        instance.mapDuration(current -> (int) Math.round(current * (1.0 + bonus)));
    }

    // ===== LUCK drops =====

    @SubscribeEvent
    public static void onOreDrops(BlockDropsEvent event) {
        if (!(event.getBreaker() instanceof ServerPlayer player)) return;
        if (!isOre(event.getState())) return;
        double chance = AttributeBonus.oreDropChance(AttributeBonus.data(player));
        if (chance <= 0 || player.getRandom().nextDouble() >= chance) return;
        duplicateItemEntities(event.getDrops(), player.serverLevel());
    }

    @SubscribeEvent
    public static void onMobDrops(LivingDropsEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        double chance = AttributeBonus.mobDropChance(AttributeBonus.data(player));
        if (chance <= 0 || player.getRandom().nextDouble() >= chance) return;
        if (event.getDrops().isEmpty()) return;
        ItemEntity source = event.getDrops().stream().skip(player.getRandom().nextInt(event.getDrops().size()))
                .findFirst().orElse(null);
        if (source == null) return;
        ItemStack copy = source.getItem().copy();
        copy.setCount(1);
        event.getDrops().add(new ItemEntity(player.serverLevel(), source.getX(), source.getY(), source.getZ(), copy));
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;
        if (!(event.getOriginal() instanceof ServerPlayer oldPlayer)) return;
        if (!(event.getEntity() instanceof ServerPlayer newPlayer)) return;
        AttributeData copied = new AttributeData();
        copied.copyFrom(oldPlayer.getData(ModAttachments.PLAYER_ATTRIBUTES));
        newPlayer.getData(ModAttachments.PLAYER_ATTRIBUTES).copyFrom(copied);
        newPlayer.syncData(ModAttachments.PLAYER_ATTRIBUTES);
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            AttributeBonus.applyVanillaModifiers(player);
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            AttributeBonus.applyVanillaModifiers(player);
        }
    }

    public static void grantXp(ServerPlayer player, int amount) {
        if (player.isCreative() || player.isSpectator()) return;
        AttributeData data = player.getData(ModAttachments.PLAYER_ATTRIBUTES);
        int levels = data.addXp(amount);
        player.syncData(ModAttachments.PLAYER_ATTRIBUTES);
        if (levels > 0) {
            player.sendSystemMessage(Component.literal(
                    "§aNível de personagem §f" + data.getCharacterLevel()
                            + "§a! +" + levels + " ponto(s). Pressione §fP§a para distribuir."));
        }
    }

    /**
     * Desconto de villager: NeoForge não deixa alterar o preço-base da oferta
     * sem mixin, então devolvemos parte do item pago depois da troca.
     */
    private static void rebateVillagerPrice(ServerPlayer player, TradeWithVillagerEvent event) {
        double discount = AttributeBonus.villagerDiscount(AttributeBonus.data(player));
        if (discount <= 0) return;
        MerchantOffer offer = event.getMerchantOffer();
        ItemStack cost = offer.getCostA().copy();
        int rebate = (int) Math.floor(cost.getCount() * discount);
        if (rebate <= 0) return;
        cost.setCount(rebate);
        if (!player.getInventory().add(cost)) player.drop(cost, false);
    }

    private static boolean isOre(BlockState state) {
        return state.is(Tags.Blocks.ORES) || state.is(BlockTags.COAL_ORES)
                || state.is(BlockTags.IRON_ORES) || state.is(BlockTags.COPPER_ORES)
                || state.is(BlockTags.GOLD_ORES) || state.is(BlockTags.REDSTONE_ORES)
                || state.is(BlockTags.EMERALD_ORES) || state.is(BlockTags.LAPIS_ORES)
                || state.is(BlockTags.DIAMOND_ORES) || state.is(Tags.Blocks.ORES_NETHERITE_SCRAP);
    }

    private static boolean isRanged(LivingIncomingDamageEvent event) {
        return event.getSource().getDirectEntity() instanceof Projectile;
    }

    private static boolean isMelee(LivingIncomingDamageEvent event) {
        if (isRanged(event)) return false;
        if (event.getSource().is(DamageTypes.MAGIC) || event.getSource().is(DamageTypes.INDIRECT_MAGIC)) return false;
        if (event.getSource().is(DamageTypes.EXPLOSION) || event.getSource().is(DamageTypes.PLAYER_EXPLOSION)) return false;
        return event.getSource().getDirectEntity() == event.getSource().getEntity();
    }

    private static boolean isUndodgeable(LivingIncomingDamageEvent event) {
        return event.getSource().is(DamageTypes.STARVE)
                || event.getSource().is(DamageTypes.DROWN)
                || event.getSource().is(DamageTypes.GENERIC_KILL)
                || event.getSource().is(DamageTypes.FELL_OUT_OF_WORLD);
    }

    private static void duplicateItemEntities(java.util.List<ItemEntity> drops, ServerLevel level) {
        java.util.List<ItemEntity> extra = new java.util.ArrayList<>();
        for (ItemEntity drop : drops) {
            ItemStack copy = drop.getItem().copy();
            extra.add(new ItemEntity(level, drop.getX(), drop.getY(), drop.getZ(), copy));
        }
        drops.addAll(extra);
    }
}
