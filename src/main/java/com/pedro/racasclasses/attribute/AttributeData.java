package com.pedro.racasclasses.attribute;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * Progresso de atributos do jogador (nível de personagem + 6 trilhas).
 * Persistido e sincronizado via attachment, igual a {@code PlayerRaceData}.
 */
public class AttributeData {
    private int characterLevel = 1;
    private int characterXp = 0;
    private int availablePoints = 0;

    private int strengthLevel = 0;
    private int dexterityLevel = 0;
    private int constitutionLevel = 0;
    private int intelligenceLevel = 0;
    private int wisdomLevel = 0;
    private int luckLevel = 0;

    public AttributeData() {}

    public AttributeData(int characterLevel, int characterXp, int availablePoints,
                         int strengthLevel, int dexterityLevel, int constitutionLevel,
                         int intelligenceLevel, int wisdomLevel, int luckLevel) {
        this.characterLevel = characterLevel;
        this.characterXp = characterXp;
        this.availablePoints = availablePoints;
        this.strengthLevel = clampStat(strengthLevel);
        this.dexterityLevel = clampStat(dexterityLevel);
        this.constitutionLevel = clampStat(constitutionLevel);
        this.intelligenceLevel = clampStat(intelligenceLevel);
        this.wisdomLevel = clampStat(wisdomLevel);
        this.luckLevel = clampStat(luckLevel);
    }

    public static final Codec<AttributeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("character_level", 1).forGetter(AttributeData::getCharacterLevel),
            Codec.INT.optionalFieldOf("character_xp", 0).forGetter(AttributeData::getCharacterXp),
            Codec.INT.optionalFieldOf("available_points", 0).forGetter(AttributeData::getAvailablePoints),
            Codec.INT.optionalFieldOf("str", 0).forGetter(AttributeData::getStrengthLevel),
            Codec.INT.optionalFieldOf("dex", 0).forGetter(AttributeData::getDexterityLevel),
            Codec.INT.optionalFieldOf("con", 0).forGetter(AttributeData::getConstitutionLevel),
            Codec.INT.optionalFieldOf("int", 0).forGetter(AttributeData::getIntelligenceLevel),
            Codec.INT.optionalFieldOf("wis", 0).forGetter(AttributeData::getWisdomLevel),
            Codec.INT.optionalFieldOf("luck", 0).forGetter(AttributeData::getLuckLevel)
    ).apply(instance, AttributeData::new));

    // StreamCodec.composite só cobre até 6 campos; aqui são 9 ints.
    public static final StreamCodec<RegistryFriendlyByteBuf, AttributeData> STREAM_CODEC = StreamCodec.of(
            (buf, data) -> {
                buf.writeVarInt(data.characterLevel);
                buf.writeVarInt(data.characterXp);
                buf.writeVarInt(data.availablePoints);
                buf.writeVarInt(data.strengthLevel);
                buf.writeVarInt(data.dexterityLevel);
                buf.writeVarInt(data.constitutionLevel);
                buf.writeVarInt(data.intelligenceLevel);
                buf.writeVarInt(data.wisdomLevel);
                buf.writeVarInt(data.luckLevel);
            },
            buf -> new AttributeData(
                    buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
                    buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
                    buf.readVarInt(), buf.readVarInt(), buf.readVarInt())
    );

    public int getCharacterLevel() { return characterLevel; }
    public int getCharacterXp() { return characterXp; }
    public int getAvailablePoints() { return availablePoints; }

    public int getStrengthLevel() { return strengthLevel; }
    public int getDexterityLevel() { return dexterityLevel; }
    public int getConstitutionLevel() { return constitutionLevel; }
    public int getIntelligenceLevel() { return intelligenceLevel; }
    public int getWisdomLevel() { return wisdomLevel; }
    public int getLuckLevel() { return luckLevel; }

    public int getLevel(Attribute attribute) {
        return switch (attribute) {
            case STR -> strengthLevel;
            case DEX -> dexterityLevel;
            case CON -> constitutionLevel;
            case INT -> intelligenceLevel;
            case WIS -> wisdomLevel;
            case LUCK -> luckLevel;
        };
    }

    public void copyFrom(AttributeData other) {
        this.characterLevel = other.characterLevel;
        this.characterXp = other.characterXp;
        this.availablePoints = other.availablePoints;
        this.strengthLevel = other.strengthLevel;
        this.dexterityLevel = other.dexterityLevel;
        this.constitutionLevel = other.constitutionLevel;
        this.intelligenceLevel = other.intelligenceLevel;
        this.wisdomLevel = other.wisdomLevel;
        this.luckLevel = other.luckLevel;
    }

    /**
     * Soma XP da barra própria e sobe de nível (1 ponto por nível).
     * @return quantos níveis foram ganhos nesta somatória
     */
    public int addXp(int amount) {
        if (amount <= 0) return 0;
        characterXp += amount;
        int gained = 0;
        while (characterLevel < AttributeRegistry.MAX_CHARACTER_LEVEL) {
            int needed = AttributeRegistry.xpToNextLevel(characterLevel);
            if (characterXp < needed) break;
            characterXp -= needed;
            characterLevel++;
            availablePoints++;
            gained++;
        }
        if (characterLevel >= AttributeRegistry.MAX_CHARACTER_LEVEL) {
            characterXp = 0;
        }
        return gained;
    }

    /** Gasta 1 ponto na trilha. Retorna false se não houver ponto ou se já estiver no cap. */
    public boolean tryDistribute(Attribute attribute) {
        if (availablePoints <= 0) return false;
        if (getLevel(attribute) >= AttributeRegistry.MAX_STAT_LEVEL) return false;
        availablePoints--;
        switch (attribute) {
            case STR -> strengthLevel++;
            case DEX -> dexterityLevel++;
            case CON -> constitutionLevel++;
            case INT -> intelligenceLevel++;
            case WIS -> wisdomLevel++;
            case LUCK -> luckLevel++;
        }
        return true;
    }

    private static int clampStat(int value) {
        return Math.max(0, Math.min(AttributeRegistry.MAX_STAT_LEVEL, value));
    }
}
