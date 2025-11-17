package dev.lucaargolo.furniture.menu;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.registry.ModMenuTypeRegistry;
import dev.lucaargolo.furniture.registry.minecraft.MinecraftEntry;
import net.minecraft.world.inventory.MenuType;

public class ModMenuTypes {

    public static final ModMenuTypeRegistry REGISTRY = FurnitureMod.menuTypeRegistry();

    public static final MinecraftEntry<MenuType<StorageMenu>> STORAGE = REGISTRY.register("storage", StorageMenu::new);

}
