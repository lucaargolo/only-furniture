package dev.lucaargolo.furniture.client.model;

import dev.lucaargolo.furniture.FurnitureMod;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public abstract class FurnitureModelManager {

    public void init() {
        registerModel(FurnitureMod.id("block/small_table"));
        replaceModel(FurnitureMod.id("block/furniture"), new FurnitureUnbakedModel());
    }

    public abstract void registerModel(ResourceLocation location);

    public abstract void replaceModel(ResourceLocation location, UnbakedModel model);

    @Nullable
    public abstract BakedModel getModel(ResourceLocation location);

    public abstract UnbakedModel getModelReplacement(ResourceLocation location);

}
