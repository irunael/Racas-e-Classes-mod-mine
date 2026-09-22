package com.pedro.racasclasses.attribute;

import com.pedro.racasclasses.RacasClasses;
import com.pedro.racasclasses.capability.ModAttachments;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Cálculo dos bônus e aplicação dos que cabem em AttributeModifier vanilla
 * (HP, velocidade e armadura). Dano, esquiva, cura, drops etc. entram por eventos.
 */
public final class AttributeBonus {
    public static final ResourceLocation ID_CON_HEALTH =
            ResourceLocation.fromNamespaceAndPath(RacasClasses.MODID, "attr_con_health");
    public static final ResourceLocation ID_CON_ARMOR =
            ResourceLocation.fromNamespaceAndPath(RacasClasses.MODID, "attr_con_armor");
    public static final ResourceLocation ID_DEX_SPEED =
            ResourceLocation.fromNamespaceAndPath(RacasClasses.MODID, "attr_dex_speed");

    private AttributeBonus() {}

    public static AttributeData data(Player player) {
        return player.getData(ModAttachments.PLAYER_ATTRIBUTES);
    }

    /** +1% dano corpo a corpo por nível de STR (cap 50%). */
    public static double meleeDamageBonus(AttributeData data) {
        return data.getStrengthLevel() * 0.01;
    }

    /** +2% knockback a cada 5 níveis de STR (cap 20%). */
    public static double knockbackBonus(AttributeData data) {
        return (data.getStrengthLevel() / 5) * 0.02;
    }

    /** +1% dano à distância por nível de DEX (cap 50%). */
    public static double rangedDamageBonus(AttributeData data) {
        return data.getDexterityLevel() * 0.01;
    }

    /** +0.001 velocidade vanilla por nível de DEX (cap +0.05). */
    public static double movementSpeedBonus(AttributeData data) {
        return data.getDexterityLevel() * 0.001;
    }

    /** +1% chance de anular dano a cada 5 níveis de DEX (cap 10%). */
    public static double dodgeChance(AttributeData data) {
        return (data.getDexterityLevel() / 5) * 0.01;
    }

    /**
     * +0.25 coração por CON = +0.5 HP vanilla por nível (cap +25 HP / +12.5 ❤️).
     * Soma com o HP da raça, que usa outro modifier.
     */
    public static double extraMaxHealth(AttributeData data) {
        return data.getConstitutionLevel() * 0.5;
    }

    /** +0.5 armadura a cada 5 níveis de CON (cap +5). */
    public static double extraArmor(AttributeData data) {
        return (data.getConstitutionLevel() / 5) * 0.5;
    }

    /**
     * +1% duração de efeitos por INT (cap 50%).
     * Mana máxima / regen de mana ficam para quando magia existir.
     */
    public static double effectDurationBonus(AttributeData data) {
        return data.getIntelligenceLevel() * 0.01;
    }

    /** +5% XP vanilla a cada 10 níveis de INT (cap 25%). */
    public static double vanillaXpBonus(AttributeData data) {
        return (data.getIntelligenceLevel() / 10) * 0.05;
    }

    /**
     * +1% cura recebida por WIS (cap 50%).
     * Mana máxima / regen de mana ficam para quando magia existir.
     */
    public static double healBonus(AttributeData data) {
        return data.getWisdomLevel() * 0.01;
    }

    /** +2% regeneração a cada 5 níveis de WIS (cap 20%). */
    public static double regenBonus(AttributeData data) {
        return (data.getWisdomLevel() / 5) * 0.02;
    }

    /** +1 coração de absorção ao comer a cada 10 WIS (cap 5 ❤️). Valor em HP (2 por coração). */
    public static float absorptionOnEatHp(AttributeData data) {
        return (data.getWisdomLevel() / 10) * 2.0f;
    }

    public static float absorptionOnEatHearts(AttributeData data) {
        return absorptionOnEatHp(data) / 2.0f;
    }

    /** +0.5% drop extra de minério por LUCK (cap 25%). */
    public static double oreDropChance(AttributeData data) {
        return data.getLuckLevel() * 0.005;
    }

    /** +0.5% drop extra de mob a cada 5 LUCK (cap 10%). */
    public static double mobDropChance(AttributeData data) {
        return (data.getLuckLevel() / 5) * 0.005;
    }

    /** -5% preço de villager a cada 10 LUCK (cap -25%). */
    public static double villagerDiscount(AttributeData data) {
        return (data.getLuckLevel() / 10) * 0.05;
    }

    public static void applyVanillaModifiers(ServerPlayer player) {
        AttributeData data = data(player);

        AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            remove(maxHealth, ID_CON_HEALTH);
            double hp = extraMaxHealth(data);
            if (hp != 0) {
                maxHealth.addPermanentModifier(new AttributeModifier(
                        ID_CON_HEALTH, hp, AttributeModifier.Operation.ADD_VALUE));
            }
            if (player.getHealth() > player.getMaxHealth()) {
                player.setHealth(player.getMaxHealth());
            }
        }

        AttributeInstance armor = player.getAttribute(Attributes.ARMOR);
        if (armor != null) {
            remove(armor, ID_CON_ARMOR);
            double extra = extraArmor(data);
            if (extra != 0) {
                armor.addPermanentModifier(new AttributeModifier(
                        ID_CON_ARMOR, extra, AttributeModifier.Operation.ADD_VALUE));
            }
        }

        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            remove(speed, ID_DEX_SPEED);
            double extra = movementSpeedBonus(data);
            if (extra != 0) {
                speed.addPermanentModifier(new AttributeModifier(
                        ID_DEX_SPEED, extra, AttributeModifier.Operation.ADD_VALUE));
            }
        }
    }

    public static List<String> previewLines(AttributeData data) {
        List<String> lines = new ArrayList<>();
        lines.add(String.format("Melee +%.0f%%", meleeDamageBonus(data) * 100));
        lines.add(String.format("Knockback +%.0f%%", knockbackBonus(data) * 100));
        lines.add(String.format("Ranged +%.0f%%", rangedDamageBonus(data) * 100));
        lines.add(String.format("Vel +%.3f", movementSpeedBonus(data)));
        lines.add(String.format("Esquiva +%.0f%%", dodgeChance(data) * 100));
        lines.add(String.format("HP +%.1f (%s❤️)", extraMaxHealth(data), formatHearts(extraMaxHealth(data) / 2.0)));
        lines.add(String.format("Armadura +%.1f", extraArmor(data)));
        lines.add(String.format("Efeitos +%.0f%%", effectDurationBonus(data) * 100));
        lines.add(String.format("XP vanilla +%.0f%%", vanillaXpBonus(data) * 100));
        lines.add(String.format("Cura +%.0f%%", healBonus(data) * 100));
        lines.add(String.format("Regen +%.0f%%", regenBonus(data) * 100));
        lines.add(String.format("Absorção +%s❤️", formatHearts(absorptionOnEatHearts(data))));
        lines.add(String.format("Minério +%.1f%%", oreDropChance(data) * 100));
        lines.add(String.format("Drop mob +%.1f%%", mobDropChance(data) * 100));
        lines.add(String.format("Villager -%.0f%%", villagerDiscount(data) * 100));
        return lines;
    }

    private static String formatHearts(double hearts) {
        if (Math.abs(hearts - Math.rint(hearts)) < 0.05) return String.format("%.0f", hearts);
        return String.format("%.2f", hearts);
    }

    private static void remove(AttributeInstance instance, ResourceLocation id) {
        if (instance.getModifier(id) != null) instance.removeModifier(id);
    }
}
