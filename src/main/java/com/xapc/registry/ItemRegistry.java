package com.xapc.registry;

import com.xapc.Xapc;
//import com.xapc.weapons.shootgun4;
import com.xapc.weapons.granade;
import com.xapc.weapons.snootgun4;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier; // Используем то, что видим в дереве
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

import java.util.function.Function;

public class ItemRegistry {
    public static final snootgun4 SHOOTGUN4 = ItemRegistry.register("shootgun4", snootgun4::new, new Item.Properties());
    public static final granade GRANADE = ItemRegistry.register("granade", granade::new, new Item.Properties());

    public static <T extends Item> T register(String name, Function<Item.Properties, T> itemFactory, Item.Properties settings) {
        // Create the item key.

        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Xapc.MOD_ID, name));

        // Create the item instance.
        T item = itemFactory.apply(settings.setId(itemKey));

        // Register the item.
        Registry.register(BuiltInRegistries.ITEM, itemKey, item);

        return item;
    }
    public static void initialize() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS)
           .register((creativeTab) -> creativeTab.accept(SHOOTGUN4));
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS)
            .register((creativeTab) -> creativeTab.accept(GRANADE));
    }
}