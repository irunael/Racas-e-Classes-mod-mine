package com.pedro.racasclasses.attribute;

import com.pedro.racasclasses.RacasClasses;

/**
 * Constantes do sistema de atributos. Não há registro dinâmico:
 * as seis trilhas estão no enum {@link Attribute}.
 */
public final class AttributeRegistry {
    public static final int MAX_STAT_LEVEL = 50;
    /** Teto de nível de personagem: ao chegar aqui, para de ganhar XP e pontos. */
    public static final int MAX_CHARACTER_LEVEL = 120;

    public static final int XP_KILL = 10;
    public static final int XP_ORE = 5;
    public static final int XP_TRADE = 3;
    public static final int XP_FISH = 2;
    public static final int XP_CRAFT = 1;

    private AttributeRegistry() {}

    public static void init() {
        RacasClasses.LOGGER.info("Sistema de atributos: {} trilhas, cap {} por trilha.",
                Attribute.values().length, MAX_STAT_LEVEL);
    }

    /** XP para ir do nível atual para o próximo: nível × 100. */
    public static int xpToNextLevel(int characterLevel) {
        return Math.max(1, characterLevel) * 100;
    }
}
