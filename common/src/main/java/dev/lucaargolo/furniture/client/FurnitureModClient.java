package dev.lucaargolo.furniture.client;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.client.model.FurnitureUnbakedModel;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public abstract class FurnitureModClient {

    public static FurnitureModClient INSTANCE;

    public void init() {
        INSTANCE = this;
        registerModel(FurnitureMod.id("block/small_table"));
        replaceModel(FurnitureMod.id("block/furniture"), new FurnitureUnbakedModel());
    }

    public abstract void registerModel(ResourceLocation location);

    public abstract void replaceModel(ResourceLocation location, UnbakedModel model);

    @Nullable
    public abstract BakedModel getModel(ResourceLocation location);

}
