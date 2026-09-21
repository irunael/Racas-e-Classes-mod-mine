package com.pedro.racasclasses.network;

import com.pedro.racasclasses.RacasClasses;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ChooseRacePayload(String raceId, String subraceId, String classId, String subclassId)
        implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ChooseRacePayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(
                    RacasClasses.MODID, "choose_race"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ChooseRacePayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8,
                    ChooseRacePayload::raceId,
                    ByteBufCodecs.STRING_UTF8,
                    ChooseRacePayload::subraceId,
                    ByteBufCodecs.STRING_UTF8,
                    ChooseRacePayload::classId,
                    ByteBufCodecs.STRING_UTF8,
                    ChooseRacePayload::subclassId,
                    ChooseRacePayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
