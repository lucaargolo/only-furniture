package dev.lucaargolo.furniture;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lucaargolo.furniture.utils.FurnitureUtils;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class ChunkFurnitureData {

    public static final Codec<ChunkFurnitureData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT_STREAM.fieldOf("pos").forGetter(data -> data.furnitureDataMap.keySet().intStream()),
        Codec.LONG_STREAM.fieldOf("layers").forGetter(data -> data.furnitureDataMap.values().longStream())
    ).apply(instance, (posStream, layersStream) -> {
        int[] posArray = posStream.toArray();
        long[] layersArray = layersStream.toArray();
        Preconditions.checkState(posArray.length == layersArray.length, "Mismatched key/value array lengths");
        Int2LongMap furnitureDataMap = new Int2LongOpenHashMap();
        for (int i = 0; i < posArray.length; i++) {
            furnitureDataMap.put(posArray[i], layersArray[i]);
        }
        return new ChunkFurnitureData(furnitureDataMap);
    }));

    public static final StreamCodec<ByteBuf, ChunkFurnitureData> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.map(Int2LongOpenHashMap::new, ByteBufCodecs.VAR_INT, ByteBufCodecs.VAR_LONG),
        ChunkFurnitureData::getFurnitureDataMap,
        ChunkFurnitureData::new
    );

    private final Int2LongMap furnitureDataMap;
    private final Int2ObjectMap<VoxelShape> shapeCacheMap = new Int2ObjectOpenHashMap<>();

    private ChunkFurnitureData(Map<Integer, Long> map) {
        if(map instanceof Int2LongMap int2LongMap) {
            this.furnitureDataMap = Int2LongMaps.synchronize(int2LongMap);
        }else{
            this.furnitureDataMap = Int2LongMaps.synchronize(new Int2LongOpenHashMap(map));
        }
        this.furnitureDataMap.defaultReturnValue(FurnitureData.DEFAULT_PACKED_LAYERS);
    }

    public ChunkFurnitureData() {
        this(new Int2LongOpenHashMap());
    }

    public FurnitureData[] get(BlockPos pos) {
        int packedPos = FurnitureUtils.packChunkLocalPos(pos);
        long packedLayers = furnitureDataMap.get(packedPos);
        if(packedLayers != FurnitureData.DEFAULT_PACKED_LAYERS) {
            return FurnitureUtils.unpackFurnitureDataLayers(packedLayers);
        }
        return FurnitureData.DEFAULT_LAYERS.clone();
    }

    public void set(BlockPos pos, FurnitureData[] layers) {
        int packedPos = FurnitureUtils.packChunkLocalPos(pos);
        shapeCacheMap.remove(packedPos);

        long newPackedLayers = FurnitureUtils.packFurnitureDataLayers(layers);
        boolean isDefault = newPackedLayers == FurnitureData.DEFAULT_PACKED_LAYERS;

        long packedLayers = furnitureDataMap.get(packedPos);
        if(packedLayers != FurnitureData.DEFAULT_PACKED_LAYERS || !isDefault) {
            packedLayers = newPackedLayers;
            if(packedLayers != FurnitureData.DEFAULT_PACKED_LAYERS) {
                furnitureDataMap.put(packedPos, packedLayers);
            }else{
                furnitureDataMap.remove(packedPos);
            }
        }
    }

    public VoxelShape cachedShape(BlockPos pos, Supplier<VoxelShape> shapeSupplier) {
        int packedPos = FurnitureUtils.packChunkLocalPos(pos);
        return shapeCacheMap.computeIfAbsent(packedPos, k -> shapeSupplier.get());
    }

    @Nullable
    protected VoxelShape getCachedShape(BlockPos pos) {
        int packedPos = FurnitureUtils.packChunkLocalPos(pos);
        return shapeCacheMap.get(packedPos);
    }

    protected void forEach(BiConsumer<Integer, Long> consumer) {
        furnitureDataMap.forEach(consumer);
    }

    private Int2LongMap getFurnitureDataMap() {
        return furnitureDataMap;
    }

}
