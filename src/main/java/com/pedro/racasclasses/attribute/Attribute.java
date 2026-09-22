package com.pedro.racasclasses.attribute;

/**
 * Trilhas de atributo do personagem (independentes dos Attributes vanilla).
 * Cada uma vai de 0 a {@link AttributeRegistry#MAX_STAT_LEVEL}.
 */
public enum Attribute {
    STR("str", "Strength", "Força"),
    DEX("dex", "Dexterity", "Destreza"),
    CON("con", "Constitution", "Constituição"),
    INT("int", "Intelligence", "Inteligência"),
    WIS("wis", "Wisdom", "Sabedoria"),
    LUCK("luck", "Luck", "Sorte");

    private final String id;
    private final String englishName;
    private final String displayName;

    Attribute(String id, String englishName, String displayName) {
        this.id = id;
        this.englishName = englishName;
        this.displayName = displayName;
    }

    public String getId() { return id; }
    public String getEnglishName() { return englishName; }
    public String getDisplayName() { return displayName; }

    public static Attribute fromId(String id) {
        if (id == null) return null;
        for (Attribute attribute : values()) {
            if (attribute.id.equalsIgnoreCase(id) || attribute.name().equalsIgnoreCase(id)) {
                return attribute;
            }
        }
        return null;
    }
}
