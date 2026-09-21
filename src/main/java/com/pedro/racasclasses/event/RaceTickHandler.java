package com.pedro.racasclasses.event;

import com.pedro.racasclasses.RacasClasses;
import com.pedro.racasclasses.capability.ModAttachments;
import com.pedro.racasclasses.capability.PlayerRaceData;
import com.pedro.racasclasses.race.Race;
import com.pedro.racasclasses.race.RaceRegistry;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = RacasClasses.MODID)
public class RaceTickHandler {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        // Roda a cada 2 ticks (10x por segundo) — mais responsivo
        if (player.tickCount % 2 != 0) return;

        PlayerRaceData data = player.getData(ModAttachments.PLAYER_RACE);
        Race race = RaceRegistry.get(data.getRaceId());
        if (race == null) return;

        race.onPlayerTick(player);
    }
}