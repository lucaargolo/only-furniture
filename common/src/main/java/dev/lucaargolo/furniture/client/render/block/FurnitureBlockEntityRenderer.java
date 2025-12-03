package dev.lucaargolo.furniture.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.lucaargolo.furniture.FurnitureData;
import dev.lucaargolo.furniture.block.FurnitureBlock;
import dev.lucaargolo.furniture.block.entity.FurnitureBlockEntity;
import dev.lucaargolo.furniture.client.utils.GroupedBakedQuad;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class FurnitureBlockEntityRenderer implements BlockEntityRenderer<FurnitureBlockEntity> {

    private final RandomSource random = RandomSource.create();
    private final BlockRenderDispatcher blockRenderDispatcher;

    public FurnitureBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.blockRenderDispatcher = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(@NotNull FurnitureBlockEntity blockEntity, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        Level level = blockEntity.getLevel();
        if(level != null) {
            BlockState state = blockEntity.getBlockState();
            BlockPos pos = blockEntity.getBlockPos();
            Block block = state.getBlock();
            if(block instanceof FurnitureBlock furniture && furniture.shouldRenderBlockEntity(level, pos, state)) {
                FurnitureData data = FurnitureData.getOriginal(level, pos);
                if(data.hasOriginal()) {
                    float offset = ((((pos.getX() & 1) << 2) | ((pos.getY() & 1) << 1) | (pos.getZ() & 1)) - 3.5f) * 0.001f;

                    poseStack.pushPose();
                    poseStack.translate(data.getX(state), data.getY(state), data.getZ(state));
                    poseStack.translate(0.5f, 0.5f, 0.5f);
                    poseStack.mulPose(data.getRotation(state));
                    poseStack.scale(1f + offset, 1f + offset, 1f + offset);
                    poseStack.translate(-0.5f, -0.5f, -0.5f);

                    BakedModel model = this.blockRenderDispatcher.getBlockModel(state);

                    List<BakedQuad> quads = new ArrayList<>(model.getQuads(state, null, random));
                    for (Direction direction : Direction.values()) {
                        quads.addAll(model.getQuads(state, direction, random));
                    }

                    VertexConsumer consumer = bufferSource.getBuffer(RenderType.entitySolid(InventoryMenu.BLOCK_ATLAS));
                    for (BakedQuad quad : quads) {
                        String group = ((GroupedBakedQuad) quad).furniture$getGroupHint();
                        if(group != null && group.startsWith("top.door")) {

                        }else{
                            consumer.putBulkData(poseStack.last(), quad, 1f, 1f, 1f, 1f, packedLight, packedOverlay);
                        }
                    }

                    poseStack.popPose();
                }
            }
        }

    }

}
