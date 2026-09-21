package com.pedro.racasclasses.race.impl;

import com.pedro.racasclasses.capability.ModAttachments;
import com.pedro.racasclasses.capability.PlayerRaceData;
import com.pedro.racasclasses.race.Race;

import net.minecraft.server.level.ServerPlayer;

public class GnomeRace implements Race {

    @Override
    public String getId() { return "gnome"; }

    @Override
    public String getDisplayName() { return "Gnomo"; }

    @Override
    public double getMovementSpeed() { return 0.09; }

    @Override
    public double getScale() { return 0.60; }

    @Override
    public boolean hasNightVision() { return true; }

    // ===== Sub-raça =====

    @Override
    public boolean hasSubrace() { return true; }

    @Override
    public String[] getSubraceIds() {
        return new String[]{"forest", "rock"};
    }

    @Override
    public void onSubraceChosen(ServerPlayer player, String subraceId) {
        PlayerRaceData data = player.getData(ModAttachments.PLAYER_RACE);
        data.setGnomeSubrace(subraceId);
    }

    @Override
    public String getSubraceDisplayName(ServerPlayer player) {
        PlayerRaceData data = player.getData(ModAttachments.PLAYER_RACE);
        String id = data.getGnomeSubrace();
        return switch (id) {
            case "forest" -> "Gnomo da Floresta";
            case "rock" -> "Gnomo da Rocha";
            default -> id;
        };
    }

    /** Imunidade a queda do Rock Gnome (3 blocos) */
    public void onFall(ServerPlayer player, net.neoforged.neoforge.event.entity.living.LivingFallEvent event) {
        float distance = event.getDistance();
        if (distance <= 3.0f) {
            event.setCanceled(true);
        } else {
            event.setDistance(distance - 3.0f);
        }
    }

    /** Mobs passivos/lobos/aranhas não atacam Forest Gnome */
    @Override
    public void onMobTarget(ServerPlayer player, net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent event) {
        PlayerRaceData data = player.getData(ModAttachments.PLAYER_RACE);
        if (!data.getGnomeSubrace().equals("forest")) return;

        if (!(event.getEntity() instanceof net.minecraft.world.entity.Mob mob)) return;
        net.minecraft.world.entity.Entity target = event.getNewAboutToBeSetTarget();
        if (!(target instanceof ServerPlayer p)) return;
        if (p != player) return;

        if (mob instanceof net.minecraft.world.entity.animal.Animal
                || mob instanceof net.minecraft.world.entity.animal.Wolf
                || mob instanceof net.minecraft.world.entity.monster.Spider) {
            event.setCanceled(true);
        }
    }
}
