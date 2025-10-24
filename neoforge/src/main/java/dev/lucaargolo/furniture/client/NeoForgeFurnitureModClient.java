package dev.lucaargolo.furniture.client;

import dev.lucaargolo.furniture.mixin.LevelRendererAccessor;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ChunkEvent;

public class NeoForgeFurnitureModClient extends FurnitureModClient {

    public NeoForgeFurnitureModClient() {
        this.init();
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load event) {
        if(event.getLevel() instanceof Level level && level.isClientSide) {
            this.onClientChunkWatch(level, event.getChunk().getPos());
        }
    }

    @SubscribeEvent
    public void onChunkUnload(ChunkEvent.Unload event) {
        if(event.getLevel() instanceof Level level && level.isClientSide) {
            this.onClientChunkUnwatch(level, event.getChunk().getPos());
        }
    }

    @SubscribeEvent
    public void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        boolean result = this.onMouseScroll(event.getScrollDeltaX(), event.getScrollDeltaY());
        event.setCanceled(result);
    }

    @SubscribeEvent
    public void onDrawBlockHighlight(RenderHighlightEvent.Block event) {
        LevelRendererAccessor levelRenderer = (LevelRendererAccessor) event.getLevelRenderer();
        boolean result = this.onDrawBlockOutline(levelRenderer, event.getCamera(), event.getTarget().getBlockPos(), levelRenderer.getLevel().getBlockState(event.getTarget().getBlockPos()), event.getPoseStack(), event.getMultiBufferSource());
        event.setCanceled(result);
    }

    @SubscribeEvent
    public void onRenderLevelStage(RenderLevelStageEvent event) {
        if(event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            LevelRendererAccessor levelRenderer = (LevelRendererAccessor) event.getLevelRenderer();
            this.onFinishTranslucentLayer(levelRenderer, event.getCamera(), event.getPoseStack());
        }
    }

}
