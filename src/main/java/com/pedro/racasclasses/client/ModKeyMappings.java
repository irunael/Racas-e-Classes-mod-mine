package com.pedro.racasclasses.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.pedro.racasclasses.RacasClasses;

import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = RacasClasses.MODID, value = Dist.CLIENT)
public class ModKeyMappings {

    public static final KeyMapping SUPER_JUMP = new KeyMapping(
            "key.racasclasses.super_jump",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "key.categories.racasclasses"
    );

    public static final KeyMapping HEAL = new KeyMapping(
            "key.racasclasses.heal",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            "key.categories.racasclasses"
    );

    public static final KeyMapping CLASS_PRIMARY = new KeyMapping(
            "key.racasclasses.class_primary", InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_C, "key.categories.racasclasses"
    );

    public static final KeyMapping CLASS_SECONDARY = new KeyMapping(
            "key.racasclasses.class_secondary", InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_Z, "key.categories.racasclasses"
    );

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(SUPER_JUMP);
        event.register(HEAL);
        event.register(CLASS_PRIMARY);
        event.register(CLASS_SECONDARY);
    }
}
