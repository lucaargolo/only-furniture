package dev.lucaargolo.furniture.menu;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.registry.ModMenuTypeRegistry;
import net.minecraft.network.codec.ByteBufCodecs;

public class ModMenuTypes {

    public static final ModMenuTypeRegistry REGISTRY = FurnitureMod.menuTypeRegistry();

    public static final ModMenuTypeRegistry.AdvancedMenuTypeEntry<StorageMenu, Integer> STORAGE = REGISTRY.register("storage", StorageMenu::new, ByteBufCodecs.INT);

}
