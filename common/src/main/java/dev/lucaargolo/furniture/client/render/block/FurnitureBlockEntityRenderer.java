package dev.lucaargolo.furniture.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.lucaargolo.furniture.FurnitureData;
import dev.lucaargolo.furniture.attachment.ModDataAttachments;
import dev.lucaargolo.furniture.attachment.impl.AnimationDataAttachment;
import dev.lucaargolo.furniture.block.FurnitureBlock;
import dev.lucaargolo.furniture.block.entity.FurnitureBlockEntity;
import dev.lucaargolo.furniture.client.FurnitureModClient;
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

    public FurnitureBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(@NotNull FurnitureBlockEntity blockEntity, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        Level level = blockEntity.getLevel();
        if(level != null) {
            BlockState state = blockEntity.getBlockState();
            BlockPos pos = blockEntity.getBlockPos();
            Block block = state.getBlock();
            if(block instanceof FurnitureBlock furniture && furniture.shouldRenderBlockEntity(blockEntity)) {
                FurnitureData data = FurnitureData.getOriginal(level, pos);
                if(data.hasOriginal()) {
                    VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.translucent());
                    AnimationDataAttachment animations = ModDataAttachments.ANIMATION_DATA.get(blockEntity);
                    FurnitureModClient.getInstance().renderFurnitureModel(blockEntity.getLevel(), blockEntity.getBlockPos(), data, state, poseStack, vertexConsumer, partialTick, animations, packedLight, 0xFFFFFFFF);
                }
            }
        }

    }

}
