package dev.lucaargolo.furniture.client;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;

public class NeoForgeFurnitureModClient extends FurnitureModClient {

    public NeoForgeFurnitureModClient() {
        this.init();
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        boolean result = this.onMouseScroll(event.getScrollDeltaX(), event.getScrollDeltaY());
        event.setCanceled(result);
    }

    @SubscribeEvent
    public void onRenderLevelStage(RenderLevelStageEvent event) {
        if(event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            this.renderFurniturePreview(event.getPoseStack());
        }
    }

}
