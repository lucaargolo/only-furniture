package dev.lucaargolo.furniture.client;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.block.FurnitureBlock;
import dev.lucaargolo.furniture.client.render.ModRenderTypeManager;
import dev.lucaargolo.furniture.client.render.ModShaderManager;
import dev.lucaargolo.furniture.entity.ModEntityTypes;
import dev.lucaargolo.furniture.item.FurnitureBlockItem;
import dev.lucaargolo.furniture.mixin.LevelRendererAccessor;
import dev.lucaargolo.furniture.utils.LocalFurnitureData;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.BiConsumer;

public abstract class FurnitureModClient {

    public static FurnitureModClient INSTANCE;

    private final MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(new ByteBufferBuilder(128));

    private final ModShaderManager shaderManager = FurnitureMod.INSTANCE.loadPlatformClass(ModShaderManager.class);
    private final ModRenderTypeManager renderTypeManager = FurnitureMod.INSTANCE.loadPlatformClass(ModRenderTypeManager.class);

    public final void init() {
        INSTANCE = this;
        shaderManager.init();
    }

    public ModRenderTypeManager getRenderTypeManager() {
        return renderTypeManager;
    }

    public final void onRegisterEntityRenderers(BiConsumer<EntityType<?>, EntityRendererProvider<?>> consumer) {
        consumer.accept(ModEntityTypes.SEAT.get(), NoopRenderer::new);
    }

    public final void onClientChunkWatch(Level level, ChunkPos pos) {
        LocalFurnitureData.watchChunk(level.dimension(), pos);
    }

    public final void onClientChunkUnwatch(Level level, ChunkPos pos) {
        LocalFurnitureData.unwatchChunk(level.dimension(), pos);
    }

    public final void onDisconnect() {
        LocalFurnitureData.unwatchWorld();
    }

    public final boolean onMouseScroll(double deltaX, double deltaY) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if(player != null) {
            return FurnitureBlockItem.rotateFurniture(player, deltaY);
        }
        return false;
    }

    public final boolean onDrawBlockOutline(LevelRendererAccessor levelRenderer, Camera camera, BlockPos pos, BlockState state, PoseStack poseStack, MultiBufferSource bufferSource) {
        if(state.getBlock() instanceof FurnitureBlock block) {
            return block.renderFurnitureOutline(levelRenderer.getLevel(), camera, pos, state, poseStack, bufferSource);
        }else{
            return false;
        }
    }

    public final void onFinishTranslucentLayer(LevelRendererAccessor levelRenderer, Camera camera, PoseStack poseStack) {
        FurnitureBlockItem.renderFurniturePreview(levelRenderer.getLevel(), camera, poseStack, bufferSource);
        LocalFurnitureData.renderFurnitureDataDebug(levelRenderer.getLevel(), camera, poseStack, bufferSource);
        bufferSource.endBatch();
    }



}
