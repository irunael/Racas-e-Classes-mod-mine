package com.pedro.racasclasses.network;

import com.pedro.racasclasses.RacasClasses;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ClassPrimaryPayload() implements CustomPacketPayload {
    public static final Type<ClassPrimaryPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(RacasClasses.MODID, "class_primary"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClassPrimaryPayload> STREAM_CODEC =
            StreamCodec.unit(new ClassPrimaryPayload());
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
