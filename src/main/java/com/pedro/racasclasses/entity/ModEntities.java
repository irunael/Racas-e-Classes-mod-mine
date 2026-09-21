package com.pedro.racasclasses.entity;

import com.pedro.racasclasses.RacasClasses;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, RacasClasses.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<IllusionCloneEntity>> ILLUSION_CLONE =
            TYPES.register("illusion_clone", () -> EntityType.Builder.<IllusionCloneEntity>of(
                    IllusionCloneEntity::new, MobCategory.MISC)
                    .sized(0.6F, 1.8F).clientTrackingRange(10).updateInterval(1)
                    .build("illusion_clone"));

    private ModEntities() {}
    public static void register(IEventBus bus) { TYPES.register(bus); }
}
