package dev.lucaargolo.furniture.menu;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.registry.ModMenuTypeRegistry;

public class ModMenuTypes {

    public static final ModMenuTypeRegistry REGISTRY = FurnitureMod.menuTypeRegistry();

    public static final ModMenuTypeRegistry.AdvancedMenuTypeEntry<StorageMenu, StorageMenu.Definition> STORAGE = REGISTRY.register("storage", StorageMenu::new, StorageMenu.Definition.STREAM_CODEC);

}
