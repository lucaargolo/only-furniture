package dev.lucaargolo.furniture.data;

import com.mojang.datafixers.util.Pair;
import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.mixin.RenderChunkRegionAccessor;
import dev.lucaargolo.furniture.network.ChunkFurnitureDataPayload;
import dev.lucaargolo.furniture.network.FurnitureDataPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class FurnitureData {

    public static FurnitureData DEFAULT = new FurnitureData(0.5f, 0.5f, 0f);

    private final int packed;

    protected FurnitureData(int packed) {
        this.packed = packed;
    }

    public FurnitureData(float x, float z, float rotation) {
        int ofx = Mth.floor(Math.min(x*16f, 15f)) & 0b1111;
        int ofz = Mth.floor(Math.min(z*16f, 15f)) & 0b1111;
        int rot = Mth.floor(Math.min(rotation, 359f) / 22.5f) & 0b1111;
        this.packed = (rot << 8) | (ofz << 4) | ofx;
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
        Pair<String, Integer> pair = blockToRegion(chunkPos);
        FurnitureData data;
        if(level instanceof ServerLevel serverLevel) {
            data = getRegion(serverLevel, pair.getFirst()).get(pair.getSecond(), pos.asLong());
        }else{
            ResourceKey<Level> dimension = getBlockGetterDimension(level);
            if(dimension == null) {
                data = FurnitureData.DEFAULT;
            }else{
                data = LocalFurnitureData.get(dimension, chunkPos.toLong(), pos.asLong());
            }
        }
        return data;
    }

    public static void set(Level level, BlockPos pos, FurnitureData data) {
        ChunkPos chunkPos = new ChunkPos(pos);
        Pair<String, Integer> pair = blockToRegion(chunkPos);
        if(level instanceof ServerLevel serverLevel) {
            getRegion(serverLevel, pair.getFirst()).set(pair.getSecond(), pos.asLong(), data);
            FurnitureMod.INSTANCE.getPacketManager().sendToPlayersTrackingChunk(serverLevel, chunkPos, new FurnitureDataPayload(level.dimension(), chunkPos.toLong(), pos.asLong(), data.getPacked()));
        }else {
            LocalFurnitureData.set(level.dimension(), chunkPos.toLong(), pos.asLong(), data.getPacked());
        }
    }

    public static void sendChunk(ServerPlayer player, ServerLevel level, ChunkPos chunkPos) {
        Pair<String, Integer> pair = blockToRegion(chunkPos);
        RegionFurnitureData data = getRegion(level, pair.getFirst());
        FurnitureMod.INSTANCE.getPacketManager().sendToPlayer(player, new ChunkFurnitureDataPayload(level.dimension(), chunkPos.toLong(), data.get(pair.getSecond())));
    }

    private static RegionFurnitureData getRegion(ServerLevel level, String key) {
        return level.getDataStorage().computeIfAbsent(RegionFurnitureData.FACTORY, key);
    }

    private static Pair<String, Integer> blockToRegion(ChunkPos pos) {
        return Pair.of(String.format("furniture_r_%s_%s", pos.x >> 5, pos.z >> 5), ((pos.z & 31) << 5) | (pos.x & 31));
    }

    @Nullable
    private static ResourceKey<Level> getBlockGetterDimension(BlockGetter blockGetter) {
        if(blockGetter instanceof Level level) {
            return level.dimension();
        }else if(blockGetter instanceof RenderChunkRegionAccessor region) {
            return region.getLevel().dimension();
        }else{
            return null;
        }
    }

}

