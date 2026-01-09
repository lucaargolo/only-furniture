package dev.lucaargolo.furniture.network;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.mixin.LevelRendererAccessor;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;

public record BlockChangedPayload(BlockPos pos) implements CustomPacketPayload {

    public static final Type<BlockChangedPayload> TYPE = new Type<>(FurnitureMod.id("block_changed"));

    public static final StreamCodec<ByteBuf, BlockChangedPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            BlockChangedPayload::pos,
            BlockChangedPayload::new
    );

    public static void handleClient(BlockChangedPayload payload, Executor executor) {
        executor.execute(() -> {
            BlockPos pos = payload.pos();
            ((LevelRendererAccessor) Minecraft.getInstance().levelRenderer).invokeSetSectionDirty(
                    SectionPos.blockToSectionCoord(pos.getX()),
                    SectionPos.blockToSectionCoord(pos.getY()),
                    SectionPos.blockToSectionCoord(pos.getZ()),
                    true
            );
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
