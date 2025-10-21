package dev.lucaargolo.furniture.client.model;

import dev.lucaargolo.furniture.FurnitureMod;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class FurnitureUnbakedModel implements UnbakedModel {

    @Override
    public @NotNull Collection<ResourceLocation> getDependencies() {
        return List.of();
    }

    @Override
    public void resolveParents(@NotNull Function<ResourceLocation, UnbakedModel> pResolver) {

    }

    @Override
    public @Nullable BakedModel bake(@NotNull ModelBaker pBaker, @NotNull Function<Material, TextureAtlasSprite> pSpriteGetter, @NotNull ModelState pState) {
        return FurnitureMod.INSTANCE.loadPlatformClass(FurnitureBakedModel.class);
    }

}
