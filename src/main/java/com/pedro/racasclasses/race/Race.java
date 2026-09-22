package com.pedro.racasclasses.race;

import com.pedro.racasclasses.attribute.AttributeData;
import com.pedro.racasclasses.capability.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

public interface Race {

    String getId();
    String getDisplayName();

    // ===== Atributos base =====

    default double getMaxHealth() { return 20.0; }
    default double getSubraceHealthBonus(ServerPlayer player) { return 0.0; }
    default double getAttackDamage() { return 1.0; }
    default double getMovementSpeed() { return 0.10; }
    default double getArmor() { return 0.0; }
    default double getScale() { return 1.0; }
    default boolean hasNightVision() { return false; }
    default double getPoisonResistance() { return 0.0; }
    default double getMiningSpeedBonus() { return 0.0; }
    default double getSwimSpeedBonus() { return 0.0; }

    // ===== Bônus de atributo (D&D 5e → STR/DEX/CON/INT/WIS/LUCK) =====
    // CHA vira LUCK. Cada ponto racial já entra como nível da trilha.
    // ASI default 0; pontos livres default 3 (raças PHB com ASI definido retornam 0).

    default int getRacialStrength() { return 0; }
    default int getRacialDexterity() { return 0; }
    default int getRacialConstitution() { return 0; }
    default int getRacialIntelligence() { return 0; }
    default int getRacialWisdom() { return 0; }
    default int getRacialLuck() { return 0; }

    /** Pontos pra distribuir na HUD (tecla P). PHB com ASI definido deve retornar 0. */
    default int getFreeAttributePoints() { return 3; }

    /**
     * Aplica o ASI base e os pontos livres. Sub-raça soma depois, em {@link #onSubraceChosen}.
     * Os 6 stats são SET (não ADD) pra reescolher raça não empilhar bônus.
     * Pontos disponíveis = (nível de personagem − 1) + pontos livres da raça,
     * devolvendo os pontos ganhos por level-up pra redistribuir.
     */
    default void applyInitialAttributes(AttributeData data) {
        data.setStrength(getRacialStrength());
        data.setDexterity(getRacialDexterity());
        data.setConstitution(getRacialConstitution());
        data.setIntelligence(getRacialIntelligence());
        data.setWisdom(getRacialWisdom());
        data.setLuck(getRacialLuck());
        int fromLevels = Math.max(0, data.getCharacterLevel() - 1);
        data.setAvailablePoints(fromLevels + getFreeAttributePoints());
    }

    default void applyInitialAttributes(ServerPlayer player) {
        applyInitialAttributes(player.getData(ModAttachments.PLAYER_ATTRIBUTES));
    }

    // ===== Sub-raça =====

    default boolean hasSubrace() { return false; }
    default String[] getSubraceIds() { return new String[0]; }
    default void onSubraceChosen(ServerPlayer player, String subraceId) {}
    default String getSubraceDisplayName(ServerPlayer player) { return ""; }

    // ===== Entrada/Saída =====

    default void onRaceEnter(ServerPlayer player) {}
    default void onRaceExit(ServerPlayer player) {}
    default void onPlayerRespawn(ServerPlayer player) {}

    // ===== Super Jump (tecla R) =====

    default boolean canSuperJump() { return false; }
    default void executeSuperJump(ServerPlayer player) {}

    // ===== Habilidade ativa (tecla H) =====

    default boolean canUseAbility() { return false; }
    default void executeAbility(ServerPlayer player) {}

    // ===== Drops de mob (Harengon) =====

    default void onMobDrops(ServerPlayer player,
                            net.minecraft.world.entity.LivingEntity killed,
                            net.neoforged.neoforge.event.entity.living.LivingDropsEvent event) {}

    // ===== Villager trade (Kenku) =====

    default boolean blockVillagerTrade(ServerPlayer player) { return false; }

    // ===== Hooks dinâmicos =====

    default void onPlayerTick(ServerPlayer player) {}
    default void onPlayerJoin(ServerPlayer player) {}
    default void onPlayerHurt(ServerPlayer player, LivingIncomingDamageEvent event) {}
    default void onRangedDamage(ServerPlayer player, LivingIncomingDamageEvent event) {}
    default void onBlockBreak(ServerPlayer player, BlockEvent.BreakEvent event) {}
    default void onXpDrop(ServerPlayer player, LivingExperienceDropEvent event) {}
    default void onCriticalHit(ServerPlayer player, CriticalHitEvent event) {}
    default void onMobTarget(ServerPlayer player, LivingChangeTargetEvent event) {}
    default void onAttackEntity(ServerPlayer player, LivingIncomingDamageEvent event) {}
    
    // ===== Interação com items (Vampire - bloquear comida) =====
    
    default void onItemUse(ServerPlayer player, net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickItem event) {}
    
    // ===== Interação com entidades (Vampire - drenar animais) =====
    
    default void onEntityInteract(ServerPlayer player, net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.EntityInteract event) {}
    
    // ===== Teleporte (Githyanki) =====
    
    default void onTeleport(ServerPlayer player, EntityTeleportEvent event) {}
    
    // ===== Queda (Harengon, Goliath) =====
    
    default void onFall(ServerPlayer player, LivingFallEvent event) {}
}