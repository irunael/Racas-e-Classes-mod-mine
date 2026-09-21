package com.pedro.racasclasses.network;

import com.pedro.racasclasses.RacasClasses;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SuperJumpPayload() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SuperJumpPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(RacasClasses.MODID, "super_jump"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SuperJumpPayload> STREAM_CODEC =
            StreamCodec.unit(new SuperJumpPayload());

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}