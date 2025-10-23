package dev.lucaargolo.furniture.client;

import dev.lucaargolo.furniture.mixin.LevelRendererAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;

public class FabricFurnitureModClient extends FurnitureModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        this.init();
        WorldRenderEvents.BLOCK_OUTLINE.register(this::onBlockOutline);
        WorldRenderEvents.AFTER_TRANSLUCENT.register(this::onAfterTranslucent);
        ClientChunkEvents.CHUNK_UNLOAD.register(this::onChunkUnload);
        ClientPlayConnectionEvents.DISCONNECT.register(this::onDisconnect);
    }

    private void onDisconnect(ClientPacketListener handler, Minecraft client) {
        this.onDisconnect();
    }

    private boolean onBlockOutline(WorldRenderContext worldContext, WorldRenderContext.BlockOutlineContext outlineContext) {
        return !this.onDrawBlockOutline((LevelRendererAccessor) worldContext.worldRenderer(), worldContext.camera(), outlineContext.blockPos(), outlineContext.blockState(), worldContext.matrixStack(), worldContext.consumers());
    }

    private void onAfterTranslucent(WorldRenderContext context) {
        this.onFinishTranslucentLayer((LevelRendererAccessor) context.worldRenderer(), context.camera(), context.matrixStack());
    }

}

