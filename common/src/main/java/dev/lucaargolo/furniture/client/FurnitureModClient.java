package dev.lucaargolo.furniture.client;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.block.FurnitureBlock;
import dev.lucaargolo.furniture.client.model.ModModelManager;
import dev.lucaargolo.furniture.client.render.ModRenderTypeManager;
import dev.lucaargolo.furniture.client.render.ModShaderManager;
import dev.lucaargolo.furniture.data.LocalFurnitureData;
import dev.lucaargolo.furniture.item.FurnitureBlockItem;
import dev.lucaargolo.furniture.mixin.LevelRendererAccessor;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

public abstract class FurnitureModClient {

    public static FurnitureModClient INSTANCE;

    private final MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(new ByteBufferBuilder(128));

    private final ModModelManager modelManager = FurnitureMod.INSTANCE.loadPlatformClass(ModModelManager.class);
    private final ModShaderManager shaderManager = FurnitureMod.INSTANCE.loadPlatformClass(ModShaderManager.class);
    private final ModRenderTypeManager renderTypeManager = FurnitureMod.INSTANCE.loadPlatformClass(ModRenderTypeManager.class);

    public final void init() {
        INSTANCE = this;
        modelManager.init();
        shaderManager.init();
    }

    public final ModModelManager getModelManager() {
        return modelManager;
    }

    public ModShaderManager getShaderManager() {
        return shaderManager;
    }

    public ModRenderTypeManager getRenderTypeManager() {
        return renderTypeManager;
    }

    public final void onChunkUnload(Level level, ChunkAccess chunk) {
        LocalFurnitureData.remove(level.dimension(), chunk.getPos().toLong());
    }

    public final void onDisconnect() {
        LocalFurnitureData.clear();
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
        return FurnitureBlock.renderFurnitureOutline(levelRenderer, camera, pos, state, poseStack, bufferSource);
    }

    public final void onFinishTranslucentLayer(LevelRendererAccessor levelRenderer, Camera camera, PoseStack poseStack) {
        FurnitureBlockItem.renderFurniturePreview(levelRenderer.getLevel(), camera, poseStack, bufferSource);
        LocalFurnitureData.renderFurnitureDebug(levelRenderer.getLevel(), camera, poseStack, bufferSource);
        bufferSource.endBatch();
    }



}
