package dev.lucaargolo.furniture.utils;

import com.mojang.datafixers.util.Pair;
import dev.lucaargolo.furniture.network.FurnitureDataPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class FurnitureData {

    public static FurnitureData DEFAULT = new FurnitureData(0.5f, 0.5f, 0f, null);
    public static long DEFAULT_PACKED_LAYERS = FurnitureUtils.packFurnitureDataLayers(DEFAULT, DEFAULT, DEFAULT, DEFAULT);

    private final short packed;

    public FurnitureData(short packed) {
        this.packed = packed;
    }

    public FurnitureData(float x, float z, float rotation, @Nullable Direction toOriginal) {
        int ofx = Mth.clamp(Mth.floor(x * 16f), 0, 15);
        int ofz = Mth.clamp(Mth.floor(z * 16f), 0, 15);
        int rot = Mth.floor(Math.min(rotation, 359f) / 22.5f);
        int dir = (toOriginal == null ? 0 : toOriginal.ordinal() + 1);
        this.packed = (short) ((dir << 12) | (rot << 8) | (ofz << 4) | ofx);
    }

    public float getX() {
        int value = packed & 0xFFFF;
        return (value & 0b1111) / 16f - 0.5f;
    }

    public float getZ() {
        int value = packed & 0xFFFF;
        return ((value >> 4) & 0b1111) / 16f - 0.5f;
    }

    public float getRotation() {
        int value = packed & 0xFFFF;
        int rotationIndex = (value >> 8) & 0b1111;
        return rotationIndex * 22.5f;
    }

    @Nullable
    public Direction getDirectionToOriginal() {
        int value = packed & 0xFFFF;
        int dir = (value >> 12) & 0b1111;
        return dir == 0 ? null : Direction.values()[dir - 1];
    }

    public short getPacked() {
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

    public static Pair<FurnitureData, Vec3i> getOriginal(BlockGetter level, BlockPos pos, int layer) {
        FurnitureData data = FurnitureData.get(level, pos, layer);
        Vec3i toOriginal = Vec3i.ZERO;
        Set<BlockPos> positions = new HashSet<>();
        while (data.getDirectionToOriginal() != null && !positions.contains(pos)) {
            positions.add(pos);
            Direction direction = data.getDirectionToOriginal();
            pos = pos.relative(direction);
            toOriginal = toOriginal.relative(direction);
            data = FurnitureData.get(level, pos, layer);
        }
        return Pair.of(data, toOriginal);
    }

    public static FurnitureData get(BlockGetter level, BlockPos pos, int layer) {
        ChunkPos chunkPos = new ChunkPos(pos);
        long regionPos = FurnitureUtils.chunkPosToRegionPos(chunkPos);
        int regionLocalBlockPos = FurnitureUtils.blockPosToRegionLocalBlockPos(pos);
        if(level instanceof ServerLevel serverLevel) {
            return RegionFurnitureData.get(serverLevel, FurnitureUtils.regionKey(regionPos)).get(regionLocalBlockPos, layer);
        }else{
            ResourceKey<Level> dimension = FurnitureUtils.getBlockGetterDimension(level);
            if(dimension != null) {
                return LocalFurnitureData.get(dimension, regionPos, regionLocalBlockPos, layer);
            }
        }
        return FurnitureData.DEFAULT;
    }

    public static void set(Level level, BlockPos pos, int layer, FurnitureData data) {
        ChunkPos chunkPos = new ChunkPos(pos);
        long regionPos = FurnitureUtils.chunkPosToRegionPos(chunkPos);
        int regionLocalBlockPos = FurnitureUtils.blockPosToRegionLocalBlockPos(pos);
        if(level instanceof ServerLevel serverLevel) {
            RegionFurnitureData.get(serverLevel, FurnitureUtils.regionKey(regionPos)).set(regionLocalBlockPos, layer, data);
            RegionFurnitureData.sendToPlayersTrackingRegion(serverLevel, regionPos, new FurnitureDataPayload(level.dimension(), regionPos, regionLocalBlockPos, layer, data.getPacked()));
        }else {
            LocalFurnitureData.set(level.dimension(), regionPos, regionLocalBlockPos, layer, data.getPacked());
        }
    }



}

