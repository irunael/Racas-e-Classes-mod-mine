package com.pedro.racasclasses.capability;

import com.pedro.racasclasses.RacasClasses;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, RacasClasses.MODID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerRaceData>> PLAYER_RACE =
            ATTACHMENT_TYPES.register("player_race",
                    () -> AttachmentType.builder(PlayerRaceData::new)
                            .serialize(PlayerRaceData.CODEC)
                            .sync(PlayerRaceData.STREAM_CODEC)
                            .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerClassData>> PLAYER_CLASS =
            ATTACHMENT_TYPES.register("player_class",
                    () -> AttachmentType.builder(PlayerClassData::new)
                            .serialize(PlayerClassData.CODEC)
                            .sync(PlayerClassData.STREAM_CODEC)
                            .build());

    public static void register(IEventBus bus) {
        ATTACHMENT_TYPES.register(bus);
    }
}
