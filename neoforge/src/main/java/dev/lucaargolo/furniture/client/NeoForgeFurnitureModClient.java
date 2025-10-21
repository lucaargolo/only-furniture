package dev.lucaargolo.furniture.client;

import dev.lucaargolo.furniture.NeoForgeFurnitureMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class NeoForgeFurnitureModClient extends FurnitureModClient {

    public static NeoForgeFurnitureModClient INSTANCE;

    private final Map<ResourceLocation, ModelResourceLocation> modelsToRegister = new HashMap<>();
    private final Map<ResourceLocation, UnbakedModel> modelsToReplace = new HashMap<>();

    public NeoForgeFurnitureModClient() {
        this.init();
    }

    @Override
    public void init() {
        INSTANCE = this;
        super.init();
        NeoForgeFurnitureMod.INSTANCE.getModBus().register(this);
    }

    @SubscribeEvent
    public void onModelRegister(ModelEvent.RegisterAdditional event) {
        modelsToRegister.values().forEach(event::register);
    }

    @Override
    public void registerModel(ResourceLocation location) {
        modelsToRegister.put(location, ModelResourceLocation.standalone(location));
    }

    @Override
    public void replaceModel(ResourceLocation location, UnbakedModel model) {
        modelsToReplace.put(location, model);
    }

    @Override
    @Nullable
    public BakedModel getModel(ResourceLocation location) {
        ModelResourceLocation modelLocation = modelsToRegister.get(location);
        return modelLocation != null ? Minecraft.getInstance().getModelManager().getModel(modelLocation) : null;
    }

    @Nullable
    public UnbakedModel getModelReplacement(ResourceLocation location) {
        return modelsToReplace.get(location);
    }

}
