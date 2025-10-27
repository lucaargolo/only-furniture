package dev.lucaargolo.furniture;

import dev.lucaargolo.furniture.block.FurnitureBlock;
import dev.lucaargolo.furniture.block.ModBlocks;
import dev.lucaargolo.furniture.item.ModItems;
import dev.lucaargolo.furniture.network.ModPacketManager;
import dev.lucaargolo.furniture.utils.ModRegistry;
import dev.lucaargolo.furniture.utils.RegionFurnitureData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unchecked")
public abstract class FurnitureMod {

    public static final String MOD_ID = "furniture";
    public static final String MOD_NAME = "Furniture Mod";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

    public static FurnitureMod INSTANCE;

    private final ModPacketManager packetManager = loadPlatformClass(ModPacketManager.class);

    public final void init() {
        INSTANCE = this;
        ModBlocks.BLOCKS.init();
        ModItems.ITEMS.init();
        packetManager.init();
    }

    public abstract String getPlatform();

    public final ModPacketManager getPacketManager() {
        return packetManager;
    }

    public final boolean beforeBlockBreak(BlockState state, ServerLevel level, BlockPos pos, Player player) {
        if(state.getBlock() instanceof FurnitureBlock block) {
            return block.onBeforeRemove(level, pos, player);
        }else{
            return false;
        }
    }

    public final void onServerChunkWatch(ServerLevel level, ServerPlayer player, ChunkPos pos) {
        RegionFurnitureData.watchChunk(level, player, pos);
    }

    public final void onServerChunkUnwatch(ServerLevel level, ServerPlayer player, ChunkPos pos) {
        RegionFurnitureData.unwatchChunk(level, player, pos);
    }

    public final <T> ModRegistry<T> registry(ResourceKey<Registry<T>> registryKey) {
        return loadPlatformClass(ModRegistry.class, registryKey);
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
