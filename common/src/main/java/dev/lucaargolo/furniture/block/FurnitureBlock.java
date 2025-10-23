package dev.lucaargolo.furniture.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.lucaargolo.furniture.data.FurnitureData;
import dev.lucaargolo.furniture.mixin.LevelRendererAccessor;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class FurnitureBlock extends Block {

    private final VoxelShape shape;

    public FurnitureBlock(Block base, VoxelShape... shapes) {
        super(BlockBehaviour.Properties.ofFullCopy(base).noOcclusion());
        VoxelShape shape = Shapes.empty();
        for (VoxelShape s : shapes) {
            shape = Shapes.join(shape, s, BooleanOp.OR);
        }
        this.shape = shape;
    }

    @Override
    protected void onRemove(@NotNull BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull BlockState pNewState, boolean pMovedByPiston) {
        super.onRemove(pState, pLevel, pPos, pNewState, pMovedByPiston);
        FurnitureData.set(pLevel, pPos, FurnitureData.DEFAULT);
    }

    @Override
    protected @NotNull VoxelShape getShape(@NotNull BlockState pState, @NotNull BlockGetter pLevel, @NotNull BlockPos pPos, @NotNull CollisionContext pContext) {
        FurnitureData data = FurnitureData.get(pLevel, pPos);
        return this.shape.move(data.getX(), 0.0, data.getZ());
    }

    public static boolean renderFurnitureOutline(LevelRendererAccessor levelRenderer, Camera camera, BlockPos pos, BlockState state, PoseStack poseStack, MultiBufferSource bufferSource) {
        if(state.getBlock() instanceof FurnitureBlock) {
            FurnitureData data = FurnitureData.get(levelRenderer.getLevel(), pos);
            if(data.getRotation() != 0f) {
                VertexConsumer consumer = bufferSource.getBuffer(RenderType.lines());

                Vec3 offsetPos = Vec3.atCenterOf(pos).add(data.getX(), 0.0, data.getZ());

                poseStack.pushPose();
                poseStack.translate(offsetPos.x-camera.getPosition().x, offsetPos.y-camera.getPosition().y, offsetPos.z-camera.getPosition().z);
                poseStack.mulPose(Axis.YP.rotationDegrees(data.getRotation()));

                renderHitOutline(levelRenderer, poseStack, consumer, camera.getEntity(), offsetPos.x, offsetPos.y, offsetPos.z, pos, state);
                poseStack.popPose();
                return true;
            }
        }
        return false;
    }

    private static void renderHitOutline(LevelRendererAccessor levelRenderer, PoseStack pPoseStack, VertexConsumer pConsumer, Entity pEntity, double pCamX, double pCamY, double pCamZ, BlockPos pPos, BlockState pState) {
        LevelRendererAccessor.invokeRenderShape(pPoseStack, pConsumer, pState.getShape(levelRenderer.getLevel(), pPos, CollisionContext.of(pEntity)), (double)pPos.getX() - pCamX, (double)pPos.getY() - pCamY, (double)pPos.getZ() - pCamZ, 0.0F, 0.0F, 0.0F, 0.4F);
    }
}
