package com.pedro.racasclasses.client;

import com.pedro.racasclasses.client.gui.AttributeScreen;
import com.pedro.racasclasses.network.OpenAttributeScreenS2CPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Handlers de payload que só existem no cliente (não carregar no dedicated server). */
public final class ClientPayloadHandler {
    private ClientPayloadHandler() {}

    public static void handleOpenAttributes(OpenAttributeScreenS2CPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> Minecraft.getInstance().setScreen(new AttributeScreen()));
    }
}
