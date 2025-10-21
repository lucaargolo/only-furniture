package dev.lucaargolo.furniture;

import dev.lucaargolo.furniture.block.ModBlocks;
import dev.lucaargolo.furniture.item.ModItems;
import dev.lucaargolo.furniture.utils.ModRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unchecked")
public abstract class FurnitureMod {

    public static final String MOD_ID = "furniture";
    public static final String MOD_NAME = "Furniture Mod";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

    public static FurnitureMod INSTANCE;

    private final PlatformHelper platformHelper = loadPlatformClass(PlatformHelper.class);

    public void init() {
        INSTANCE = this;
        ModBlocks.BLOCKS.init();
        ModItems.ITEMS.init();
    }

    public abstract String getPlatform();

    public PlatformHelper getPlatformHelper() {
        return platformHelper;
    }

    public <T> ModRegistry<T> registry(ResourceKey<Registry<T>> registryKey) {
        return loadPlatformClass(ModRegistry.class, registryKey);
    }

    public <T> T loadPlatformClass(Class<T> clazz, Object... parameters) {
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
