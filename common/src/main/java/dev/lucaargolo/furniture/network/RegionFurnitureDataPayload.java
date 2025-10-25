package dev.lucaargolo.furniture.network;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.data.FurnitureData;
import dev.lucaargolo.furniture.data.LocalFurnitureData;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2LongMap;
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;

public record RegionFurnitureDataPayload(ResourceKey<Level> dimension, long regionPos, Int2LongMap regionMap) implements CustomPacketPayload {

    public static final Type<RegionFurnitureDataPayload> TYPE = new Type<>(FurnitureMod.id("chunk_furniture_data"));

    public static final StreamCodec<ByteBuf, RegionFurnitureDataPayload> STREAM_CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(Registries.DIMENSION),
            RegionFurnitureDataPayload::dimension,
            ByteBufCodecs.VAR_LONG,
            RegionFurnitureDataPayload::regionPos,
            ByteBufCodecs.map(Int2LongOpenHashMap::new, ByteBufCodecs.VAR_INT, ByteBufCodecs.VAR_LONG),
            RegionFurnitureDataPayload::regionMap,
            RegionFurnitureDataPayload::new
    );

    public static void handleClient(RegionFurnitureDataPayload payload, Executor executor) {
        executor.execute(() -> {
            payload.regionMap().defaultReturnValue(FurnitureData.DEFAULT_PACKED_LAYERS);
            LocalFurnitureData.put(payload.dimension(), payload.regionPos(), payload.regionMap());
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
