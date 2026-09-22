package com.pedro.racasclasses.network;

import com.pedro.racasclasses.RacasClasses;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Cliente pede a HUD; o servidor sincroniza o attachment e manda o mesmo tipo de volta. */
public record OpenAttributeScreenPayload() implements CustomPacketPayload {
    public static final Type<OpenAttributeScreenPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(RacasClasses.MODID, "open_attributes"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenAttributeScreenPayload> STREAM_CODEC =
            StreamCodec.unit(new OpenAttributeScreenPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
