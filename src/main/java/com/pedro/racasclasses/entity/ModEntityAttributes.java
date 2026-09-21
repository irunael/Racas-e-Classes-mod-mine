package com.pedro.racasclasses.entity;

import com.pedro.racasclasses.RacasClasses;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@EventBusSubscriber(modid = RacasClasses.MODID)
public final class ModEntityAttributes {
    @SubscribeEvent
    public static void register(EntityAttributeCreationEvent event) {
        event.put(ModEntities.ILLUSION_CLONE.get(),
                net.minecraft.world.entity.Mob.createMobAttributes()
                        .add(Attributes.MAX_HEALTH, 1.0)
                        .add(Attributes.MOVEMENT_SPEED, 0.28)
                        .add(Attributes.FOLLOW_RANGE, 16.0)
                        .build());
    }
}
