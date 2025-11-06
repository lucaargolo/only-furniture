package dev.lucaargolo.furniture.client;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.block.FurnitureBlock;
import dev.lucaargolo.furniture.block.ModBlocks;
import dev.lucaargolo.furniture.client.model.FurnitureBakedModel;
import dev.lucaargolo.furniture.item.ModItems;
import dev.lucaargolo.furniture.mixin.LevelRendererAccessor;
import dev.lucaargolo.furniture.registry.ModBlockRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

public class FabricFurnitureModClient extends FurnitureModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        this.init();
        this.registerModelPlugins();
        this.registerEntityRenderers();
        ClientChunkEvents.CHUNK_LOAD.register(this::onChunkLoad);
        ClientChunkEvents.CHUNK_UNLOAD.register(this::onChunkUnload);
        ClientPlayConnectionEvents.DISCONNECT.register(this::onDisconnect);
        WorldRenderEvents.BLOCK_OUTLINE.register(this::onBlockOutline);
        WorldRenderEvents.AFTER_TRANSLUCENT.register(this::onAfterTranslucent);
        ModBlocks.REGISTRY.forEach(entry -> {
            if (entry.getTintColor() != null)
                ColorProviderRegistry.BLOCK.register(entry.getTintColor()::getColor, entry.get());
        });
        ModItems.REGISTRY.forEach(entry -> {
            if (entry.getTintColor() != null)
                ColorProviderRegistry.ITEM.register(entry.getTintColor()::getColor, entry.get());
        });
    }

    private void registerModelPlugins() {
        ModelLoadingPlugin.register(plugin -> {
            plugin.modifyModelAfterBake().register((model, context) -> {
                ModelResourceLocation location = context.topLevelId();
                if(location != null) {
                    String namespace = location.id().getNamespace();
                    String path = location.id().getPath();
                    String variant = location.variant();
                    if(namespace.equals(FurnitureMod.MOD_ID) && !variant.equals("inventory")) {
                        ModBlockRegistry.BlockEntry<?> entry = ModBlocks.REGISTRY.get(path);
                        if(entry != null && entry.get() instanceof FurnitureBlock) {
                            return new FurnitureBakedModel(model);
                        }
                    }
                }
                return model;
            });
        });
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void registerEntityRenderers() {
        this.onRegisterEntityRenderers((entityType, entityRendererProvider) -> {
            EntityRendererRegistry.register((EntityType) entityType, (EntityRendererProvider) entityRendererProvider);
        });
    }

    private void onChunkLoad(Level level, LevelChunk chunk) {
        this.onClientChunkWatch(level, chunk.getPos());
    }

    private void onChunkUnload(Level level, LevelChunk chunk) {
        this.onClientChunkUnwatch(level, chunk.getPos());
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

