package dev.lucaargolo.furniture.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

import java.util.Objects;

public class FabricFurnitureModClient extends FurnitureModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        this.init();
        WorldRenderEvents.AFTER_TRANSLUCENT.register(this::onFinishTranslucent);
    }

    private void onFinishTranslucent(WorldRenderContext context) {
        renderFurniturePreview(Objects.requireNonNull(context.matrixStack()), context.tickCounter().getGameTimeDeltaPartialTick(false));
    }

}

