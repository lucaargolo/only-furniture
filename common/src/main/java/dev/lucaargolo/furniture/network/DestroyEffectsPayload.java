package dev.lucaargolo.furniture.network;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.block.FurnitureBlock;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.Executor;

public record DestroyEffectsPayload(BlockPos blockPos, VoxelShape shape, int state) implements CustomPacketPayload {

    public static final Type<DestroyEffectsPayload> TYPE = new Type<>(FurnitureMod.id("destroy_effects"));

    private static final StreamCodec<ByteBuf, VoxelShape> VOXEL_SHAPE_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull VoxelShape decode(@NotNull ByteBuf buffer) {
            VoxelShape shape = Shapes.empty();
            int size = buffer.readInt();
            for(int i = 0; i < size; i++) {
                shape = Shapes.joinUnoptimized(shape, Shapes.create(buffer.readDouble(), buffer.readDouble(), buffer.readDouble(), buffer.readDouble(), buffer.readDouble(), buffer.readDouble()), BooleanOp.OR);
            }
            return shape;
        }

        @Override
        public void encode(@NotNull ByteBuf buffer, @NotNull VoxelShape value) {
            List<AABB> boxes = value.toAabbs();
            buffer.writeInt(boxes.size());
            for (AABB box : boxes) {
                buffer.writeDouble(box.minX);
                buffer.writeDouble(box.minY);
                buffer.writeDouble(box.minZ);
                buffer.writeDouble(box.maxX);
                buffer.writeDouble(box.maxY);
                buffer.writeDouble(box.maxZ);
            }
        }
    };


    public static final StreamCodec<RegistryFriendlyByteBuf, DestroyEffectsPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            DestroyEffectsPayload::blockPos,
            VOXEL_SHAPE_CODEC,
            DestroyEffectsPayload::shape,
            ByteBufCodecs.VAR_INT,
            DestroyEffectsPayload::state,
            DestroyEffectsPayload::new
    );

    public static void handleClient(DestroyEffectsPayload payload, Executor executor) {
        executor.execute(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            ClientLevel level = minecraft.level;
            if(level != null) {
                FurnitureBlock.destroyEffects(level, payload.blockPos, Block.stateById(payload.state), payload.shape);
            }

        });
    }

    public DestroyEffectsPayload(BlockPos blockPos, VoxelShape shape, BlockState state) {
        this(blockPos, shape, Block.getId(state));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
