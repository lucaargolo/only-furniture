package dev.lucaargolo.furniture.network;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.item.FurnitureBlockItem;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;

public record FurnitureRotationPayload(float rotation) implements CustomPacketPayload {

    public static final Type<FurnitureRotationPayload> TYPE = new Type<>(FurnitureMod.id("furniture_rotation"));

    public static final StreamCodec<ByteBuf, FurnitureRotationPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT,
            FurnitureRotationPayload::rotation,
            FurnitureRotationPayload::new
    );

    public static void handleServer(FurnitureRotationPayload payload, ServerPlayer player, Executor executor) {
        executor.execute(() -> FurnitureBlockItem.setRotation(player, payload.rotation));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
