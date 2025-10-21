package dev.lucaargolo.furniture.data;

import com.mojang.datafixers.util.Pair;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;

import java.util.Objects;

public class FurnitureData {

    public static FurnitureData DEFAULT = new FurnitureData(0);

    public static final StreamCodec<ByteBuf, FurnitureData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            FurnitureData::getPacked,
            FurnitureData::new
    );

    private final int packed;

    public FurnitureData(int packed) {
        this.packed = packed;
    }

    public FurnitureData(int x, int z, float rotation) {
        int rot = Math.round(rotation / 22.5f) & 0b1111;
        this.packed = (rot << 8) | ((z & 0b1111) << 4) | (x & 0b1111);
    }

    public int getX() {
        return packed & 0b1111;
    }

    public int getZ() {
        return (packed >> 4) & 0b1111;
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

    public static FurnitureData get(ServerLevel level, BlockPos pos) {
        Pair<String, Integer> pair = blockToRegion(pos);
        return regionData(level, pair.getFirst()).get(pair.getSecond(), pos.asLong());
    }


    public static void set(ServerLevel level, BlockPos pos, FurnitureData data) {
        Pair<String, Integer> pair = blockToRegion(pos);
        regionData(level, pair.getFirst()).set(pair.getSecond(), pos.asLong(), data);
    }

    private static RegionFurnitureData regionData(ServerLevel level, String key) {
        return level.getDataStorage().get(RegionFurnitureData.FACTORY, key);
    }

    private static Pair<String, Integer> blockToRegion(BlockPos pos) {
        int x = SectionPos.blockToSectionCoord(pos.getX());
        int z = SectionPos.blockToSectionCoord(pos.getZ());
        return Pair.of(String.format("furniture_r_%s_%s", x >> 5, z >> 5), ((z & 31) << 5) | (x & 31));
    }


}

