package com.pedro.racasclasses.race.impl;
import com.pedro.racasclasses.race.Race;
import com.pedro.racasclasses.race.RaceElytra;
import com.pedro.racasclasses.race.RacialWeakness;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

public class HadozeeRace implements Race {
    private static final Map<UUID,Long> ABILITY_COOLDOWNS=new HashMap<>();
    private static final String GLIDE_UNTIL="hadozee_glide_until";
    
    @Override public String getId(){return "hadozee";}
    @Override public String getDisplayName(){return "Hadozee";}
    @Override public double getMaxHealth(){return 22;}
    @Override public double getMovementSpeed(){return .11;}
    @Override public double getScale(){return 1.05;}
    @Override public boolean hasNightVision(){return true;}
    
    @Override public void onPlayerTick(ServerPlayer p){
        // Planador: Slow Falling quando caindo
        if(!p.onGround() && !p.isFallFlying()) {
            p.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING,40,0,false,false,false));
        }
        
        // Escalada melhorada: SHIFT + olhando para bloco sólido
        if(p.isShiftKeyDown() && !p.onGround()) {
            // Pega direção que o player está olhando
            Vec3 lookVec = p.getLookAngle();
            Direction lookDir = Direction.getNearest(lookVec.x, 0, lookVec.z);
            
            // Checa bloco na frente (1 bloco de distância)
            BlockPos frontPos = p.blockPosition().relative(lookDir);
            BlockState frontBlock = p.level().getBlockState(frontPos);
            
            // Se tiver bloco sólido na frente E olhando para ele
            if (!frontBlock.isAir() && frontBlock.isSolid()) {
                // Verifica se está realmente olhando para frente (ângulo < 60°)
                double lookAngle = Math.toDegrees(Math.acos(lookVec.y));
                if (lookAngle > 30 && lookAngle < 150) { // Não tá olhando muito pra cima ou pra baixo
                    // Sobe como uma escada
                    Vec3 motion = p.getDeltaMovement();
                    p.setDeltaMovement(motion.x, 0.3, motion.z); // Velocidade de escalada
                    p.hurtMarked = true;
                    p.fallDistance = 0; // Reseta dano de queda
                }
            }
        }
        
        // Remove elytra temporária quando tempo acabar
        long until=p.getPersistentData().getLong(GLIDE_UNTIL);
        if(until>0 && p.serverLevel().getServer().getTickCount()>=until){
            if(RaceElytra.isRaceElytra(p.getItemBySlot(EquipmentSlot.CHEST)))
                p.setItemSlot(EquipmentSlot.CHEST,ItemStack.EMPTY);
            p.getPersistentData().remove(GLIDE_UNTIL);
        }
    }
    
    @Override public boolean canUseAbility(){return true;}
    
    @Override public void executeAbility(ServerPlayer p){
        long t=p.serverLevel().getServer().getTickCount();
        Long r=ABILITY_COOLDOWNS.get(p.getUUID());
        if(r!=null&&t<r){
            p.sendSystemMessage(Component.literal("§cGlide em recarga: §f"+(r-t)/20.0+"s"));
            return;
        }
        
        if(p.getItemBySlot(EquipmentSlot.CHEST).isEmpty()){
            p.setItemSlot(EquipmentSlot.CHEST,RaceElytra.create(p));
            p.getPersistentData().putLong(GLIDE_UNTIL,t+600);
        }
        
        p.startFallFlying();
        ABILITY_COOLDOWNS.put(p.getUUID(),t+1200);
        p.sendSystemMessage(Component.literal("§aGlide ativado!"));
    }

    // ===== Fraqueza: +30% dano perfurante =====
    @Override
    public void onPlayerHurt(ServerPlayer p, LivingIncomingDamageEvent event) {
        RacialWeakness.applyPiercing(event, 1.3f);
    }
}
