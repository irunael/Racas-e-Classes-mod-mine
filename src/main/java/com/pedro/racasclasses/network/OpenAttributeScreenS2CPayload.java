package com.pedro.racasclasses.network;

import com.pedro.racasclasses.RacasClasses;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Servidor → cliente: attachment já foi sincronizado, pode abrir a HUD.
 * Tipo separado do C2S — o NeoForge não registra o mesmo payload nos dois sentidos.
 */
public record OpenAttributeScreenS2CPayload() implements CustomPacketPayload {
    public static final Type<OpenAttributeScreenS2CPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(RacasClasses.MODID, "open_attributes_s2c"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenAttributeScreenS2CPayload> STREAM_CODEC =
            StreamCodec.unit(new OpenAttributeScreenS2CPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
