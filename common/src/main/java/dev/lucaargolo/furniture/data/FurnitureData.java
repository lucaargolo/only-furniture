package dev.lucaargolo.furniture.data;

import dev.lucaargolo.furniture.network.FurnitureDataPayload;
import dev.lucaargolo.furniture.utils.FurnitureUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class FurnitureData {

    public static FurnitureData DEFAULT = new FurnitureData(0.5f, 0.5f, 0f, null);

    private final int packed;

    protected FurnitureData(int packed) {
        this.packed = packed;
    }

    public FurnitureData(float x, float z, float rotation, @Nullable Direction toOriginal) {
        int ofx = Mth.clamp(Mth.floor(x * 16f), 0, 15);
        int ofz = Mth.clamp(Mth.floor(z * 16f), 0, 15);
        int rot = Mth.floor(Math.min(rotation, 359f) / 22.5f);
        int dir = (toOriginal == null ? 0 : toOriginal.ordinal() + 1);
        this.packed = (dir << 12) | (rot << 8) | (ofz << 4) | ofx;
    }

    public float getX() {
        return (packed & 0b1111)/16f - 0.5f;
    }

    public float getZ() {
        return ((packed >> 4) & 0b1111)/16f - 0.5f;
    }

    public float getRotation() {
        int rotationIndex = (packed >> 8) & 0b1111;
        return rotationIndex * 22.5f;
    }

    @Nullable
    public Direction getDirectionToOriginal() {
        int dir = (packed >> 12) & 0b1111;
        return dir == 0 ? null : Direction.values()[dir - 1];
    }

    protected int getPacked() {
        return packed;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        FurnitureData that = (FurnitureData) o;
        return packed == that.packed;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(packed);
    }

    public static FurnitureData get(BlockGetter level, BlockPos pos) {
        ChunkPos chunkPos = new ChunkPos(pos);
        long regionPos = FurnitureUtils.chunkPosToRegionPos(chunkPos);
        int regionLocalBlockPos = FurnitureUtils.blockPosToRegionLocalBlockPos(pos);
        if(level instanceof ServerLevel serverLevel) {
            return RegionFurnitureData.get(serverLevel, FurnitureUtils.regionKey(regionPos)).get(regionLocalBlockPos);
        }else{
            ResourceKey<Level> dimension = FurnitureUtils.getBlockGetterDimension(level);
            if(dimension != null) {
                return LocalFurnitureData.get(dimension, regionPos, regionLocalBlockPos);
            }
        }
        return FurnitureData.DEFAULT;
    }

    public static void set(Level level, BlockPos pos, FurnitureData data) {
        ChunkPos chunkPos = new ChunkPos(pos);
        long regionPos = FurnitureUtils.chunkPosToRegionPos(chunkPos);
        int regionLocalBlockPos = FurnitureUtils.blockPosToRegionLocalBlockPos(pos);
        if(level instanceof ServerLevel serverLevel) {
            RegionFurnitureData.get(serverLevel, FurnitureUtils.regionKey(regionPos)).set(regionLocalBlockPos, data);
            RegionFurnitureData.sendToPlayersTrackingRegion(serverLevel, regionPos, new FurnitureDataPayload(level.dimension(), regionPos, regionLocalBlockPos, data.getPacked()));
        }else {
            LocalFurnitureData.set(level.dimension(), regionPos, regionLocalBlockPos, data.getPacked());
        }
    }



}

