package com.pedro.racasclasses.race.impl;

import com.pedro.racasclasses.capability.ModAttachments;
import com.pedro.racasclasses.capability.PlayerRaceData;
import com.pedro.racasclasses.race.Race;
import com.pedro.racasclasses.race.RacialWeakness;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

public class DragonbornRace implements Race {

    @Override
    public String getId() { return "dragonborn"; }

    @Override
    public String getDisplayName() { return "Draconato"; }

    @Override public int getRacialStrength() { return 2; }
    @Override public int getRacialLuck() { return 1; }
    @Override public int getFreeAttributePoints() { return 0; }

    @Override
    public double getMaxHealth() { return 22.0; }

    @Override
    public double getAttackDamage() { return 1.2; }

    @Override
    public double getArmor() { return 2.0; }

    @Override
    public boolean hasNightVision() { return true; }

    // ===== Sub-raça =====

    @Override
    public boolean hasSubrace() { return true; }

    @Override
    public String[] getSubraceIds() {
        return new String[]{
                "red", "gold", "brass",
                "white", "silver",
                "blue", "bronze",
                "black", "copper",
                "green"
        };
    }

    @Override
    public void onSubraceChosen(ServerPlayer player, String subraceId) {
        PlayerRaceData data = player.getData(ModAttachments.PLAYER_RACE);
        data.setDragonbornSubrace(subraceId);
    }

    @Override
    public String getSubraceDisplayName(ServerPlayer player) {
        PlayerRaceData data = player.getData(ModAttachments.PLAYER_RACE);
        return data.getDragonbornSubrace();
    }

    // ===== Elemento da sub-raça =====

    /** Retorna o elemento (fire, ice, lightning, poison, acid) */
    public static String getElement(String subrace) {
        return switch (subrace) {
            case "red", "gold", "brass" -> "fire";
            case "white", "silver" -> "ice";
            case "blue", "bronze" -> "lightning";
            case "black", "copper" -> "acid";
            case "green" -> "poison";
            default -> "fire";
        };
    }

    // ===== Resistência elemental =====

    @Override
    public void onPlayerHurt(ServerPlayer player, LivingIncomingDamageEvent event) {
        PlayerRaceData data = player.getData(ModAttachments.PLAYER_RACE);
        String element = getElement(data.getDragonbornSubrace());
        DamageSource source = event.getSource();

        boolean immune = false;
        boolean resist = false;

        switch (element) {
            case "fire" -> {
                if (source.is(DamageTypes.IN_FIRE)
                        || source.is(DamageTypes.ON_FIRE)
                        || source.is(DamageTypes.LAVA)
                        || source.is(DamageTypes.HOT_FLOOR)) {
                    resist = true;
                }
            }
            case "ice" -> {
                if (source.is(DamageTypes.FREEZE)) resist = true;
            }
            case "lightning" -> {
                if (source.is(DamageTypes.LIGHTNING_BOLT)) resist = true;
            }
            case "poison" -> {
                if (player.hasEffect(MobEffects.POISON)
                        && (source.is(DamageTypes.MAGIC) || source.getMsgId().equals("magic"))) {
                    immune = true;
                }
            }
            case "acid" -> {
                if (source.is(DamageTypes.MAGIC) || source.getMsgId().equals("magic")) {
                    immune = true;
                }
            }
        }

        if (immune) {
            event.setAmount(0f);
            return;
        }
        if (resist) {
            event.setAmount(event.getAmount() * 0.25f);
            return;
        }

        // Elemento oposto: fogo↔poison, gelo↔fogo, raio↔fogo, ácido↔raio
        boolean isOpposite = switch (element) {
            case "fire" -> RacialWeakness.isPoisonMagic(player, source);
            case "ice" -> RacialWeakness.isFire(source);
            case "poison" -> RacialWeakness.isFreeze(source);
            case "lightning" -> RacialWeakness.isFire(source);
            case "acid" -> RacialWeakness.isLightning(source);
            default -> false;
        };
        if (isOpposite) {
            RacialWeakness.scale(event, 1.5f);
        }
    }
}
