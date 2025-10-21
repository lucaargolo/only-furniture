package dev.lucaargolo.furniture.client.model;

import dev.lucaargolo.furniture.client.FurnitureModClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class FurnitureBakedModel implements BakedModel {

    private TextureAtlasSprite missingSprite;

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState blockState, @Nullable Direction direction, @NotNull RandomSource randomSource) {
        BakedModel bakedModel = getBakedModel(blockState);
        return bakedModel != null ? bakedModel.getQuads(blockState, direction, randomSource) : List.of();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleIcon() {
        if(missingSprite == null) {
            Minecraft minecraft = Minecraft.getInstance();
            ModelManager modelManager = minecraft.getModelManager();
            TextureAtlas textureAtlas = modelManager.getAtlas(InventoryMenu.BLOCK_ATLAS);
            missingSprite = textureAtlas.getSprite(MissingTextureAtlasSprite.getLocation());
        }
        return missingSprite;
    }

    @Override
    public @NotNull ItemTransforms getTransforms() {
        return ItemTransforms.NO_TRANSFORMS;
    }

    @Override
    public @NotNull ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

    @Nullable
    @SuppressWarnings("deprecation")
    protected static BakedModel getBakedModel(@Nullable BlockState state) {
        if(state != null) {
            Block block = state.getBlock();
            ResourceLocation location = block.builtInRegistryHolder().key().location().withPrefix("block/");
            return FurnitureModClient.INSTANCE.getModelManager().getModel(location);
        }else{
            return null;
        }
    }

}
