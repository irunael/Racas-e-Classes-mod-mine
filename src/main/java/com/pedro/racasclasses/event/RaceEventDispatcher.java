package com.pedro.racasclasses.event;

import com.pedro.racasclasses.RacasClasses;
import com.pedro.racasclasses.capability.ModAttachments;
import com.pedro.racasclasses.capability.PlayerRaceData;
import com.pedro.racasclasses.race.Race;
import com.pedro.racasclasses.race.RaceRegistry;
import com.pedro.racasclasses.race.impl.GnomeRace;
import com.pedro.racasclasses.race.impl.GoliathRace;
import com.pedro.racasclasses.race.impl.TabaxiRace;
import com.pedro.racasclasses.race.impl.FirbolgRace;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import com.pedro.racasclasses.race.impl.SatyrRace;
import com.pedro.racasclasses.race.impl.LeoninRace;
import com.pedro.racasclasses.race.impl.TortleRace;
import com.pedro.racasclasses.race.impl.QuachoRace;
import com.pedro.racasclasses.race.impl.HarengonRace;
import com.pedro.racasclasses.race.impl.GithyankiRace;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;


import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber(modid = RacasClasses.MODID)
public class RaceEventDispatcher {

    // ===== HURT (quem toma dano) =====

    @SubscribeEvent
    public static void onPlayerHurt(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        PlayerRaceData data = player.getData(ModAttachments.PLAYER_RACE);
        Race race = RaceRegistry.get(data.getRaceId());
        if (race == null) return;

        race.onPlayerHurt(player, event);
    }

    // ===== ATTACK (quem causa dano — backstab) =====

    @SubscribeEvent
    public static void onAttackEntity(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer attacker)) return;
        if (event.getEntity() == attacker) return;

        PlayerRaceData data = attacker.getData(ModAttachments.PLAYER_RACE);
        Race race = RaceRegistry.get(data.getRaceId());
        if (race == null) return;

        race.onAttackEntity(attacker, event);
    }

    // ===== RANGED (bônus de arco) =====

    @SubscribeEvent
    public static void onRangedDamage(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getDirectEntity() instanceof net.minecraft.world.entity.projectile.AbstractArrow)) return;
        if (!(event.getSource().getEntity() instanceof ServerPlayer shooter)) return;

        PlayerRaceData data = shooter.getData(ModAttachments.PLAYER_RACE);
        Race race = RaceRegistry.get(data.getRaceId());
        if (race == null) return;

        race.onRangedDamage(shooter, event);
    }

    // ===== BLOCK BREAK =====

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;

        PlayerRaceData data = player.getData(ModAttachments.PLAYER_RACE);
        Race race = RaceRegistry.get(data.getRaceId());
        if (race == null) return;

        race.onBlockBreak(player, event);
    }

    // ===== XP DROP =====

    @SubscribeEvent
    public static void onXpDrop(LivingExperienceDropEvent event) {
        if (!(event.getAttackingPlayer() instanceof ServerPlayer player)) return;

        PlayerRaceData data = player.getData(ModAttachments.PLAYER_RACE);
        Race race = RaceRegistry.get(data.getRaceId());
        if (race == null) return;

        race.onXpDrop(player, event);
    }

    // ===== FALL (Rock Gnome) =====

    @SubscribeEvent
    public static void onFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        PlayerRaceData data = player.getData(ModAttachments.PLAYER_RACE);
        Race race = RaceRegistry.get(data.getRaceId());

        if (race != null) {
            race.onFall(player, event);
        }
    }

    // ===== MOB TARGET =====

    @SubscribeEvent
    public static void onMobTarget(LivingChangeTargetEvent event) {
        if (!(event.getNewAboutToBeSetTarget() instanceof ServerPlayer player)) return;

        PlayerRaceData data = player.getData(ModAttachments.PLAYER_RACE);
        Race race = RaceRegistry.get(data.getRaceId());
        if (race == null) return;

        race.onMobTarget(player, event);
    }

    // ===== CRITICAL HIT =====

    @SubscribeEvent
    public static void onCriticalHit(CriticalHitEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!event.isCriticalHit()) return;

        PlayerRaceData data = player.getData(ModAttachments.PLAYER_RACE);
        Race race = RaceRegistry.get(data.getRaceId());
        if (race == null) return;

        race.onCriticalHit(player, event);
    }
    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        PlayerRaceData data = player.getData(ModAttachments.PLAYER_RACE);
        Race race = RaceRegistry.get(data.getRaceId());
        if (race == null) return;

        // Chama hook geral (usado por Vampire e outros)
        race.onEntityInteract(player, event);

        // Firbolg específico
        if (race instanceof FirbolgRace firbolg) {
            firbolg.onEntityInteract(player, event);
        }
    }
    @SubscribeEvent
    public static void onItemUse(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        PlayerRaceData data = player.getData(ModAttachments.PLAYER_RACE);
        Race race = RaceRegistry.get(data.getRaceId());
        if (race == null) return;

        // Chama hook geral (usado por Vampire e outros)
        race.onItemUse(player, event);

        // Leonin específico
        if (race instanceof LeoninRace leonin) {
            leonin.onItemUse(player, event);
        }
    }
    
    @SubscribeEvent
    public static void onEquipmentChange(net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        PlayerRaceData data = player.getData(ModAttachments.PLAYER_RACE);
        Race race = RaceRegistry.get(data.getRaceId());

        if (race instanceof TortleRace tortle) {
            tortle.onEquipmentChange(player, event);
        }
    }
    @SubscribeEvent
    public static void onEffectApplicable(net.neoforged.neoforge.event.entity.living.MobEffectEvent.Applicable event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        PlayerRaceData data = player.getData(ModAttachments.PLAYER_RACE);
        Race race = RaceRegistry.get(data.getRaceId());

        if (race instanceof QuachoRace quacho) {
            quacho.onEffectApplicable(player, event);
        }
    }
    // ===== MOB DROPS (Harengon) =====

    @SubscribeEvent
    public static void onMobDrops(net.neoforged.neoforge.event.entity.living.LivingDropsEvent event) {
        if (!(event.getEntity().level() instanceof net.minecraft.server.level.ServerLevel)) return;

        var source = event.getSource();
        if (!(source.getEntity() instanceof ServerPlayer player)) return;

        PlayerRaceData data = player.getData(ModAttachments.PLAYER_RACE);
        Race race = RaceRegistry.get(data.getRaceId());
        if (race == null) return;

        race.onMobDrops(player, event.getEntity(), event);
    }

    // ===== VILLAGER TRADE (Kenku) =====

    @SubscribeEvent
    public static void onVillagerTrade(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof net.minecraft.world.entity.npc.Villager)) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        PlayerRaceData data = player.getData(ModAttachments.PLAYER_RACE);
        Race race = RaceRegistry.get(data.getRaceId());
        if (race == null) return;

        if (race.blockVillagerTrade(player)) {
            event.setCanceled(true);
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§cO villager não entende seus sons..."));
        }
    }
    
    // ===== TELEPORT (Githyanki) =====

    @SubscribeEvent
    public static void onTeleport(EntityTeleportEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        PlayerRaceData data = player.getData(ModAttachments.PLAYER_RACE);
        Race race = RaceRegistry.get(data.getRaceId());
        if (race == null) return;

        if (race instanceof GithyankiRace gith) {
            gith.onTeleport(player, event);
        }
    }
    
    // ===== CHAT (Changeling escolha de player) =====
    
    @SubscribeEvent
    public static void onChat(net.neoforged.neoforge.event.ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        PlayerRaceData data = player.getData(ModAttachments.PLAYER_RACE);
        
        if (!"changeling".equals(data.getRaceId())) return;
        if (!player.getPersistentData().contains("changeling_choosing")) return;
        
        String message = event.getMessage().getString();
        
        try {
            int choice = Integer.parseInt(message.trim());
            String playerList = player.getPersistentData().getString("changeling_player_list");
            String[] players = playerList.split(",");
            
            if (choice < 1 || choice > players.length) {
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§cNúmero inválido! Escolha entre 1 e " + players.length));
                return;
            }
            
            String targetName = players[choice - 1];
            com.pedro.racasclasses.race.impl.ChangelingRace.applyDisguise(player, targetName);
            
            event.setCanceled(true); // Cancela a mensagem no chat
        } catch (NumberFormatException ignored) {
            // Não é número, ignora
        }
    }

}
