package com.pedro.racasclasses.race.impl;
import com.pedro.racasclasses.race.Race; 
import java.util.*; 
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component; 
import net.minecraft.server.level.ServerPlayer; 
import net.minecraft.world.effect.*; 
import net.minecraft.world.entity.Entity; 
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.food.FoodData;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public class VampireRace implements Race {
    private static final Map<UUID,Long> ABILITY_COOLDOWNS=new HashMap<>();
    private static final String LAST="vampire_last";
    private static final String DRAIN_TARGET="vampire_drain_target";
    private static final String DRAIN_START="vampire_drain_start";
    private static final String FOOD_DRAINED="vampire_food_drained";
    
    @Override public String getId(){return "vampire";}
    @Override public String getDisplayName(){return "Vampire";}
    @Override public double getAttackDamage(){return 1.1;}
    @Override public double getMovementSpeed(){return .11;}
    @Override public boolean hasNightVision(){return true;}
    
    // ===== Ataque com mão vazia: só +2 dano (SEM CURA) =====
    @Override public void onAttackEntity(ServerPlayer p,LivingIncomingDamageEvent e){
        p.getPersistentData().putUUID(LAST,e.getEntity().getUUID());
        if(p.getMainHandItem().isEmpty()){
            e.setAmount(e.getAmount()+2.0f);
        }
    }
    
    // ===== Bloquear comida =====
    public void onItemUse(ServerPlayer player, PlayerInteractEvent.RightClickItem event) {
        if (event.getItemStack().has(net.minecraft.core.component.DataComponents.FOOD)) {
            event.setCanceled(true);
            player.sendSystemMessage(Component.literal("§cVampiros não podem comer comida normal!"));
        }
    }
    
    // ===== Fraqueza no sol + dano + NAUSEA MÁXIMA =====
    @Override public void onPlayerTick(ServerPlayer p){
        // Sol com Nausea nível máximo
        if(p.tickCount % 20 == 0 && p.level().isDay() && p.level().canSeeSky(p.blockPosition())){
            p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,40,0,false,false,false));
            p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,40,0,false,false,false));
            p.addEffect(new MobEffectInstance(MobEffects.CONFUSION,40,99,false,false,false)); // NÍVEL 100 (máximo)
            p.hurt(p.damageSources().onFire(), 1.0f);
        }
        
        // Fome 20% mais lenta - usar addExhaustion negativo
        // A cada 5 segundos (100 ticks), adiciona -0.1 de exhaustion
        // Isso compensa ~20% do gasto normal de exhaustion
        if (p.tickCount % 100 == 0) {
            p.getFoodData().addExhaustion(-0.1f);
        }
        
        // Drenagem com distância de 5 blocos
        if (!p.getPersistentData().contains(DRAIN_TARGET)) return;
        
        UUID targetId = p.getPersistentData().getUUID(DRAIN_TARGET);
        long startTick = p.getPersistentData().getLong(DRAIN_START);
        long currentTick = p.serverLevel().getServer().getTickCount();
        int foodDrained = p.getPersistentData().getInt(FOOD_DRAINED);
        
        Entity entity = p.serverLevel().getEntity(targetId);
        if (!(entity instanceof LivingEntity target) || !target.isAlive()) {
            stopDraining(p);
            return;
        }
        
        // Distância: 5 blocos
        if (p.distanceTo(target) > 5.0) {
            p.sendSystemMessage(Component.literal("§7Muito longe do animal."));
            stopDraining(p);
            return;
        }
        
        if (!p.isShiftKeyDown()) {
            p.sendSystemMessage(Component.literal("§7Você parou de drenar."));
            stopDraining(p);
            return;
        }
        
        if (currentTick - startTick >= 100) {
            p.sendSystemMessage(Component.literal("§aDrenagem completa!"));
            stopDraining(p);
            return;
        }
        
        // Drena a cada 10 ticks (0.5s)
        if ((currentTick - startTick) % 10 == 0) {
            FoodData food = p.getFoodData();
            
            if (food.getFoodLevel() >= 20) {
                p.sendSystemMessage(Component.literal("§aVocê está saciado!"));
                stopDraining(p);
                return;
            }
            
            // Restaura 2 pontos de fome
            food.setFoodLevel(Math.min(20, food.getFoodLevel() + 2));
            food.eat(0, 1.0f);
            
            // Conta quantos pontos de fome foram drenados
            foodDrained += 2;
            p.getPersistentData().putInt(FOOD_DRAINED, foodDrained);
            
            // A cada 1 ponto de fome (meia barra), cura meio coração (1 HP)
            if (foodDrained % 1 == 0) {
                p.heal(1.0f); // Meio coração
                
                // Efeito visual de cura
                p.serverLevel().sendParticles(
                    ParticleTypes.HEART,
                    p.getX(),
                    p.getY() + 1.5,
                    p.getZ(),
                    1, 0.3, 0.3, 0.3, 0.1
                );
            }
            
            // Dano no animal
            target.hurt(p.damageSources().magic(), 0.5f);
            
            // Partículas de sangue no animal
            p.serverLevel().sendParticles(
                ParticleTypes.DAMAGE_INDICATOR,
                target.getX(),
                target.getY() + target.getBbHeight() * 0.5,
                target.getZ(),
                5, 0.3, 0.3, 0.3, 0.1
            );
        }
    }
    
    private void stopDraining(ServerPlayer player) {
        player.getPersistentData().remove(DRAIN_TARGET);
        player.getPersistentData().remove(DRAIN_START);
        player.getPersistentData().remove(FOOD_DRAINED);
    }
    
    // ===== Interação com animais (botão direito) =====
    public void onEntityInteract(ServerPlayer player, PlayerInteractEvent.EntityInteract event) {
        Entity target = event.getTarget();
        
        if (!(target instanceof Animal || target instanceof WaterAnimal 
                || target instanceof Squid || target instanceof Dolphin)) {
            return;
        }
        
        LivingEntity livingTarget = (LivingEntity) target;
        
        if (player.getPersistentData().contains(DRAIN_TARGET)) {
            return;
        }
        
        if (player.getFoodData().getFoodLevel() >= 20) {
            player.sendSystemMessage(Component.literal("§cVocê já está saciado!"));
            return;
        }
        
        long tick = player.serverLevel().getServer().getTickCount();
        player.getPersistentData().putUUID(DRAIN_TARGET, livingTarget.getUUID());
        player.getPersistentData().putLong(DRAIN_START, tick);
        player.getPersistentData().putInt(FOOD_DRAINED, 0);
        
        player.sendSystemMessage(Component.literal("§cVocê começa a drenar a vitalidade do animal..."));
        player.sendSystemMessage(Component.literal("§7Segure SHIFT para continuar drenando!"));
        player.sendSystemMessage(Component.literal("§7Alcance: 5 blocos"));
        
        event.setCanceled(true);
    }
    
    // ===== Blood Drain (H) - CURA 3 CORAÇÕES =====
    @Override public boolean canUseAbility(){return true;}
    
    @Override public void executeAbility(ServerPlayer p){
        long t=p.serverLevel().getServer().getTickCount();
        Long r=ABILITY_COOLDOWNS.get(p.getUUID());
        if(r!=null&&t<r){
            p.sendSystemMessage(Component.literal("§cBlood Drain em recarga: §f"+(r-t)/20.0+"s"));
            return;
        }
        
        if(!p.getPersistentData().hasUUID(LAST)){
            p.sendSystemMessage(Component.literal("§cAtaque um mob primeiro."));
            return;
        }
        
        Entity entity=p.serverLevel().getEntity(p.getPersistentData().getUUID(LAST));
        if(!(entity instanceof LivingEntity target)||!target.isAlive()){
            p.sendSystemMessage(Component.literal("§cNenhum alvo válido."));
            return;
        }
        
        target.addEffect(new MobEffectInstance(MobEffects.WITHER,100,1));
        p.heal(6.0f); // CURA 3 CORAÇÕES
        
        // Partículas de cura no player
        p.serverLevel().sendParticles(
            ParticleTypes.HEART,
            p.getX(),
            p.getY() + 1.5,
            p.getZ(),
            10, 0.5, 0.5, 0.5, 0.2
        );
        
        ABILITY_COOLDOWNS.put(p.getUUID(),t+800);
        p.sendSystemMessage(Component.literal("§aBlood Drain! (+3 ❤)"));
    }
}
