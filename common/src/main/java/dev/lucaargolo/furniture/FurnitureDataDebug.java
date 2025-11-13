package dev.lucaargolo.furniture;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import dev.lucaargolo.furniture.block.base.SeatBlock;
import dev.lucaargolo.furniture.client.render.RenderHelper;
import dev.lucaargolo.furniture.item.FurnitureBlockItem;
import dev.lucaargolo.furniture.item.FurnitureConnectingBlockItem;
import dev.lucaargolo.furniture.utils.FurnitureUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.FastColor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;

public class FurnitureDataDebug {

    public static void renderFurnitureDataDebug(Level level, Camera camera, PoseStack poseStack, MultiBufferSource bufferSource) {
        Minecraft minecraft = Minecraft.getInstance();
        if(!minecraft.getEntityRenderDispatcher().shouldRenderHitBoxes()) {
            return;
        }

        VertexConsumer lineConsumer = bufferSource.getBuffer(RenderType.lines());

        if(camera.getEntity() instanceof LocalPlayer player) {
            Pair<FurnitureBlockItem, InteractionHand> holding = FurnitureBlockItem.getHoldingFurniture(player);
            if(holding != null && holding.getFirst() instanceof FurnitureConnectingBlockItem) {
                BlockPos blockPos = FurnitureConnectingBlockItem.getLastPosition(player);
                if(blockPos != null) {
                    Vec3 pos = Vec3.atLowerCornerOf(blockPos);
                    poseStack.pushPose();
                    poseStack.translate(pos.x - camera.getPosition().x, pos.y - camera.getPosition().y, pos.z - camera.getPosition().z);
                    LevelRenderer.renderLineBox(poseStack, lineConsumer, 0.25f, 0.25f, 0.25f, 0.75f, 0.75f, 0.75f, 1f, 1f, 1f, 1f);
                    poseStack.popPose();
                }
            }
        }

        List<ChunkPos> chunksToDebug = new ArrayList<>();
        ChunkPos centerPos = camera.getEntity().chunkPosition();
        int renderDistance = minecraft.options.getEffectiveRenderDistance();
        for(int x = -renderDistance; x <= renderDistance; x++) {
            for(int z = -renderDistance; z <= renderDistance; z++) {
                chunksToDebug.add(new ChunkPos(centerPos.x + x, centerPos.z + z));
            }
        }

        IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
        Level debugLevel;
        if(server != null && camera.getEntity().isShiftKeyDown()) {
            debugLevel = server.getLevel(level.dimension());
        }else{
            debugLevel = level;
        }

        chunksToDebug.forEach(chunkPos -> {
            ChunkFurnitureData chunkData = FurnitureMod.INSTANCE.getDataManager().getChunkData(debugLevel, chunkPos);
            chunkData.forEach((packedPos, packedLayers) -> {
                BlockPos blockPos = FurnitureUtils.unpackChunkLocalPos(chunkPos, packedPos);
                FurnitureData[] layers = FurnitureUtils.unpackFurnitureDataLayers(packedLayers);
                for(int layer = 0; layer < layers.length; layer++) {
                    renderFurnitureBlockDebug(debugLevel, blockPos, layers[layer], camera, poseStack, lineConsumer, layer == 0 ? 0xFFFF00 : layer == 1 ? 0xFF00FF : layer == 2 ? 0x00FFFF : 0x00FF00);
                }
                renderFurnitureShapeDebug(blockPos, chunkData.getCachedShape(blockPos), camera, poseStack, lineConsumer);
            });
        });
    }

    private static void renderFurnitureShapeDebug(BlockPos blockPos, VoxelShape shape, Camera camera, PoseStack poseStack, VertexConsumer lineConsumer) {
        if(shape != null) {
            Vec3 pos = Vec3.atLowerCornerOf(blockPos);
            poseStack.pushPose();
            poseStack.translate(pos.x- camera.getPosition().x, pos.y- camera.getPosition().y, pos.z- camera.getPosition().z);
            shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
                LevelRenderer.renderLineBox(poseStack, lineConsumer, minX, minY, minZ, maxX, maxY, maxZ, 0f, 0f, 1f, 1f);
            });
            poseStack.popPose();
        }
    }

    private static void renderFurnitureBlockDebug(Level level, BlockPos blockPos, FurnitureData data, Camera camera, PoseStack poseStack, VertexConsumer lineConsumer, int color) {
        Vec3 pos = Vec3.atLowerCornerOf(blockPos);

        poseStack.pushPose();
        poseStack.translate(pos.x-camera.getPosition().x, pos.y-camera.getPosition().y, pos.z-camera.getPosition().z);

        Direction toOriginal = data.getDirectionToOriginal();

        float red = FastColor.ARGB32.red(color)/255f;
        float green = FastColor.ARGB32.green(color)/255f;
        float blue = FastColor.ARGB32.blue(color)/255f;

        if(data.hasOriginal()) {
            BlockState state = level.getBlockState(blockPos);
            if(state.getBlock() instanceof SeatBlock block) {
                Vec3[] seats = block.getSeats();
                for(int i = 0; i < seats.length; i++) {
                    Vec3 position = block.getPositionForSeat(data, blockPos, state, i).subtract(pos);
                    AABB bounds = AABB.ofSize(position, 0.1, 0.1, 0.1);
                    LevelRenderer.renderLineBox(poseStack, lineConsumer, bounds.minX, bounds.minY, bounds.minZ, bounds.maxX, bounds.maxY, bounds.maxZ, 1f, level.isClientSide ? 1f : 0f, level.isClientSide ? 1f : 0f, 1f);
                }
            }
        }

        if(data.hasOriginal() || toOriginal != null) {
            LevelRenderer.renderLineBox(poseStack, lineConsumer, 0.001f, 0.001f, 0.001f, 0.999f, 0.999f, 0.999f, red, green, blue, 1f);
            if(toOriginal != null) {
                Vec3 vector = new Vec3(toOriginal.getStepX(), toOriginal.getStepY(), toOriginal.getStepZ()).multiply(0.5, 0.5, 0.5);
                RenderHelper.renderArrow(poseStack, lineConsumer, new Vec3(0.5, 0.5, 0.5), vector, red, green, blue, 1f);
            }
            if(data.hasOriginal()) {
                RenderHelper.renderCrossedCube(poseStack, lineConsumer, 0.001f, 0.001f, 0.001f, 0.999f, 0.999f, 0.999f, red, green, blue, 1f);
            }
        }

        poseStack.popPose();
    }

}
