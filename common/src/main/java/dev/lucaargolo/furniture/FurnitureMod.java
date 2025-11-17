package dev.lucaargolo.furniture;

import dev.lucaargolo.furniture.attachment.ModAttachmentManager;
import dev.lucaargolo.furniture.attachment.ModDataAttachments;
import dev.lucaargolo.furniture.block.ModBlocks;
import dev.lucaargolo.furniture.block.entity.ModBlockEntityTypes;
import dev.lucaargolo.furniture.entity.ModEntityTypes;
import dev.lucaargolo.furniture.item.ModCreativeTabs;
import dev.lucaargolo.furniture.item.ModItems;
import dev.lucaargolo.furniture.menu.ModMenuTypes;
import dev.lucaargolo.furniture.network.BlockChangedPayload;
import dev.lucaargolo.furniture.network.ModPacketManager;
import dev.lucaargolo.furniture.registry.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiFunction;

@SuppressWarnings("unchecked")
public abstract class FurnitureMod {

    public static final String MOD_ID = "onlyfurniture";
    public static final String MOD_NAME = "Only Furniture";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

    private static FurnitureMod instance;

    private final ModPacketManager packetManager;
    private final ModAttachmentManager attachmentManager;

    public FurnitureMod() {
        instance = this;
        this.packetManager = loadPlatformClass(ModPacketManager.class);
        this.attachmentManager = loadPlatformClass(ModAttachmentManager.class);
    }

    public final void init() {
        ModBlocks.REGISTRY.init();
        ModBlockEntityTypes.REGISTRY.init();
        ModItems.REGISTRY.init();
        ModCreativeTabs.REGISTRY.init();
        ModEntityTypes.REGISTRY.init();
        ModMenuTypes.REGISTRY.init();
        ModDataAttachments.REGISTRY.init();
        this.packetManager.init();
    }

    public abstract String getPlatform();

    public abstract boolean isFakePlayer(Player player);

    public abstract Block getPottedBlock(Block block);

    public <M extends AbstractContainerMenu, D> void openMenu(ModMenuTypeRegistry.AdvancedMenuTypeEntry<M, D> entry, TriFunction<Integer, Inventory, Container, M> constructor, Player player, Container container, D data, Component title) {
        this.openMenu(entry, (syncId, inventory) -> constructor.apply(syncId, inventory, container), player, data, title);
    }

    public abstract <M extends AbstractContainerMenu, D> void openMenu(ModMenuTypeRegistry.AdvancedMenuTypeEntry<M, D> entry, BiFunction<Integer, Inventory, M> constructor, Player player, D data, Component title);

    public <M extends AbstractContainerMenu> void openMenu(TriFunction<Integer, Inventory, Container, M> constructor, Player player, Container container, Component title) {
        this.openMenu((syncId, inventory) -> constructor.apply(syncId, inventory, container), player, title);
    }

    public <M extends AbstractContainerMenu> void openMenu(BiFunction<Integer, Inventory, M> constructor, Player player, Component title) {
        player.openMenu(new MenuProvider() {
            @Override
            public @NotNull Component getDisplayName() {
                return title;
            }

            @Override
            public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
                return constructor.apply(containerId, playerInventory);
            }
        });
    };

    public static FurnitureMod getInstance() {
        return instance;
    }

    public static void updateBlock(Level level, BlockPos pos) {
        if(level instanceof ServerLevel serverLevel) {
            instance.packetManager.sendToPlayersTrackingChunk(serverLevel, new ChunkPos(pos), new BlockChangedPayload(pos));
        }
    }

    public static ModPacketManager getPacketManager() {
        return instance.packetManager;
    }

    public static ModAttachmentManager getAttachmentManager() {
        return instance.attachmentManager;
    }

    public static <T> ModRegistry<T> registry(ResourceKey<Registry<T>> registryKey) {
        return loadPlatformClass(ModRegistry.class, registryKey);
    }

    public static ModBlockRegistry blockRegistry() {
        return loadPlatformClass(ModBlockRegistry.class);
    }

    public static ModBlockEntityTypeRegistry blockEntityTypeRegistry() {
        return loadPlatformClass(ModBlockEntityTypeRegistry.class);
    }

    public static ModItemRegistry itemRegistry() {
        return loadPlatformClass(ModItemRegistry.class);
    }

    public static ModMenuTypeRegistry menuTypeRegistry() { return loadPlatformClass(ModMenuTypeRegistry.class); }

    public static ModAttachmentRegistry<?> attachmentRegistry() {
        return loadPlatformClass(ModAttachmentRegistry.class);
    }

    public static <T> T loadPlatformClass(Class<T> clazz, Object... parameters) {
        String name = clazz.getName();
        String platformName = name.substring(0, name.lastIndexOf('.')) + "." + instance.getPlatform() + name.substring(name.lastIndexOf('.') + 1);
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
