package dev.lucaargolo.furniture.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.lucaargolo.furniture.FurnitureData;
import dev.lucaargolo.furniture.attachment.ModDataAttachments;
import dev.lucaargolo.furniture.attachment.impl.AnimationDataAttachment;
import dev.lucaargolo.furniture.block.FurnitureBlock;
import dev.lucaargolo.furniture.block.entity.FurnitureBlockEntity;
import dev.lucaargolo.furniture.client.FurnitureModClient;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class FurnitureBlockEntityRenderer implements BlockEntityRenderer<FurnitureBlockEntity> {

    private static final long DELAY_TIME = 500L;
    private static final Long2LongOpenHashMap DELAY_MAP = new Long2LongOpenHashMap();

    public FurnitureBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(@NotNull FurnitureBlockEntity blockEntity, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        Level level = blockEntity.getLevel();
        if(level != null) {
            BlockState state = blockEntity.getBlockState();
            BlockPos pos = blockEntity.getBlockPos();
            Block block = state.getBlock();
            boolean isDelayed = isDelayed(pos);
            if(block instanceof FurnitureBlock furniture && (isDelayed || furniture.shouldRenderBlockEntity(blockEntity))) {
                FurnitureData data = FurnitureData.getOriginal(level, pos);
                if(data.hasOriginal()) {
                    VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.translucent());
                    AnimationDataAttachment animations = ModDataAttachments.ANIMATION_DATA.get(blockEntity);
                    poseStack.pushPose();
                    poseStack.translate(0.5, 0.5, 0.5);
                    poseStack.scale(1.001f, 1.001f, 1.001f);
                    poseStack.translate(-0.5, -0.5, -0.5);
                    FurnitureModClient.getInstance().renderFurnitureModel(blockEntity.getLevel(), blockEntity.getBlockPos(), state, data, animations, poseStack, vertexConsumer, partialTick, packedLight, 0xFFFFFFFF, true);
                    poseStack.popPose();
                }
                if(!isDelayed) {
                    DELAY_MAP.put(pos.asLong(), System.currentTimeMillis());
                }
            }else{
                DELAY_MAP.remove(pos.asLong());
            }
        }
    }

    private static boolean isDelayed(BlockPos pos) {
        long current = System.currentTimeMillis();
        long time = DELAY_MAP.getOrDefault(pos.asLong(), 0L);
        return current - time < DELAY_TIME;
    }

}
