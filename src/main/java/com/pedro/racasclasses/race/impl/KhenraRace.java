package com.pedro.racasclasses.race.impl;
import com.pedro.racasclasses.race.Race;
import com.pedro.racasclasses.race.RacialWeakness;
import java.util.*;
import net.minecraft.network.chat.Component; import net.minecraft.server.level.ServerPlayer; import net.minecraft.world.effect.*; import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
public class KhenraRace implements Race { private static final Map<UUID,Long> ABILITY_COOLDOWNS=new HashMap<>(); private static final String LAST="khenra_last", LAST_TICK="khenra_tick";
 @Override public String getId(){return "khenra";} @Override public String getDisplayName(){return "Khenra";} @Override public double getMaxHealth(){return 22;} @Override public double getAttackDamage(){return 1.1;} @Override public double getMovementSpeed(){return .11;} @Override public boolean hasNightVision(){return true;}
 @Override public void onPlayerTick(ServerPlayer p){if(p.tickCount%20==0)p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,40,0,false,false,false));}
 @Override public void onAttackEntity(ServerPlayer p,LivingIncomingDamageEvent e){var tag=p.getPersistentData();long tick=p.serverLevel().getServer().getTickCount();UUID id=e.getEntity().getUUID();if(tag.hasUUID(LAST)&&tag.getUUID(LAST).equals(id)&&tick-tag.getLong(LAST_TICK)<=100)e.setAmount(e.getAmount()*1.2f);tag.putUUID(LAST,id);tag.putLong(LAST_TICK,tick);}
 @Override public void onPlayerHurt(ServerPlayer p,LivingIncomingDamageEvent e){RacialWeakness.applyFire(e,1.5f);}
 @Override public boolean canUseAbility(){return true;} @Override public void executeAbility(ServerPlayer p){long t=p.serverLevel().getServer().getTickCount();Long r=ABILITY_COOLDOWNS.get(p.getUUID());if(r!=null&&t<r){p.sendSystemMessage(Component.literal("§cTwin Fury em recarga: §f"+(r-t)/20.0+"s"));return;}p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,200,1));p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,200,0));ABILITY_COOLDOWNS.put(p.getUUID(),t+900);p.sendSystemMessage(Component.literal("§aTwin Fury ativado!"));}
}
