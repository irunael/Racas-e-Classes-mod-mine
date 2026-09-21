package com.pedro.racasclasses.client;

import com.pedro.racasclasses.RacasClasses;
import com.pedro.racasclasses.entity.ModEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = RacasClasses.MODID, value = Dist.CLIENT)
public final class ClientEntityRenderers {
    @SubscribeEvent
    public static void register(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.ILLUSION_CLONE.get(), IllusionCloneRenderer::new);
    }
}
