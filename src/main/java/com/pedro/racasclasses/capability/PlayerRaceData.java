package com.pedro.racasclasses.capability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class PlayerRaceData {

    private String raceId = "none";
    private String dragonbornSubrace = "red";
    private String elfSubrace = "wood";
    private String gnomeSubrace = "forest";
    private String halflingSubrace = "lightfoot";
    private String tieflingSubrace = "asmodeus";

    public PlayerRaceData() {}

    public PlayerRaceData(String raceId, String dragonbornSubrace, String elfSubrace,
                          String gnomeSubrace, String halflingSubrace, String tieflingSubrace) {
        this.raceId = raceId;
        this.dragonbornSubrace = dragonbornSubrace;
        this.elfSubrace = elfSubrace;
        this.gnomeSubrace = gnomeSubrace;
        this.halflingSubrace = halflingSubrace;
        this.tieflingSubrace = tieflingSubrace;
    }

    public static final Codec<PlayerRaceData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("race").orElse("none").forGetter(PlayerRaceData::getRaceId),
                    Codec.STRING.fieldOf("dragonborn_subrace").orElse("red").forGetter(PlayerRaceData::getDragonbornSubrace),
                    Codec.STRING.fieldOf("elf_subrace").orElse("wood").forGetter(PlayerRaceData::getElfSubrace),
                    Codec.STRING.fieldOf("gnome_subrace").orElse("forest").forGetter(PlayerRaceData::getGnomeSubrace),
                    Codec.STRING.fieldOf("halfling_subrace").orElse("lightfoot").forGetter(PlayerRaceData::getHalflingSubrace),
                    Codec.STRING.fieldOf("tiefling_subrace").orElse("asmodeus").forGetter(PlayerRaceData::getTieflingSubrace)
            ).apply(instance, PlayerRaceData::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerRaceData> STREAM_CODEC =
            StreamCodec.composite(
                    net.minecraft.network.codec.ByteBufCodecs.STRING_UTF8, PlayerRaceData::getRaceId,
                    net.minecraft.network.codec.ByteBufCodecs.STRING_UTF8, PlayerRaceData::getDragonbornSubrace,
                    net.minecraft.network.codec.ByteBufCodecs.STRING_UTF8, PlayerRaceData::getElfSubrace,
                    net.minecraft.network.codec.ByteBufCodecs.STRING_UTF8, PlayerRaceData::getGnomeSubrace,
                    net.minecraft.network.codec.ByteBufCodecs.STRING_UTF8, PlayerRaceData::getHalflingSubrace,
                    net.minecraft.network.codec.ByteBufCodecs.STRING_UTF8, PlayerRaceData::getTieflingSubrace,
                    PlayerRaceData::new
            );

    public String getRaceId() { return raceId; }
    public void setRaceId(String raceId) { this.raceId = raceId; }

    public String getDragonbornSubrace() { return dragonbornSubrace; }
    public void setDragonbornSubrace(String id) { this.dragonbornSubrace = id; }

    public String getElfSubrace() { return elfSubrace; }
    public void setElfSubrace(String id) { this.elfSubrace = id; }

    public String getGnomeSubrace() { return gnomeSubrace; }
    public void setGnomeSubrace(String id) { this.gnomeSubrace = id; }

    public String getHalflingSubrace() { return halflingSubrace; }
    public void setHalflingSubrace(String id) { this.halflingSubrace = id; }

    public String getTieflingSubrace() { return tieflingSubrace; }
    public void setTieflingSubrace(String id) { this.tieflingSubrace = id; }
}