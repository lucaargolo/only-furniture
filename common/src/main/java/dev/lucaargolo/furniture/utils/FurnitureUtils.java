package dev.lucaargolo.furniture.utils;

import dev.lucaargolo.furniture.mixin.RenderChunkRegionAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class FurnitureUtils {

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
