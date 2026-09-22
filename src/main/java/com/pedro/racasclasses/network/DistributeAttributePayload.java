package com.pedro.racasclasses.network;

import com.pedro.racasclasses.RacasClasses;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Cliente gasta 1 ponto na trilha indicada (id: str, dex, con, int, wis, luck). */
public record DistributeAttributePayload(String attributeId) implements CustomPacketPayload {
    public static final Type<DistributeAttributePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(RacasClasses.MODID, "distribute_attribute"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DistributeAttributePayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.STRING_UTF8, DistributeAttributePayload::attributeId, DistributeAttributePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
