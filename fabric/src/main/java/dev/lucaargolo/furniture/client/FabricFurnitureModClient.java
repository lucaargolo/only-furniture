package dev.lucaargolo.furniture.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.lucaargolo.furniture.FurnitureData;
import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.block.FurnitureBlock;
import dev.lucaargolo.furniture.block.FurnitureFenceBlock;
import dev.lucaargolo.furniture.block.ModBlocks;
import dev.lucaargolo.furniture.block.behaviour.PlantBehaviour;
import dev.lucaargolo.furniture.client.model.FurnitureBakedModel;
import dev.lucaargolo.furniture.client.model.FurnitureFenceBakedModel;
import dev.lucaargolo.furniture.client.utils.VanillaRenderContext;
import dev.lucaargolo.furniture.item.ModItems;
import dev.lucaargolo.furniture.mixin.LevelRendererAccessor;
import dev.lucaargolo.furniture.registry.ModBlockRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class FabricFurnitureModClient extends FurnitureModClient implements ClientModInitializer {

    private static final RandomSource random = RandomSource.create();

    @Override
    public void onInitializeClient() {
        this.init();
        this.registerModelPlugins();
        this.registerEntityRenderers();
        WorldRenderEvents.BLOCK_OUTLINE.register(this::onBlockOutline);
        WorldRenderEvents.AFTER_TRANSLUCENT.register(this::onAfterTranslucent);
        ModBlocks.REGISTRY.getEntries().forEach(entry -> {
            if (entry.getTintColor() != null)
                ColorProviderRegistry.BLOCK.register(entry.getTintColor()::getColor, entry.get());
        });
        ModItems.REGISTRY.getEntries().forEach(entry -> {
            if (entry.getTintColor() != null)
                ColorProviderRegistry.ITEM.register(entry.getTintColor()::getColor, entry.get());
        });
        //TODO: Figure it out if its possible to use FAPI to render other render types in the baked model.
        ModBlocks.REGISTRY.getEntries().forEach(entry -> {
            if(entry.get() instanceof FurnitureBlock furniture && furniture.getInteractions(PlantBehaviour.class).length > 0) {
                BlockRenderLayerMap.INSTANCE.putBlock(entry.get(), RenderType.cutout());
            }
        });
    }

    @Override
    protected void renderFurnitureModel(Level level, BlockPos pos, FurnitureData data, BlockState state, PoseStack poseStack, VertexConsumer consumer, int packedColor) {
        Minecraft minecraft = Minecraft.getInstance();

        BlockRenderDispatcher dispatcher = minecraft.getBlockRenderer();
        BakedModel model = dispatcher.getBlockModel(state);

        if(model instanceof FurnitureBakedModel furnitureModel) {
            RenderContext render = VanillaRenderContext.of(poseStack, consumer, LightTexture.FULL_BRIGHT, packedColor);
            furnitureModel.emitBlockQuads(level, state, pos, () -> random, render, data);
        }
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
                        if(entry != null) {
                            if(entry.get() instanceof FurnitureFenceBlock)
                                return new FurnitureFenceBakedModel(model);
                            if(entry.get() instanceof FurnitureBlock)
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

    private boolean onBlockOutline(WorldRenderContext worldContext, WorldRenderContext.BlockOutlineContext outlineContext) {
        return !this.onDrawBlockOutline((LevelRendererAccessor) worldContext.worldRenderer(), worldContext.camera(), outlineContext.blockPos(), outlineContext.blockState(), worldContext.matrixStack(), worldContext.consumers());
    }

    private void onAfterTranslucent(WorldRenderContext context) {
        this.onFinishTranslucentLayer((LevelRendererAccessor) context.worldRenderer(), context.camera(), context.matrixStack());
    }

}

