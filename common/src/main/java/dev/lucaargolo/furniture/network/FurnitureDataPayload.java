package dev.lucaargolo.furniture.network;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.utils.FurnitureData;
import dev.lucaargolo.furniture.utils.FurnitureUtils;
import dev.lucaargolo.furniture.utils.LocalFurnitureData;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;

public record FurnitureDataPayload(ResourceKey<Level> dimension, long regionPos, int regionLocalBlockPos, FurnitureData[] layers) implements CustomPacketPayload {

    public static final Type<FurnitureDataPayload> TYPE = new Type<>(FurnitureMod.id("furniture_data"));

    private static final StreamCodec<ByteBuf, FurnitureData[]> LAYER_CODEC = new StreamCodec<>() {
        @Override
        public FurnitureData @NotNull [] decode(@NotNull ByteBuf buffer) {
            return FurnitureUtils.unpackFurnitureDataLayers(buffer.readLong());
        }

        @Override
        public void encode(@NotNull ByteBuf buffer, FurnitureData @NotNull [] value) {
            buffer.writeLong(FurnitureUtils.packFurnitureDataLayers(value));
        }
    };

    public static final StreamCodec<ByteBuf, FurnitureDataPayload> STREAM_CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(Registries.DIMENSION),
            FurnitureDataPayload::dimension,
            ByteBufCodecs.VAR_LONG,
            FurnitureDataPayload::regionPos,
            ByteBufCodecs.VAR_INT,
            FurnitureDataPayload::regionLocalBlockPos,
            LAYER_CODEC,
            FurnitureDataPayload::layers,
            FurnitureDataPayload::new
    );

    public static void handleClient(FurnitureDataPayload payload, Executor executor) {
        executor.execute(() -> LocalFurnitureData.set(payload.dimension(), payload.regionPos(), payload.regionLocalBlockPos(), payload.layers()));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
