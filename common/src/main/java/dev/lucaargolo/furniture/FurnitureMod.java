package dev.lucaargolo.furniture;

import dev.lucaargolo.furniture.block.ModBlocks;
import dev.lucaargolo.furniture.entity.ModEntityTypes;
import dev.lucaargolo.furniture.item.ModCreativeTabs;
import dev.lucaargolo.furniture.item.ModItems;
import dev.lucaargolo.furniture.network.ModPacketManager;
import dev.lucaargolo.furniture.registry.ModBlockRegistry;
import dev.lucaargolo.furniture.registry.ModItemRegistry;
import dev.lucaargolo.furniture.registry.ModRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unchecked")
public abstract class FurnitureMod {

    public static final String MOD_ID = "onlyfurniture";
    public static final String MOD_NAME = "Only Furniture";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

    public static FurnitureMod INSTANCE;

    private final FurnitureDataManager dataManager = loadPlatformClass(FurnitureDataManager.class);
    private final ModPacketManager packetManager = loadPlatformClass(ModPacketManager.class);

    public final void init() {
        INSTANCE = this;
        ModBlocks.REGISTRY.init();
        ModItems.REGISTRY.init();
        ModCreativeTabs.REGISTRY.init();
        ModEntityTypes.REGISTRY.init();
        this.dataManager.init();
        this.packetManager.init();
    }

    public abstract boolean isFakePlayer(Player player);

    public abstract String getPlatform();

    public FurnitureDataManager getDataManager() {
        return dataManager;
    }

    public final ModPacketManager getPacketManager() {
        return packetManager;
    }

    public final <T> ModRegistry<T> registry(ResourceKey<Registry<T>> registryKey) {
        return loadPlatformClass(ModRegistry.class, registryKey);
    }

    public final ModBlockRegistry blockRegistry() {
        return loadPlatformClass(ModBlockRegistry.class);
    }

    public final ModItemRegistry itemRegistry() {
        return loadPlatformClass(ModItemRegistry.class);
    }

    public final <T> T loadPlatformClass(Class<T> clazz, Object... parameters) {
        String name = clazz.getName();
        String platformName = name.substring(0, name.lastIndexOf('.')) + "." + getPlatform() + name.substring(name.lastIndexOf('.') + 1);
        Class<?>[] parameterTypes = new Class<?>[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            parameterTypes[i] = parameters[i].getClass();
        }
        try {
            return (T) clazz.getClassLoader().loadClass(platformName).getConstructor(parameterTypes).newInstance(parameters);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }


}
