package com.pedro.racasclasses.race.impl;

import com.pedro.racasclasses.RacasClasses;
import com.pedro.racasclasses.capability.ModAttachments;
import com.pedro.racasclasses.capability.PlayerRaceData;
import com.pedro.racasclasses.race.Race;
import com.pedro.racasclasses.race.RacialWeakness;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

public class TieflingRace implements Race {

    private static final float HELLISH_RESISTANCE = 0.75f;
    private static final float ZARIEL_PHYSICAL_RESISTANCE = 0.50f;

    @Override
    public String getId() { return "tiefling"; }

    @Override
    public String getDisplayName() { return "Tiefling"; }

    @Override public int getRacialIntelligence() { return 1; }
    @Override public int getRacialLuck() { return 2; }
    @Override public int getFreeAttributePoints() { return 0; }

    @Override
    public boolean hasNightVision() { return true; }

    // ===== Sub-raça =====

    @Override
    public boolean hasSubrace() { return true; }

    @Override
    public String[] getSubraceIds() {
        return new String[]{"asmodeus", "levistus", "zariel"};
    }

    @Override
    public void onSubraceChosen(ServerPlayer player, String subraceId) {
        PlayerRaceData data = player.getData(ModAttachments.PLAYER_RACE);
        data.setTieflingSubrace(subraceId);
    }

    @Override
    public String getSubraceDisplayName(ServerPlayer player) {
        PlayerRaceData data = player.getData(ModAttachments.PLAYER_RACE);
        return switch (data.getTieflingSubrace()) {
            case "asmodeus" -> "Sangue de Asmodeus";
            case "levistus" -> "Sangue de Levistus";
            case "zariel" -> "Sangue de Zariel";
            default -> data.getTieflingSubrace();
        };
    }

    // ===== Tick (Levistus: imunidade a Slowness) =====

    @Override
    public void onPlayerTick(ServerPlayer player) {
        if (player.tickCount % 20 != 0) return;

        PlayerRaceData data = player.getData(ModAttachments.PLAYER_RACE);

        if (data.getTieflingSubrace().equals("levistus")) {
            player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        }
    }

    // ===== Resistência a dano =====

    @Override
    public void onPlayerHurt(ServerPlayer player, LivingIncomingDamageEvent event) {
        PlayerRaceData data = player.getData(ModAttachments.PLAYER_RACE);
        String subrace = data.getTieflingSubrace();
        var source = event.getSource();

        // --- FOGO (base: 75% de redução) ---
        boolean isFire =
                source.is(DamageTypes.IN_FIRE)
                        || source.is(DamageTypes.ON_FIRE)
                        || source.is(DamageTypes.LAVA)
                        || source.is(DamageTypes.HOT_FLOOR);

        if (isFire) {
            if (subrace.equals("asmodeus")) {
                // Asmodeus: imunidade total a fogo/lava
                event.setAmount(0f);
                RacasClasses.LOGGER.info("[TIEFLING-ASMODEUS] Imune a fogo!");
            } else {
                // Base: 75% de redução
                event.setAmount(event.getAmount() * (1.0f - HELLISH_RESISTANCE));
            }
            return;
        }

        // --- ZARIEL: 50% de redução a dano físico (armas corpo a corpo) ---
        if (subrace.equals("zariel")) {
            boolean isPhysical =
                    source.is(DamageTypes.PLAYER_ATTACK)
                            || source.is(DamageTypes.MOB_ATTACK);

            if (isPhysical) {
                event.setAmount(event.getAmount() * (1.0f - ZARIEL_PHYSICAL_RESISTANCE));
            }
        }

        // Fraqueza: dano sagrado mapeado pra MAGIC (+30%)
        RacialWeakness.applyMagic(event, 1.3f);
    }

    // ===== Ataque mão vazia (efeito na sub-raça) =====

    @Override
    public void onAttackEntity(ServerPlayer player, LivingIncomingDamageEvent event) {
        // Só mão vazia
        if (!player.getMainHandItem().isEmpty()) return;

        if (!(event.getEntity() instanceof LivingEntity target)) return;

        PlayerRaceData data = player.getData(ModAttachments.PLAYER_RACE);
        String subrace = data.getTieflingSubrace();

        switch (subrace) {
            case "asmodeus" -> {
                target.setRemainingFireTicks(40); // 2s
                RacasClasses.LOGGER.info("[TIEFLING] Asmodeus aplicou fogo!");
            }
            case "levistus" -> {
                target.addEffect(new MobEffectInstance(
                        MobEffects.MOVEMENT_SLOWDOWN, 60, 0, false, false, false)); // 3s
                RacasClasses.LOGGER.info("[TIEFLING] Levistus aplicou slowness!");
            }
            case "zariel" -> {
                target.addEffect(new MobEffectInstance(
                        MobEffects.WEAKNESS, 60, 0, false, false, false)); // 3s
                RacasClasses.LOGGER.info("[TIEFLING] Zariel aplicou weakness!");
            }
        }
    }
}