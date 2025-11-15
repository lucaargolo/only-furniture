package dev.lucaargolo.furniture.attachment.impl;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lucaargolo.furniture.FurnitureData;
import dev.lucaargolo.furniture.attachment.DataAttachment;
import dev.lucaargolo.furniture.utils.PackingUtils;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public final class ChunkFurnitureDataAttachment implements DataAttachment<ChunkFurnitureDataAttachment> {

    public static final Codec<ChunkFurnitureDataAttachment> CODEC = RecordCodecBuilder.create(instance -> instance.group(
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
        return new ChunkFurnitureDataAttachment(furnitureDataMap);
    }));

    public static final StreamCodec<ByteBuf, ChunkFurnitureDataAttachment> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(Int2LongOpenHashMap::new, ByteBufCodecs.VAR_INT, ByteBufCodecs.VAR_LONG),
            ChunkFurnitureDataAttachment::get,
            ChunkFurnitureDataAttachment::new
    );

    private final Int2LongMap furnitureDataMap;
    private final Int2ObjectMap<VoxelShape> shapeCacheMap = new Int2ObjectOpenHashMap<>();

    public ChunkFurnitureDataAttachment(Map<Integer, Long> map) {
        if(map instanceof Int2LongMap int2LongMap) {
            this.furnitureDataMap = Int2LongMaps.synchronize(int2LongMap);
        }else{
            this.furnitureDataMap = Int2LongMaps.synchronize(new Int2LongOpenHashMap(map));
        }
        this.furnitureDataMap.defaultReturnValue(FurnitureData.DEFAULT_PACKED_LAYERS);
    }

    private Int2LongMap get() {
        return this.furnitureDataMap;
    }

    public FurnitureData[] get(BlockPos pos) {
        int packedPos = PackingUtils.packChunkLocalPos(pos);
        long packedLayers = furnitureDataMap.get(packedPos);
        if(packedLayers != FurnitureData.DEFAULT_PACKED_LAYERS) {
            return PackingUtils.unpackFurnitureDataLayers(packedLayers);
        }
        return FurnitureData.DEFAULT_LAYERS.clone();
    }

    public ChunkFurnitureDataAttachment set(BlockPos pos, FurnitureData[] layers) {
        int packedPos = PackingUtils.packChunkLocalPos(pos);
        this.shapeCacheMap.remove(packedPos);

        long newPackedLayers = PackingUtils.packFurnitureDataLayers(layers);
        boolean isDefault = newPackedLayers == FurnitureData.DEFAULT_PACKED_LAYERS;

        long packedLayers = this.furnitureDataMap.get(packedPos);
        if(packedLayers != FurnitureData.DEFAULT_PACKED_LAYERS || !isDefault) {
            packedLayers = newPackedLayers;
            if(packedLayers != FurnitureData.DEFAULT_PACKED_LAYERS) {
                this.furnitureDataMap.put(packedPos, packedLayers);
            }else{
                this.furnitureDataMap.remove(packedPos);
            }
        }
        return this;
    }

    public VoxelShape cachedShape(BlockPos pos, Supplier<VoxelShape> shapeSupplier) {
        int packedPos = PackingUtils.packChunkLocalPos(pos);
        return this.shapeCacheMap.computeIfAbsent(packedPos, k -> shapeSupplier.get());
    }

    @Nullable
    @ApiStatus.Internal
    public VoxelShape getCachedShape(BlockPos pos) {
        int packedPos = PackingUtils.packChunkLocalPos(pos);
        return this.shapeCacheMap.get(packedPos);
    }

    @ApiStatus.Internal
    public void forEach(BiConsumer<Integer, Long> consumer) {
        this.furnitureDataMap.forEach(consumer);
    }

}