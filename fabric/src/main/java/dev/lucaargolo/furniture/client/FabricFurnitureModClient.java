package dev.lucaargolo.furniture.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FabricFurnitureModClient extends FurnitureModClient implements ClientModInitializer, ModelLoadingPlugin {

    public static FabricFurnitureModClient INSTANCE;

    private final List<ResourceLocation> modelsToRegister = new ArrayList<>();
    private final Map<ResourceLocation, UnbakedModel> modelsToReplace = new HashMap<>();

    @Override
    public void init() {
        INSTANCE = this;
        super.init();
        ModelLoadingPlugin.register(this);
    }

    @Override
    public void onInitializeClient() {
        this.init();
    }

    @Override
    public void onInitializeModelLoader(Context pluginContext) {
        pluginContext.addModels(modelsToRegister);
        pluginContext.resolveModel().register(( context) -> modelsToReplace.get(context.id()));
    }

    @Override
    public void registerModel(ResourceLocation location) {
        modelsToRegister.add(location);
    }

    @Override
    public void replaceModel(ResourceLocation location, UnbakedModel model) {
        modelsToReplace.put(location, model);
    }

    @Override
    @Nullable
    public BakedModel getModel(ResourceLocation location) {
        return Minecraft.getInstance().getModelManager().getModel(location);
    }

}
