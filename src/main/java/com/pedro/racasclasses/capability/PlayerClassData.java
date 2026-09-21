package com.pedro.racasclasses.capability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class PlayerClassData {
    private String classId = "none";
    private String subclassId = "none";

    public PlayerClassData() {}

    public PlayerClassData(String classId, String subclassId) {
        this.classId = classId;
        this.subclassId = subclassId;
    }

    public static final Codec<PlayerClassData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("class").orElse("none").forGetter(PlayerClassData::getClassId),
            Codec.STRING.fieldOf("subclass").orElse("none").forGetter(PlayerClassData::getSubclassId)
    ).apply(instance, PlayerClassData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerClassData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, PlayerClassData::getClassId,
            ByteBufCodecs.STRING_UTF8, PlayerClassData::getSubclassId,
            PlayerClassData::new
    );

    public String getClassId() { return classId; }
    public void setClassId(String classId) { this.classId = classId; }
    public String getSubclassId() { return subclassId; }
    public void setSubclassId(String subclassId) { this.subclassId = subclassId; }
}
