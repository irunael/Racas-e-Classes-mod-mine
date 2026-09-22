package com.pedro.racasclasses.attribute;

import com.pedro.racasclasses.RacasClasses;

/**
 * Constantes do sistema de atributos. Não há registro dinâmico:
 * as seis trilhas estão no enum {@link Attribute}.
 */
public final class AttributeRegistry {
    public static final int MAX_STAT_LEVEL = 50;
    /** 6 trilhas × 50 = teto de pontos úteis; o nível de personagem para aqui. */
    public static final int MAX_CHARACTER_LEVEL = MAX_STAT_LEVEL * Attribute.values().length;

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
