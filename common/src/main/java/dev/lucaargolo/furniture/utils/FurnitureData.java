package dev.lucaargolo.furniture.utils;

import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import dev.lucaargolo.furniture.block.FurnitureBlock;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

public class FurnitureData {

    public static FurnitureData DEFAULT = new FurnitureData(0.5f, 0.5f, 0f, null, false);
    public static FurnitureData[] DEFAULT_LAYERS = new FurnitureData[] {DEFAULT, DEFAULT, DEFAULT, DEFAULT};
    public static long DEFAULT_PACKED_LAYERS = FurnitureUtils.packFurnitureDataLayers(DEFAULT_LAYERS);

    private final short packed;

    public FurnitureData(short packed) {
        this.packed = packed;
    }

    public FurnitureData(float x, float z, float rotation, @Nullable Direction toOriginal, boolean hasOriginal) {
        int ofx = Mth.clamp(Mth.floor(x * 16f), 0, 15);
        int ofz = Mth.clamp(Mth.floor(z * 16f), 0, 15);
        int rot = Mth.floor(Math.min(rotation, 359f) / 22.5f);
        int dir = (toOriginal == null ? 0 : toOriginal.ordinal() + 1);
        int origBit = hasOriginal ? 1 : 0;
        this.packed = (short) ((origBit << 15) | (dir << 12) | (rot << 8) | (ofz << 4) | ofx);
    }

    public float x() {
        int value = packed & 0xFFFF;
        return (value & 0b1111) / 16f - 0.5f;
    }

    public float z() {
        int value = packed & 0xFFFF;
        return ((value >> 4) & 0b1111) / 16f - 0.5f;
    }

    public float rotation() {
        int value = packed & 0xFFFF;
        int rotationIndex = (value >> 8) & 0b1111;
        return rotationIndex * 22.5f;
    }

    public Rotation getRotation() {
        int rounded = Math.round(this.rotation() / 90f) * 90;
        return switch (rounded % 360) {
            case 0 -> Rotation.R0;
            case 90 -> Rotation.R90;
            case 180 -> Rotation.R180;
            case 270 -> Rotation.R270;
            default -> throw new IllegalStateException("Unexpected value: " + rounded);
        };
    }

    public Direction getFacing(BlockState state) {
        return !state.hasProperty(FurnitureBlock.FACING) ? Direction.fromYRot(this.rotation() + 180) : state.getValue(FurnitureBlock.FACING);
    }

    public Quaternionf getRotation(BlockState state) {
        if(!state.hasProperty(FurnitureBlock.FACING)) {
            return Axis.YN.rotationDegrees(this.rotation());
        }else{
            Direction facing = state.getValue(FurnitureBlock.FACING);
            return switch (facing.getAxis()) {
                case X -> facing.getAxisDirection() == Direction.AxisDirection.POSITIVE ? Axis.XP.rotationDegrees(this.rotation()) : Axis.XN.rotationDegrees(this.rotation());
                case Z ->  facing.getAxisDirection() == Direction.AxisDirection.POSITIVE ? Axis.ZP.rotationDegrees(this.rotation()) : Axis.ZN.rotationDegrees(this.rotation());
                default -> throw new IllegalStateException("Unexpected value: " + state.getValue(FurnitureBlock.FACING));
            };
        }
    }

    public float getOffset(Direction.Axis axis, BlockState state) {
        return switch (axis) {
            case X -> !state.hasProperty(FurnitureBlock.FACING) || state.getValue(FurnitureBlock.FACING).getAxis() != Direction.Axis.X ? x() : 0f;
            case Y -> !state.hasProperty(FurnitureBlock.FACING) ? 0f : switch (state.getValue(FurnitureBlock.FACING).getAxis()) {
                case X -> this.x();
                case Z -> this.z();
                default -> 0f;
            };
            case Z -> !state.hasProperty(FurnitureBlock.FACING) || state.getValue(FurnitureBlock.FACING).getAxis() != Direction.Axis.Z ? z() : 0f;
        };
    }

    public float getX(BlockState state) {
        return getOffset(Direction.Axis.X, state);
    }

    public float getY(BlockState state) {
        return getOffset(Direction.Axis.Y, state);
    }

    public float getZ(BlockState state) {
        return getOffset(Direction.Axis.Z, state);
    }


    @Nullable
    public Direction getDirectionToOriginal() {
        int value = packed & 0xFFFF;
        int dir = (value >> 12) & 0b111;
        return dir == 0 ? null : Direction.values()[dir - 1];
    }

    public boolean hasOriginal() {
        int value = packed & 0xFFFF;
        return ((value >> 15) & 0b1) != 0;
    }

    public short getPacked() {
        return packed;
    }

    public boolean equalsIgnoreRotation(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        FurnitureData that = (FurnitureData) o;
        int mask = 0b1111_0000_1111_1111; // or 0xF8FF
        return (packed & mask) == (that.packed & mask);
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

    public static VoxelShape cachedShape(BlockGetter level, BlockPos pos, Supplier<VoxelShape> shapeSupplier) {
        ChunkPos chunkPos = new ChunkPos(pos);
        long regionPos = FurnitureUtils.chunkPosToRegionPos(chunkPos);
        int regionLocalBlockPos = FurnitureUtils.blockPosToRegionLocalBlockPos(pos);
        if(level instanceof ServerLevel serverLevel) {
            return RegionFurnitureData.get(serverLevel, FurnitureUtils.regionKey(regionPos)).cachedShape(regionLocalBlockPos, shapeSupplier);
        }else{
            ResourceKey<Level> dimension = FurnitureUtils.getBlockGetterDimension(level);
            if(dimension != null) {
                return LocalFurnitureData.cachedShape(dimension, regionPos, regionLocalBlockPos, shapeSupplier);
            }
        }
        return shapeSupplier.get();
    }

    public static Pair<FurnitureData, Vec3i> getOriginal(BlockGetter level, BlockPos pos, int layer) {
        FurnitureData data = FurnitureData.get(level, pos, layer);
        Vec3i toOriginal = Vec3i.ZERO;
        Set<BlockPos> positions = new HashSet<>();
        while (data.getDirectionToOriginal() != null && !positions.contains(pos) && positions.size() < 32) {
            positions.add(pos);
            Direction direction = data.getDirectionToOriginal();
            pos = pos.relative(direction);
            toOriginal = toOriginal.relative(direction);
            data = FurnitureData.get(level, pos, layer);
        }
        return Pair.of(data, toOriginal);
    }

    public static FurnitureData get(BlockGetter level, BlockPos pos, int layer) {
        return FurnitureData.get(level, pos)[layer];
    }

    public static FurnitureData[] get(BlockGetter level, BlockPos pos) {
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
        return FurnitureData.DEFAULT_LAYERS.clone();
    }

    public static void set(Level level, BlockPos pos, int layer, FurnitureData data) {
        FurnitureData[] layers = FurnitureData.get(level, pos);
        layers[layer] = data;
        set(level, pos, layers);
    }

    public static void set(Level level, BlockPos pos, FurnitureData[] layers) {
        ChunkPos chunkPos = new ChunkPos(pos);
        long regionPos = FurnitureUtils.chunkPosToRegionPos(chunkPos);
        int regionLocalBlockPos = FurnitureUtils.blockPosToRegionLocalBlockPos(pos);
        if(level instanceof ServerLevel serverLevel) {
            RegionFurnitureData.get(serverLevel, FurnitureUtils.regionKey(regionPos)).set(regionLocalBlockPos, layers);
            RegionFurnitureData.sendToPlayersTrackingRegion(serverLevel, regionPos, new FurnitureDataPayload(level.dimension(), regionPos, regionLocalBlockPos, layers));
        }else {
            LocalFurnitureData.set(level.dimension(), regionPos, regionLocalBlockPos, layers);
        }
    }

    public static void clearShapeCache(Level level, BlockPos pos) {
        FurnitureData[] layers = FurnitureData.get(level, pos);
        FurnitureData.set(level, pos, layers);
    }

}

