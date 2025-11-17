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
import dev.lucaargolo.furniture.registry.minecraft.MinecraftEntry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.function.TriFunction;

public class FabricFurnitureModClient extends FurnitureModClient implements ClientModInitializer {

    private final RandomSource random = RandomSource.create();

    @Override
    public void onInitializeClient() {
        this.init();
        this.registerModelPlugins();
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
            if(entry.get() instanceof FurnitureBlock furniture && furniture.getBehaviours(PlantBehaviour.class).length > 0) {
                BlockRenderLayerMap.INSTANCE.putBlock(entry.get(), RenderType.cutout());
            }
        });
    }

    @Override
    protected <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void registerMenuScreen(MinecraftEntry<MenuType<M>> type, TriFunction<M, Inventory, Component, U> factory) {
        MenuScreens.register(type.get(), factory::apply);
    }

    @Override
    protected <E extends Entity, P extends EntityRendererProvider<E>> void registerEntityRenderer(MinecraftEntry<EntityType<E>> type, P provider) {
        EntityRendererRegistry.register(type.get(), provider);
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

    private boolean onBlockOutline(WorldRenderContext worldContext, WorldRenderContext.BlockOutlineContext outlineContext) {
        return !this.onDrawBlockOutline((LevelRendererAccessor) worldContext.worldRenderer(), worldContext.camera(), outlineContext.blockPos(), outlineContext.blockState(), worldContext.matrixStack(), worldContext.consumers());
    }

    private void onAfterTranslucent(WorldRenderContext context) {
        this.onFinishTranslucentLayer((LevelRendererAccessor) context.worldRenderer(), context.camera(), context.matrixStack());
    }

}

