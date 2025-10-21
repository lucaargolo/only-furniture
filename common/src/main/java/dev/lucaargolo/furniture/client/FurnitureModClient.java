package dev.lucaargolo.furniture.client;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.client.model.FurnitureModelManager;

public abstract class FurnitureModClient {

    public static FurnitureModClient INSTANCE;

    private final FurnitureModelManager modelManager = FurnitureMod.INSTANCE.loadPlatformClass(FurnitureModelManager.class);

    public void init() {
        INSTANCE = this;
        modelManager.init();
    }

    public FurnitureModelManager getModelManager() {
        return modelManager;
    }

}
