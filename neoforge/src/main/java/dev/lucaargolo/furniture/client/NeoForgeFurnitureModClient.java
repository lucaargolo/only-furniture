package dev.lucaargolo.furniture.client;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.NeoForgeFurnitureMod;
import dev.lucaargolo.furniture.block.FurnitureBlock;
import dev.lucaargolo.furniture.block.ModBlocks;
import dev.lucaargolo.furniture.client.model.FurnitureBakedModel;
import dev.lucaargolo.furniture.item.ModItems;
import dev.lucaargolo.furniture.mixin.LevelRendererAccessor;
import dev.lucaargolo.furniture.registry.ModBlockRegistry;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ChunkEvent;

import java.util.Map;

public class NeoForgeFurnitureModClient extends FurnitureModClient {

    public NeoForgeFurnitureModClient() {
        this.init();
        NeoForgeFurnitureMod.getModBus().addListener(this::onBlockColorsRegister);
        NeoForgeFurnitureMod.getModBus().addListener(this::onItemColorsRegister);
        NeoForgeFurnitureMod.getModBus().addListener(this::onModelRegister);
        NeoForgeFurnitureMod.getModBus().addListener(this::onRenderersRegister);
        NeoForge.EVENT_BUS.addListener(this::onChunkLoad);
        NeoForge.EVENT_BUS.addListener(this::onChunkUnload);
        NeoForge.EVENT_BUS.addListener(this::onLoggingOut);
        NeoForge.EVENT_BUS.addListener(this::onMouseScrolling);
        NeoForge.EVENT_BUS.addListener(this::onDrawBlockHighlight);
        NeoForge.EVENT_BUS.addListener(this::onRenderLevelStage);
    }

    @SubscribeEvent
    public void onBlockColorsRegister(RegisterColorHandlersEvent.Block event) {
        ModBlocks.REGISTRY.forEach(entry -> {
            if (entry.getTintColor() != null)
                event.register(entry.getTintColor()::getColor, entry.get());
        });
    }

    @SubscribeEvent
    public void onItemColorsRegister(RegisterColorHandlersEvent.Item event) {
        ModItems.REGISTRY.forEach(entry -> {
            if (entry.getTintColor() != null)
                event.register(entry.getTintColor()::getColor, entry.get());
        });
    }

    @SubscribeEvent
    public void onModelRegister(ModelEvent.ModifyBakingResult event) {
        for (Map.Entry<ModelResourceLocation, BakedModel> mapEntry : event.getModels().entrySet()) {
            ModelResourceLocation location = mapEntry.getKey();
            String namespace = location.id().getNamespace();
            String path = location.id().getPath();
            String variant = location.variant();
            if(namespace.equals(FurnitureMod.MOD_ID) && !variant.equals("inventory")) {
                ModBlockRegistry.BlockEntry<?> entry = ModBlocks.REGISTRY.get(path);
                if(entry != null && entry.get() instanceof FurnitureBlock) {
                    mapEntry.setValue(new FurnitureBakedModel(mapEntry.getValue()));
                }
            }
        }
    }

    @SubscribeEvent
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void onRenderersRegister(EntityRenderersEvent.RegisterRenderers event) {
        this.onRegisterEntityRenderers((entityType, entityRendererProvider) -> {
            event.registerEntityRenderer((EntityType) entityType, (EntityRendererProvider) entityRendererProvider);
        });
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
    public void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        this.onDisconnect();
    }

    @SubscribeEvent
    public void onMouseScrolling(InputEvent.MouseScrollingEvent event) {
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
