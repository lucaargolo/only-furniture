package dev.lucaargolo.furniture;

import dev.lucaargolo.furniture.attachment.ModAttachmentManager;
import dev.lucaargolo.furniture.attachment.ModDataAttachments;
import dev.lucaargolo.furniture.block.ModBlocks;
import dev.lucaargolo.furniture.block.entity.ModBlockEntities;
import dev.lucaargolo.furniture.entity.ModEntityTypes;
import dev.lucaargolo.furniture.item.ModCreativeTabs;
import dev.lucaargolo.furniture.item.ModItems;
import dev.lucaargolo.furniture.network.ModPacketManager;
import dev.lucaargolo.furniture.registry.*;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unchecked")
public abstract class FurnitureMod {

    public static final String MOD_ID = "onlyfurniture";
    public static final String MOD_NAME = "Only Furniture";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

    public static FurnitureMod INSTANCE;

    private final ModPacketManager packetManager = loadPlatformClass(ModPacketManager.class);
    private final ModAttachmentManager attachmentManager = loadPlatformClass(ModAttachmentManager.class);

    public final void init() {
        INSTANCE = this;
        ModBlocks.REGISTRY.init();
        ModBlockEntities.REGISTRY.init();
        ModItems.REGISTRY.init();
        ModCreativeTabs.REGISTRY.init();
        ModEntityTypes.REGISTRY.init();
        ModDataAttachments.REGISTRY.init();
        this.packetManager.init();
    }

    public abstract String getPlatform();

    public abstract boolean isFakePlayer(Player player);

    public abstract Block getPottedBlock(Block block);

    public final ModPacketManager getPacketManager() {
        return packetManager;
    }

    public ModAttachmentManager getAttachmentManager() {
        return attachmentManager;
    }

    public final <T> ModRegistry<T> registry(ResourceKey<Registry<T>> registryKey) {
        return loadPlatformClass(ModRegistry.class, registryKey);
    }

    public final ModBlockRegistry blockRegistry() {
        return loadPlatformClass(ModBlockRegistry.class);
    }

    public final ModBlockEntityRegistry blockEntityRegistry() {
        return loadPlatformClass(ModBlockEntityRegistry.class);
    }

    public final ModItemRegistry itemRegistry() {
        return loadPlatformClass(ModItemRegistry.class);
    }

    public final ModAttachmentRegistry<?> attachmentRegistry() {
        return loadPlatformClass(ModAttachmentRegistry.class);
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
