package com.pedro.racasclasses.race.impl;
import com.pedro.racasclasses.race.Race;
import com.pedro.racasclasses.race.RacialWeakness;
import java.util.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
public class KenderRace implements Race {
 private static final Map<UUID,Long> ABILITY_COOLDOWNS=new HashMap<>();
 @Override public String getId(){return "kender";} @Override public String getDisplayName(){return "Kender";}
 @Override public double getMovementSpeed(){return .11;} @Override public double getScale(){return .85;}
 @Override public void onPlayerTick(ServerPlayer p){if(p.tickCount%20==0){RacialWeakness.fasterHunger(p,0.25f);}if(p.isShiftKeyDown()) p.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 40,0,false,false,false));}
 @Override public void onMobDrops(ServerPlayer p, LivingEntity killed, LivingDropsEvent event){if(p.getRandom().nextFloat()>=.15f||event.getDrops().isEmpty())return; ItemEntity source=new ArrayList<>(event.getDrops()).get(p.getRandom().nextInt(event.getDrops().size())); ItemStack copy=source.getItem().copy();copy.setCount(1);event.getDrops().add(new ItemEntity((ServerLevel)killed.level(),killed.getX(),killed.getY(),killed.getZ(),copy));}
 @Override public boolean canUseAbility(){return true;} @Override public void executeAbility(ServerPlayer p){long t=p.serverLevel().getServer().getTickCount();Long r=ABILITY_COOLDOWNS.get(p.getUUID());if(r!=null&&t<r){p.sendSystemMessage(Component.literal("§cTaunt em recarga: §f"+(r-t)/20.0+"s"));return;} int[] count={0};for(Monster mob:p.level().getEntitiesOfClass(Monster.class,p.getBoundingBox().inflate(10))){mob.setTarget(p);count[0]++;}ABILITY_COOLDOWNS.put(p.getUUID(),t+600);p.sendSystemMessage(Component.literal("§aTaunt! §7("+count[0]+" mobs provocados)"));}
}
