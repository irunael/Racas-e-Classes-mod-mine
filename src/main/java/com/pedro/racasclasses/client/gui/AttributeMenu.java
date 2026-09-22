package com.pedro.racasclasses.client.gui;

import com.pedro.racasclasses.attribute.Attribute;
import com.pedro.racasclasses.attribute.AttributeRegistry;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * Textos da HUD de atributos (sem lógica de rede).
 */
public final class AttributeMenu {
    private AttributeMenu() {}

    public static String bar(int level) {
        int filled = Math.round(level * 10f / AttributeRegistry.MAX_STAT_LEVEL);
        filled = Math.max(0, Math.min(10, filled));
        return "[" + "█".repeat(filled) + "░".repeat(10 - filled) + "]";
    }

    public static String rowLabel(Attribute attribute) {
        return switch (attribute) {
            case STR -> "STR  Força";
            case DEX -> "DEX  Destreza";
            case CON -> "CON  Constituição";
            case INT -> "INT  Inteligência";
            case WIS -> "WIS  Sabedoria";
            case LUCK -> "LUCK Sorte";
        };
    }

    /** Tooltip ao passar o mouse no nome da trilha (pagina 1). */
    public static List<Component> tooltip(Attribute attribute) {
        return switch (attribute) {
            case STR -> List.of(
                    Component.literal("STRENGTH"),
                    Component.literal("Dano corpo a corpo: +1% por nível."),
                    Component.literal("A cada 5 níveis: +2% knockback."),
                    Component.literal("Máximo (50): +50% dano, +20% knockback.")
            );
            case DEX -> List.of(
                    Component.literal("DEXTERITY"),
                    Component.literal("Dano à distância: +1% por nível."),
                    Component.literal("Velocidade: +0.001 por nível."),
                    Component.literal("A cada 5 níveis: +1% esquiva."),
                    Component.literal("Máximo (50): +50% dano, +0.05 vel, +10% esquiva.")
            );
            case CON -> List.of(
                    Component.literal("CONSTITUTION"),
                    Component.literal("HP: +0.25 coração por nível (+0.5 HP)."),
                    Component.literal("A cada 5 níveis: +0.5 armadura."),
                    Component.literal("Máximo (50): +12.5 corações, +5 armadura.")
            );
            case INT -> List.of(
                    Component.literal("INTELLIGENCE"),
                    Component.literal("Duração de efeitos: +1% por nível."),
                    Component.literal("A cada 10 níveis: +5% XP vanilla."),
                    Component.literal("Máximo (50): +50% duração, +25% XP.")
            );
            case WIS -> List.of(
                    Component.literal("WISDOM"),
                    Component.literal("Cura recebida: +1% por nível."),
                    Component.literal("A cada 5 níveis: +2% regeneração."),
                    Component.literal("A cada 10 níveis: +1 coração ao comer."),
                    Component.literal("Máximo (50): +50% cura, +20% regen, +5 corações.")
            );
            case LUCK -> List.of(
                    Component.literal("LUCK"),
                    Component.literal("Drop de minério: +0.5% por nível."),
                    Component.literal("A cada 5 níveis: +0.5% drop de mob."),
                    Component.literal("A cada 10 níveis: -5% preço de villager."),
                    Component.literal("Máximo (50): +25% minério, +10% mob, -25% villager.")
            );
        };
    }

    /** Pagina 3: guia. Linhas curtas cabem no pergaminho sem scroll. */
    public static List<String> explanationLines() {
        return List.of(
                "Cada trilha vai de 0 a 50. 1 ponto = 1 nível.",
                "Personagem sobe até o nível 120; depois disso não ganha mais pontos.",
                "Os bônus raciais já entram como níveis (ex.: Anão CON 2).",
                "Raças sem ASI de PHB ganham 3 pontos livres nesta HUD.",
                "",
                "STR  dano melee + knockback a cada 5",
                "DEX  dano ranged, velocidade, esquiva a cada 5",
                "CON  HP (+0.25 coração) e armadura a cada 5",
                "INT  duração de poções; XP vanilla a cada 10",
                "WIS  cura, regen a cada 5, absorção ao comer a cada 10",
                "LUCK minério, drop de mob a cada 5, villager a cada 10",
                "",
                "Como ganhar XP desta barra (não é a barra verde):",
                "Mob +10   Minério +5   Villager +3   Pesca +2   Craft +1",
                "Próximo nível: nível atual x 100 XP  (nv.1 = 100, nv.50 = 5000, nv.120 = 12000).",
                "",
                "Exemplo: 3 pontos em CON = +0.75 coração de HP máximo.",
                "Passe o mouse no nome da trilha (página 1) para o detalhe."
        );
    }
}
