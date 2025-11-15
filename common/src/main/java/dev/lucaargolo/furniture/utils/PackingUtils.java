package dev.lucaargolo.furniture.utils;

import dev.lucaargolo.furniture.FurnitureData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

public class PackingUtils {

    public static long packFurnitureDataLayers(FurnitureData[] layers) {
        return ((long) (layers[0].getPacked() & 0xFFFF) << 48)
                | ((long) (layers[1].getPacked() & 0xFFFF) << 32)
                | ((long) (layers[2].getPacked() & 0xFFFF) << 16)
                | ((long) (layers[3].getPacked() & 0xFFFF));
    }

    public static FurnitureData[] unpackFurnitureDataLayers(long packed) {
        FurnitureData[] result = new FurnitureData[4];
        result[0] = new FurnitureData((short) ((packed >>> 48) & 0xFFFF));
        result[1] = new FurnitureData((short) ((packed >>> 32) & 0xFFFF));
        result[2] = new FurnitureData((short) ((packed >>> 16) & 0xFFFF));
        result[3] = new FurnitureData((short) (packed & 0xFFFF));
        return result;
    }

    public static int packChunkLocalPos(BlockPos pos) {
        int xLocal = pos.getX() & 15;
        int zLocal = pos.getZ() & 15;
        int y = pos.getY() + 2048;

        return (y << 8) | (zLocal << 4) | xLocal;
    }

    public static BlockPos unpackChunkLocalPos(ChunkPos chunkPos, int packed) {
        int xLocal = packed & 15;
        int zLocal = (packed >> 4) & 15;
        int y = ((packed >> 8) & 0xFFF) - 2048;

        int worldX = (chunkPos.x << 4) + xLocal;
        int worldZ = (chunkPos.z << 4) + zLocal;

        return new BlockPos(worldX, y, worldZ);
    }

}
