package com.pedro.racasclasses.network;

import com.pedro.racasclasses.RacasClasses;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ClassSecondaryPayload() implements CustomPacketPayload {
    public static final Type<ClassSecondaryPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(RacasClasses.MODID, "class_secondary"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClassSecondaryPayload> STREAM_CODEC =
            StreamCodec.unit(new ClassSecondaryPayload());
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
