package com.pedro.racasclasses.client;

import com.pedro.racasclasses.RacasClasses;
import com.pedro.racasclasses.capability.ModAttachments;
import com.pedro.racasclasses.client.gui.RaceSelectionScreen;
import com.pedro.racasclasses.network.AbilityPayload;
import com.pedro.racasclasses.network.SuperJumpPayload;
import com.pedro.racasclasses.network.ClassPrimaryPayload;
import com.pedro.racasclasses.network.ClassSecondaryPayload;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = RacasClasses.MODID, value = Dist.CLIENT)
public class ClientEvents {

    private static boolean selectionPromptShown;
    private static int promptDelay;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            selectionPromptShown = false;
            promptDelay = 0;
            return;
        }

        boolean needsCharacterCreation = player.getData(ModAttachments.PLAYER_RACE).getRaceId().equals("none")
                || player.getData(ModAttachments.PLAYER_CLASS).getClassId().equals("none");
        if (!selectionPromptShown && needsCharacterCreation) {
            if (++promptDelay >= 20 && Minecraft.getInstance().screen == null) {
                selectionPromptShown = true;
                Minecraft.getInstance().setScreen(new RaceSelectionScreen());
            }
        }

        while (ModKeyMappings.SUPER_JUMP.consumeClick()) {
            PacketDistributor.sendToServer(new SuperJumpPayload());
        }

        while (ModKeyMappings.HEAL.consumeClick()) {
            PacketDistributor.sendToServer(new AbilityPayload());
        }

        while (ModKeyMappings.CLASS_PRIMARY.consumeClick()) {
            PacketDistributor.sendToServer(new ClassPrimaryPayload());
        }

        while (ModKeyMappings.CLASS_SECONDARY.consumeClick()) {
            PacketDistributor.sendToServer(new ClassSecondaryPayload());
        }
    }
}
