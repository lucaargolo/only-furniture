package dev.lucaargolo.furniture.network;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.block.FurnitureBlock;
import dev.lucaargolo.furniture.utils.FurnitureData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;

public record DestroyEffectsPayload(BlockPos blockPos, int state, int packedData) implements CustomPacketPayload {

    public static final Type<DestroyEffectsPayload> TYPE = new Type<>(FurnitureMod.id("destroy_effects"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DestroyEffectsPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            DestroyEffectsPayload::blockPos,
            ByteBufCodecs.VAR_INT,
            DestroyEffectsPayload::state,
            ByteBufCodecs.VAR_INT,
            DestroyEffectsPayload::packedData,
            DestroyEffectsPayload::new
    );

    public static void handleClient(DestroyEffectsPayload payload, Executor executor) {
        executor.execute(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            ClientLevel level = minecraft.level;
            if(level != null) {
                FurnitureBlock.destroyEffects(level, payload.blockPos, Block.stateById(payload.state), new FurnitureData((short) payload.packedData));
            }

        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
