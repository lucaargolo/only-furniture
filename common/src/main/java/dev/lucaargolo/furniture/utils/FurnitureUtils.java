package dev.lucaargolo.furniture.utils;

import dev.lucaargolo.furniture.mixin.RenderChunkRegionAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class FurnitureUtils {

    public static long updatePackedFurnitureDataLayers(long packedFurnitureDataLayers, int layer, short packedFurnitureData) {
        if (layer < 0 || layer > 3) {
            throw new IllegalArgumentException("Layer must be between 0 and 3");
        }

        int shift = (3 - layer) * 16;
        long mask = 0xFFFFL << shift;
        long value = ((long) packedFurnitureData & 0xFFFFL) << shift;

        return (packedFurnitureDataLayers & ~mask) | value;
    }

    public static long packFurnitureDataLayers(FurnitureData s1, FurnitureData s2, FurnitureData s3, FurnitureData s4) {
        return ((long) (s1.getPacked() & 0xFFFF) << 48)
                | ((long) (s2.getPacked() & 0xFFFF) << 32)
                | ((long) (s3.getPacked() & 0xFFFF) << 16)
                | ((long) (s4.getPacked() & 0xFFFF));
    }

    public static FurnitureData[] unpackFurnitureDataLayers(long packed) {
        FurnitureData[] result = new FurnitureData[4];
        result[0] = new FurnitureData((short) ((packed >>> 48) & 0xFFFF));
        result[1] = new FurnitureData((short) ((packed >>> 32) & 0xFFFF));
        result[2] = new FurnitureData((short) ((packed >>> 16) & 0xFFFF));
        result[3] = new FurnitureData((short) (packed & 0xFFFF));
        return result;
    }

    public static int blockPosToRegionLocalBlockPos(BlockPos pos) {
        int localX = pos.getX() & 511;
        int localZ = pos.getZ() & 511;
        int localY = pos.getY();

        int yOffset = localY + 2048;

        return (yOffset << 18) | (localZ << 9) | localX;
    }

    public static BlockPos regionLocalBlockPosToBlockPos(long regionPos, int regionLocalBlockPos) {
        int localX = regionLocalBlockPos & 0x1FF;
        int localZ = (regionLocalBlockPos >> 9) & 0x1FF;
        int y = ((regionLocalBlockPos >> 18) & 0xFFF) - 2048;

        int regionX = (int) (regionPos & 0xFFFFFFFFL);
        int regionZ = (int) (regionPos >> 32);

        int worldX = (regionX << 9) | localX;
        int worldZ = (regionZ << 9) | localZ;

        return new BlockPos(worldX, y, worldZ);
    }

    public static long chunkPosToRegionPos(ChunkPos pos) {
        int regionX = pos.getRegionX();
        int regionZ = pos.getRegionZ();
        return ((long) regionZ << 32) | ((long) regionX & 0xFFFFFFFFL);
    }

    public static String regionKey(long regionPos) {
        int regionX = (int) (regionPos & 0xFFFFFFFFL);
        int regionZ = (int) (regionPos >> 32);
        return String.format("furniture_r_%s_%s", regionX, regionZ);
    }

    @Nullable
    public static ResourceKey<Level> getBlockGetterDimension(BlockGetter blockGetter) {
        if(blockGetter instanceof Level level) {
            return level.dimension();
        }else if(blockGetter instanceof RenderChunkRegionAccessor region) {
            return region.getLevel().dimension();
        }else{
            return null;
        }
    }

}
