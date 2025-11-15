package dev.lucaargolo.furniture.client;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import dev.lucaargolo.furniture.FurnitureData;
import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.block.FurnitureBlock;
import dev.lucaargolo.furniture.block.FurnitureConnectingBlock;
import dev.lucaargolo.furniture.client.render.ModRenderTypeManager;
import dev.lucaargolo.furniture.client.render.ModShaderManager;
import dev.lucaargolo.furniture.entity.ModEntityTypes;
import dev.lucaargolo.furniture.item.FurnitureBlockItem;
import dev.lucaargolo.furniture.item.FurnitureConnectingBlockItem;
import dev.lucaargolo.furniture.mixin.LevelRendererAccessor;
import dev.lucaargolo.furniture.utils.shape.FurnitureShape;
import dev.lucaargolo.furniture.utils.shape.RotatedShape;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.FastColor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.BiConsumer;

public abstract class FurnitureModClient {

    public static FurnitureModClient INSTANCE;

    private final MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(new ByteBufferBuilder(128));

    private final ModShaderManager shaderManager = FurnitureMod.INSTANCE.loadPlatformClass(ModShaderManager.class);
    private final ModRenderTypeManager renderTypeManager = FurnitureMod.INSTANCE.loadPlatformClass(ModRenderTypeManager.class);

    protected final void init() {
        INSTANCE = this;
        shaderManager.init();
    }

    public final void onRegisterEntityRenderers(BiConsumer<EntityType<?>, EntityRendererProvider<?>> consumer) {
        consumer.accept(ModEntityTypes.SEAT.get(), NoopRenderer::new);
    }

    public final boolean onMouseScroll(double deltaX, double deltaY) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if(player != null) {
            return FurnitureBlockItem.rotateFurniture(player, -deltaY);
        }
        return false;
    }

    public final boolean onDrawBlockOutline(LevelRendererAccessor levelRenderer, Camera camera, BlockPos pos, BlockState state, PoseStack poseStack, MultiBufferSource bufferSource) {
        if(state.getBlock() instanceof FurnitureBlock block) {
            return renderFurnitureOutline(block, levelRenderer.getLevel(), camera, pos, state, poseStack, bufferSource);
        }else{
            return false;
        }
    }

    public final void onFinishTranslucentLayer(LevelRendererAccessor levelRenderer, Camera camera, PoseStack poseStack) {
        FurnitureDataDebug.renderFurnitureDataDebug(levelRenderer.getLevel(), camera, poseStack, bufferSource);
        this.renderFurniturePreview(levelRenderer.getLevel(), camera, poseStack, bufferSource);
        bufferSource.endBatch();
    }

    protected abstract void renderFurnitureModel(Level level, BlockPos pos, FurnitureData data, BlockState state, PoseStack poseStack, VertexConsumer consumer, int packedColor);

    private void renderFurniturePreview(Level level, Camera camera, PoseStack poseStack, MultiBufferSource bufferSource) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        HitResult hitResult = minecraft.hitResult;
        if(player != null && level == player.level() && hitResult instanceof BlockHitResult blockHitResult && blockHitResult.getType() == HitResult.Type.BLOCK) {
            Pair<FurnitureBlockItem, InteractionHand> holding = FurnitureBlockItem.getHoldingFurniture(player);
            if(holding != null) {
                FurnitureBlockItem blockItem = holding.getFirst();
                FurnitureBlock block = blockItem.getFurnitureBlock();
                BlockPlaceContext context = new BlockPlaceContext(new UseOnContext(player, holding.getSecond(), blockHitResult));

                RenderType renderType = renderTypeManager.hologramTranslucent(InventoryMenu.BLOCK_ATLAS);
                VertexConsumer consumer = bufferSource.getBuffer(renderType);

                BlockPos clickedPos = blockHitResult.getBlockPos();
                BlockState clickedState = level.getBlockState(clickedPos);
                BlockPos placingPos = context.getClickedPos();
                BlockState placingState = level.getBlockState(placingPos);

                FurnitureData data = block.getFurnitureDataForPlacement(context);
                BlockState state = block.getStateForPlacement(context, data);
                boolean isValidPlacement = true;
                if(state == null) {
                    isValidPlacement = false;
                    state = block.defaultBlockState();
                    if(block.isWallBlock()) {
                        state = state.setValue(FurnitureBlock.FACING, context.getHorizontalDirection().getOpposite());
                    }
                }
                int color = !isValidPlacement || !placingState.canBeReplaced(context) ? 0xda3e44 : 0x5865f2;
                int packedColor = FastColor.ARGB32.color(120, color);

                if(blockItem instanceof FurnitureConnectingBlockItem connectingBlockItem && connectingBlockItem.getFurnitureBlock().getType().isDependentOnLastPosition()) {
                    BlockPos lastPosition = FurnitureConnectingBlockItem.getLastPosition(player);
                    if(lastPosition != null) {
                        BlockState lastState = level.getBlockState(lastPosition);
                        if (lastState.getBlock() instanceof FurnitureConnectingBlock lastBlock && lastBlock.getType().isDependentOnLastPosition()) {
                            FurnitureData lastData = FurnitureData.get(level, lastPosition, lastState.getValue(FurnitureBlock.LAYER));

                            BooleanProperty propertyToManuallyConnect = connectingBlockItem.manuallyConnectNeighbors(level, lastPosition, clickedPos, clickedState);
                            boolean isManuallyConnecting = !player.isShiftKeyDown() && propertyToManuallyConnect != null;
                            if (isManuallyConnecting) {
                                lastState = lastState.cycle(propertyToManuallyConnect);
                                poseStack.pushPose();
                                poseStack.translate(lastPosition.getX() - camera.getPosition().x, lastPosition.getY() - camera.getPosition().y, lastPosition.getZ() - camera.getPosition().z);
                                poseStack.translate(0.5, 0.5, 0.5);
                                poseStack.scale(1f - 0.005f, 1f - 0.005f, 1f - 0.005f);
                                poseStack.translate(-0.5, -0.5, -0.5);
                                renderFurnitureModel(level, lastPosition, lastData, lastState, poseStack, consumer, FastColor.ARGB32.color(120, 0x5865f2));
                                poseStack.popPose();
                                return;
                            }

                            if(!player.isShiftKeyDown() && clickedState.is(connectingBlockItem.getFurnitureBlock().getConnecting())) {
                                return;
                            }
                        }
                    }
                }


                poseStack.pushPose();
                poseStack.translate(placingPos.getX()-camera.getPosition().x, placingPos.getY()-camera.getPosition().y, placingPos.getZ()-camera.getPosition().z);
                poseStack.translate(0.5, 0.5, 0.5);
                poseStack.scale(1f + 0.005f, 1f + 0.005f, 1f + 0.005f);
                poseStack.translate(-0.5, -0.5, -0.5);
                renderFurnitureModel(level, placingPos, data, state, poseStack, consumer, packedColor);
                poseStack.popPose();
            }
        }
    }

    private boolean renderFurnitureOutline(FurnitureBlock block, ClientLevel level, Camera camera, BlockPos pos, BlockState state, PoseStack poseStack, MultiBufferSource bufferSource) {
        if(block instanceof FurnitureConnectingBlock furniture && furniture.getType().isDependentOnOriginalRotation()) return false;
        if(!(block.getShape(state, level, pos, CollisionContext.of(camera.getEntity())) instanceof FurnitureShape shape)) return false;
        if(shape.data().rotation() % 90f == 0f) return false;

        VoxelShape original = shape.shape();
        if(original instanceof RotatedShape rotated) {
            original = rotated.getOriginal();
        }

        FurnitureData data = shape.data();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lines());

        Vec3 offsetVec = Vec3.atLowerCornerOf(shape.offset());
        offsetVec = offsetVec.add(data.getX(state), data.getY(state), data.getZ(state));
        Vec3 offsetPos = Vec3.atCenterOf(pos).add(offsetVec);

        poseStack.pushPose();
        poseStack.translate(offsetPos.x - camera.getPosition().x, offsetPos.y - camera.getPosition().y, offsetPos.z - camera.getPosition().z);
        poseStack.mulPose(data.getRotation(state));

        LevelRendererAccessor.invokeRenderShape(poseStack, consumer, original, (double) pos.getX() - offsetPos.x, (double) pos.getY() - offsetPos.y, (double) pos.getZ() - offsetPos.z, 0.0F, 0.0F, 0.0F, 0.4F);
        poseStack.popPose();

        return true;
    }

}
