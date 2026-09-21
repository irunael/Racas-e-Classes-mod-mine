package com.pedro.racasclasses.race;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.item.enchantment.Enchantments;

public final class RaceElytra {

    private static final String NAME = "Asas raciais";

    private RaceElytra() {}

    public static ItemStack create(ServerPlayer player) {
        ItemStack elytra = new ItemStack(Items.ELYTRA);
        elytra.set(DataComponents.UNBREAKABLE, new Unbreakable(true));
        elytra.set(DataComponents.CUSTOM_NAME, Component.literal(NAME));

        var enchantments = player.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        elytra.enchant(enchantments.getHolderOrThrow(Enchantments.BINDING_CURSE), 1);
        return elytra;
    }

    public static boolean isRaceElytra(ItemStack stack) {
        Component customName = stack.get(DataComponents.CUSTOM_NAME);
        return stack.is(Items.ELYTRA) && customName != null && customName.getString().equals(NAME);
    }
}
