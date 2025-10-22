package dev.lucaargolo.furniture.network;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.data.LocalFurnitureData;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;

public record ChunkFurnitureDataPayload(ResourceKey<Level> dimension, long pos, Long2IntMap data) implements CustomPacketPayload {

    public static final Type<ChunkFurnitureDataPayload> TYPE = new Type<>(FurnitureMod.id("chunk_furniture_data"));

    public static final StreamCodec<ByteBuf, ChunkFurnitureDataPayload> STREAM_CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(Registries.DIMENSION),
            ChunkFurnitureDataPayload::dimension,
            ByteBufCodecs.VAR_LONG,
            ChunkFurnitureDataPayload::pos,
            ByteBufCodecs.map(Long2IntOpenHashMap::new, ByteBufCodecs.VAR_LONG, ByteBufCodecs.VAR_INT),
            ChunkFurnitureDataPayload::data,
            ChunkFurnitureDataPayload::new
    );

    public static void handleClient(ChunkFurnitureDataPayload payload, Executor executor) {
        executor.execute(() -> {
            LocalFurnitureData.put(payload.dimension(), payload.pos(), payload.data());
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
