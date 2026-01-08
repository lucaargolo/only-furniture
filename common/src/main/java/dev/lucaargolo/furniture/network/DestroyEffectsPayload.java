package dev.lucaargolo.furniture.network;

import dev.lucaargolo.furniture.FurnitureData;
import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.block.FurnitureBlock;
import dev.lucaargolo.furniture.client.FurnitureModClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
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
                spawnDestroyEffects(level, payload.blockPos, Block.stateById(payload.state), new FurnitureData((short) payload.packedData));
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private static void spawnDestroyEffects(ClientLevel level, BlockPos pos, BlockState state, FurnitureData data) {
        if(state.getBlock() instanceof FurnitureBlock block) {
            VoxelShape shape = block.getShapeForFurniture(level, pos, state, data, -1);
            SoundType soundType = state.getSoundType();
            level.playLocalSound(pos, soundType.getBreakSound(), SoundSource.BLOCKS, (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F, false);
            shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
                double xSize = Math.min(1.0, maxX - minX);
                double ySize = Math.min(1.0, maxY - minY);
                double zSize = Math.min(1.0, maxZ - minZ);

                int xBounds = Math.max(2, Mth.ceil(xSize / 0.25));
                int yBounds = Math.max(2, Mth.ceil(ySize / 0.25));
                int zBounds = Math.max(2, Mth.ceil(zSize / 0.25));

                for (int x = 0; x < xBounds; x++) {
                    for (int y = 0; y < yBounds; y++) {
                        for (int z = 0; z < zBounds; z++) {
                            double xOffset = ((double)x + 0.5) / (double)xBounds;
                            double yOffset = ((double)y + 0.5) / (double)yBounds;
                            double zOffset = ((double)z + 0.5) / (double)zBounds;
                            double xPos = xOffset * xSize + minX;
                            double yPos = yOffset * ySize + minY;
                            double zPos = zOffset * zSize + minZ;
                            FurnitureModClient.addTerrainParticle(level, pos, state, xPos, yPos, zPos, xOffset, yOffset, zOffset);
                        }
                    }
                }
            });
        }
    }

}
