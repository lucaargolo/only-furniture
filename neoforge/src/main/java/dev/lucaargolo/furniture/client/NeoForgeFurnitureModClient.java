package dev.lucaargolo.furniture.client;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;

public class NeoForgeFurnitureModClient extends FurnitureModClient {

    public NeoForgeFurnitureModClient() {
        this.init();
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onRenderLevelStage(RenderLevelStageEvent event) {
        if(event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            renderFurniturePreview(event.getPoseStack(), event.getPartialTick().getGameTimeDeltaPartialTick(false));
        }
    }

}
