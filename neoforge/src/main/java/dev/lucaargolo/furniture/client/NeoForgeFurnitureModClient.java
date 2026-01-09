package dev.lucaargolo.furniture.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.lucaargolo.furniture.FurnitureData;
import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.NeoForgeFurnitureMod;
import dev.lucaargolo.furniture.attachment.impl.AnimationDataAttachment;
import dev.lucaargolo.furniture.block.FurnitureBlock;
import dev.lucaargolo.furniture.block.FurnitureFenceBlock;
import dev.lucaargolo.furniture.block.ModBlocks;
import dev.lucaargolo.furniture.client.model.FurnitureBakedModel;
import dev.lucaargolo.furniture.client.model.FurnitureFenceBakedModel;
import dev.lucaargolo.furniture.item.ModItems;
import dev.lucaargolo.furniture.mixin.LevelRendererAccessor;
import dev.lucaargolo.furniture.registry.ModBlockRegistry;
import dev.lucaargolo.furniture.registry.minecraft.MinecraftEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.lighting.LightPipelineAwareModelBlockRenderer;
import net.neoforged.neoforge.client.model.lighting.QuadLighter;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.commons.lang3.function.TriFunction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class NeoForgeFurnitureModClient extends FurnitureModClient {

    private final RandomSource random = RandomSource.create();

    @SuppressWarnings("rawtypes")
    private final Map<Supplier<MenuType>, MenuScreens.ScreenConstructor> menus = new HashMap<>();
    @SuppressWarnings("rawtypes")
    private final Map<Supplier<EntityType>, EntityRendererProvider> entities = new HashMap<>();
    @SuppressWarnings("rawtypes")
    private final Map<Supplier<BlockEntityType>, BlockEntityRendererProvider> blockEntities = new HashMap<>();

    public NeoForgeFurnitureModClient() {
        this.init();
        NeoForgeFurnitureMod.getModBus().addListener(this::onBlockColorsRegister);
        NeoForgeFurnitureMod.getModBus().addListener(this::onItemColorsRegister);
        NeoForgeFurnitureMod.getModBus().addListener(this::onModelRegister);
        NeoForgeFurnitureMod.getModBus().addListener(this::onRenderersRegister);
        NeoForgeFurnitureMod.getModBus().addListener(this::onMenuScreensRegister);
        NeoForge.EVENT_BUS.addListener(this::onMouseScrolling);
        NeoForge.EVENT_BUS.addListener(this::onDrawBlockHighlight);
        NeoForge.EVENT_BUS.addListener(this::onRenderLevelStage);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void registerMenuScreen(MinecraftEntry<MenuType<M>> type, TriFunction<M, Inventory, Component, U> factory) {
        menus.put(type::get, (menu, inventory, title) -> factory.apply((M) menu, inventory, title));
    }

    @Override
    protected <E extends Entity, P extends EntityRendererProvider<E>> void registerEntityRenderer(MinecraftEntry<EntityType<E>> type, P provider) {
        entities.put(type::get, provider);
    }

    @Override
    protected <E extends BlockEntity, P extends BlockEntityRendererProvider<E>> void registerBlockEntityRenderer(MinecraftEntry<BlockEntityType<E>> type, P provider) {
        blockEntities.put(type::get, provider);
    }

    @Override
    public void renderFurnitureModel(Level level, BlockPos pos, BlockState state, FurnitureData data, AnimationDataAttachment animations, PoseStack poseStack, VertexConsumer consumer, float partialTick, int packedLight, int packedColor, boolean lightPipelineAware) {
        Minecraft minecraft = Minecraft.getInstance();
        BlockRenderDispatcher dispatcher = minecraft.getBlockRenderer();
        BakedModel model = dispatcher.getBlockModel(state);

        ModelData modelData = ModelData.EMPTY.derive()
                .with(FurnitureBakedModel.COLOR_PROPERTY, minecraft.getBlockColors().getColor(state, level, pos, 0))
                .with(FurnitureBakedModel.DATA_PROPERTY, data)
                .with(FurnitureBakedModel.HAS_DATA_PROPERTY, true)
                .with(FurnitureBakedModel.ANIMATION_PROPERTY, animations)
                .with(FurnitureBakedModel.PARTIAL_TICK_PROPERTY, partialTick)
                .build();
        modelData = model.getModelData(level, pos, state, modelData);

        if(lightPipelineAware) {
            renderLightPipelineAwareFurnitureModel(model, modelData, level, pos, state, poseStack, consumer, packedColor);
        }else{
            renderStaticLightFurnitureModel(model, modelData, state, poseStack, consumer, packedLight, packedColor);
        }
    }

    private void renderStaticLightFurnitureModel(BakedModel model, ModelData modelData, BlockState state, PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedColor) {
        for (Direction direction : Direction.values()) {
            random.setSeed(42L);
            renderQuadList(model.getQuads(state, direction, random, modelData, null), poseStack, consumer, packedLight, packedColor);
        }
        random.setSeed(42L);
        renderQuadList(model.getQuads(state, null, random, modelData, null), poseStack, consumer, packedLight, packedColor);
    }

    private static void renderQuadList(List<BakedQuad> quads, PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedColor) {
        for (BakedQuad quad : quads) {
            consumer.putBulkData(poseStack.last(), quad, FastColor.ARGB32.red(packedColor)/255f, FastColor.ARGB32.green(packedColor)/255f, FastColor.ARGB32.blue(packedColor)/255f, FastColor.ARGB32.alpha(packedColor)/255f, packedLight, OverlayTexture.NO_OVERLAY);
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    private void renderLightPipelineAwareFurnitureModel(BakedModel model, ModelData modelData, Level level, BlockPos pos, BlockState state, PoseStack poseStack, VertexConsumer consumer, int packedColor) {
        LightPipelineAwareModelBlockRenderer renderer = (LightPipelineAwareModelBlockRenderer) Minecraft.getInstance().getBlockRenderer().getModelRenderer();
        PoseStack.Pose pose = poseStack.last();
        AtomicBoolean empty = new AtomicBoolean(true);
        boolean smoothLighter = Minecraft.useAmbientOcclusion();
        QuadLighter lighter = renderer.getQuadLighter(smoothLighter);
        AtomicReference<QuadLighter> flatLighter = new AtomicReference<>();

        random.setSeed(42L);
        renderLightPipelineAwareQuadList(renderer, model.getQuads(state, null, random, modelData, null), level, pos, state, pose, consumer, packedColor, empty, smoothLighter, lighter, flatLighter);

        for (Direction direction : Direction.values()) {
            random.setSeed(42L);
            renderLightPipelineAwareQuadList(renderer, model.getQuads(state, direction, random, modelData, null), level, pos, state, pose, consumer, packedColor, empty, smoothLighter, lighter, flatLighter);
        }

        lighter.reset();
        if (flatLighter.get() != null)
            flatLighter.get().reset();
    }

    @SuppressWarnings("UnstableApiUsage")
    private static void renderLightPipelineAwareQuadList(LightPipelineAwareModelBlockRenderer renderer, List<BakedQuad> quads, Level level, BlockPos pos, BlockState state, PoseStack.Pose pose, VertexConsumer consumer, int packedColor, AtomicBoolean empty, boolean smoothLighter, QuadLighter lighter, AtomicReference<QuadLighter> flatLighter) {
        if (!quads.isEmpty()) {
            if (empty.get()) {
                empty.set(false);
                lighter.setup(level, pos, state);
            }
            for (BakedQuad quad : quads) {
                if (smoothLighter && !quad.hasAmbientOcclusion()) {
                    if (flatLighter.get() == null) {
                        flatLighter.set(renderer.getQuadLighter(false));
                        flatLighter.get().setup(level, pos, state);
                    }
                    processLightPipelineAwareQuad(flatLighter.get(), consumer, pose, quad, packedColor);
                } else {
                    processLightPipelineAwareQuad(lighter, consumer, pose, quad, packedColor);
                }
            }
        }
    }

    private static void processLightPipelineAwareQuad(QuadLighter lighter, VertexConsumer consumer, PoseStack.Pose pose, BakedQuad quad, int packedColor) {
        lighter.computeLightingForQuad(quad);
        consumer.putBulkData(pose, quad, lighter.getComputedBrightness(), FastColor.ARGB32.red(packedColor)/255f, FastColor.ARGB32.green(packedColor)/255f, FastColor.ARGB32.blue(packedColor)/255f, FastColor.ARGB32.alpha(packedColor)/255f, lighter.getComputedLightmap(), OverlayTexture.NO_OVERLAY, true);
    }

    @SubscribeEvent
    public void onBlockColorsRegister(RegisterColorHandlersEvent.Block event) {
        ModBlocks.REGISTRY.getEntries().forEach(entry -> {
            if (entry.getTintColor() != null)
                event.register(entry.getTintColor()::getColor, entry.get());
        });
    }

    @SubscribeEvent
    public void onItemColorsRegister(RegisterColorHandlersEvent.Item event) {
        ModItems.REGISTRY.getEntries().forEach(entry -> {
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
                if(entry != null) {
                    if(entry.get() instanceof FurnitureFenceBlock) {
                        mapEntry.setValue(new FurnitureFenceBakedModel(mapEntry.getValue()));
                    }else if(entry.get() instanceof FurnitureBlock) {
                        mapEntry.setValue(new FurnitureBakedModel(mapEntry.getValue()));
                    }
                }
            }
        }
    }

    @SubscribeEvent
    @SuppressWarnings("unchecked")
    public void onRenderersRegister(EntityRenderersEvent.RegisterRenderers event) {
        this.entities.forEach((type, provider) -> {
            event.registerEntityRenderer(type.get(), provider);
        });
        this.blockEntities.forEach((type, provider) -> {
            event.registerBlockEntityRenderer(type.get(), provider);
        });
    }

    @SubscribeEvent
    @SuppressWarnings("unchecked")
    public void onMenuScreensRegister(RegisterMenuScreensEvent event) {
        this.menus.forEach((type, factory) -> {
            event.register(type.get(), factory);
        });
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
            this.onFinishTranslucentLayer(levelRenderer, event.getCamera(), event.getPoseStack(), event.getPartialTick().getGameTimeDeltaTicks());
        }
    }

}
