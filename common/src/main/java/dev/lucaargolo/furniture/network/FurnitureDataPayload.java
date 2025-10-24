package dev.lucaargolo.furniture.network;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.data.LocalFurnitureData;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;

public record FurnitureDataPayload(ResourceKey<Level> dimension, long regionPos, int regionLocalBlockPos, int packedFurnitureData) implements CustomPacketPayload {

    public static final Type<FurnitureDataPayload> TYPE = new Type<>(FurnitureMod.id("furniture_data"));

    public static final StreamCodec<ByteBuf, FurnitureDataPayload> STREAM_CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(Registries.DIMENSION),
            FurnitureDataPayload::dimension,
            ByteBufCodecs.VAR_LONG,
            FurnitureDataPayload::regionPos,
            ByteBufCodecs.VAR_INT,
            FurnitureDataPayload::regionLocalBlockPos,
            ByteBufCodecs.VAR_INT,
            FurnitureDataPayload::packedFurnitureData,
            FurnitureDataPayload::new
    );

    public static void handleClient(FurnitureDataPayload payload, Executor executor) {
        executor.execute(() -> LocalFurnitureData.set(payload.dimension(), payload.regionPos(), payload.regionLocalBlockPos(), payload.packedFurnitureData()));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
