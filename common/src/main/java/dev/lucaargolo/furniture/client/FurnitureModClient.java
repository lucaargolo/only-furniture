package dev.lucaargolo.furniture.client;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import dev.lucaargolo.furniture.FurnitureData;
import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.attachment.impl.AnimationDataAttachment;
import dev.lucaargolo.furniture.block.FurnitureBlock;
import dev.lucaargolo.furniture.block.FurnitureConnectingBlock;
import dev.lucaargolo.furniture.block.entity.ModBlockEntityTypes;
import dev.lucaargolo.furniture.client.render.ModRenderTypeManager;
import dev.lucaargolo.furniture.client.render.ModShaderManager;
import dev.lucaargolo.furniture.client.render.block.FurnitureBlockEntityRenderer;
import dev.lucaargolo.furniture.client.render.screen.StorageMenuScreen;
import dev.lucaargolo.furniture.client.sound.InstrumentSoundInstance;
import dev.lucaargolo.furniture.entity.ModEntityTypes;
import dev.lucaargolo.furniture.item.FurnitureBlockItem;
import dev.lucaargolo.furniture.item.FurnitureConnectingBlockItem;
import dev.lucaargolo.furniture.menu.ModMenuTypes;
import dev.lucaargolo.furniture.mixin.LevelRendererAccessor;
import dev.lucaargolo.furniture.registry.minecraft.MinecraftEntry;
import dev.lucaargolo.furniture.utils.shape.FurnitureShape;
import dev.lucaargolo.furniture.utils.shape.RotatedShape;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.FastColor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.Nullable;

public abstract class FurnitureModClient {

    private static FurnitureModClient instance;

    private final MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(new ByteBufferBuilder(128));

    private final ModShaderManager shaderManager;
    private final ModRenderTypeManager renderTypeManager;

    public FurnitureModClient() {
        instance = this;
        this.shaderManager = FurnitureMod.loadPlatformClass(ModShaderManager.class);
        this.renderTypeManager = FurnitureMod.loadPlatformClass(ModRenderTypeManager.class);
    }

    protected final void init() {
        this.shaderManager.init();
        this.registerMenuScreen(ModMenuTypes.STORAGE, StorageMenuScreen::new);
        this.registerEntityRenderer(ModEntityTypes.SEAT, NoopRenderer::new);
        this.registerBlockEntityRenderer(ModBlockEntityTypes.FURNITURE, FurnitureBlockEntityRenderer::new);
    }

    protected abstract <M extends AbstractContainerMenu, P extends Screen & MenuAccess<M>> void registerMenuScreen(MinecraftEntry<MenuType<M>> type, TriFunction<M, Inventory, Component, P> factory);

    protected abstract <E extends Entity, P extends EntityRendererProvider<E>> void registerEntityRenderer(MinecraftEntry<EntityType<E>> type, P provider);

    protected abstract <E extends BlockEntity, P extends BlockEntityRendererProvider<E>> void registerBlockEntityRenderer(MinecraftEntry<BlockEntityType<E>> type, P provider);

    public abstract void renderFurnitureModel(Level level, BlockPos pos, FurnitureData data, BlockState state, PoseStack poseStack, VertexConsumer consumer, float partialTick, @Nullable AnimationDataAttachment animations, int packedLight, int packedColor);

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

    public final void onFinishTranslucentLayer(LevelRendererAccessor levelRenderer, Camera camera, PoseStack poseStack, float partialTick) {
        FurnitureDataDebug.renderFurnitureDataDebug(levelRenderer.getLevel(), camera, poseStack, bufferSource);
        this.renderFurniturePreview(levelRenderer.getLevel(), camera, poseStack, bufferSource, partialTick);
        bufferSource.endBatch();
    }

    private void renderFurniturePreview(Level level, Camera camera, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick) {
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
                Pair<BlockState, Integer> pair = block.getStateAndLayerForPlacement(context, data);

                boolean isValidPlacement = true;
                BlockState state;
                if(pair.getFirst() == null || pair.getSecond() == -1) {
                    isValidPlacement = false;
                    state = block.defaultBlockState();
                    if(block.isWallBlock()) {
                        state = state.setValue(FurnitureBlock.FACING, context.getHorizontalDirection().getOpposite());
                    }
                }else{
                    state = pair.getFirst();
                }

                int color = !isValidPlacement || !placingState.canBeReplaced(context) ? 0xda3e44 : 0x5865f2;
                int packedColor = FastColor.ARGB32.color(120, color);

                if(blockItem instanceof FurnitureConnectingBlockItem connectingBlockItem && connectingBlockItem.getFurnitureBlock().getType().isDependentOnLastPosition()) {
                    BlockPos lastPosition = FurnitureConnectingBlockItem.getLastPosition(player);
                    if(lastPosition != null) {
                        BlockState lastState = level.getBlockState(lastPosition);
                        if (lastState.getBlock() instanceof FurnitureConnectingBlock lastBlock && lastBlock.getType().isDependentOnLastPosition()) {
                            FurnitureData lastData = FurnitureData.getOriginal(level, lastPosition);

                            BooleanProperty propertyToManuallyConnect = connectingBlockItem.manuallyConnectNeighbors(level, lastPosition, clickedPos, clickedState);
                            boolean isManuallyConnecting = !player.isShiftKeyDown() && propertyToManuallyConnect != null;
                            if (isManuallyConnecting) {
                                lastState = lastState.cycle(propertyToManuallyConnect);
                                poseStack.pushPose();
                                poseStack.translate(lastPosition.getX() - camera.getPosition().x, lastPosition.getY() - camera.getPosition().y, lastPosition.getZ() - camera.getPosition().z);
                                poseStack.translate(0.5, 0.5, 0.5);
                                poseStack.scale(1f - 0.005f, 1f - 0.005f, 1f - 0.005f);
                                poseStack.translate(-0.5, -0.5, -0.5);
                                renderFurnitureModel(level, lastPosition, lastData, lastState, poseStack, consumer, partialTick, null, LightTexture.FULL_BRIGHT, FastColor.ARGB32.color(120, 0x5865f2));
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
                renderFurnitureModel(level, placingPos, data, state, poseStack, consumer, partialTick, null, LightTexture.FULL_BRIGHT, packedColor);
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

    public static FurnitureModClient getInstance() {
        return instance;
    }

    public static void addTerrainParticle(ClientLevel level, BlockPos pos, BlockState state, double xPos, double yPos, double zPos, double xOffset, double yOffset, double zOffset) {
        TerrainParticle particle = new TerrainParticle(
                level, (double) pos.getX() + xPos, (double) pos.getY() + yPos, (double) pos.getZ() + zPos,
                xOffset - 0.5, yOffset - 0.5, zOffset - 0.5, state, pos
        );
        Minecraft.getInstance().particleEngine.add(particle);
    }

    public static void playInstrument(SoundEvent event, float pitch, int release) {
        Minecraft.getInstance().getSoundManager().queueTickingSound(new InstrumentSoundInstance(event, pitch, release));
    }

}
