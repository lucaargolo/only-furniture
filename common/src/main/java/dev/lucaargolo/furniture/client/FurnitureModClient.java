package dev.lucaargolo.furniture.client;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.client.model.ModModelManager;

public abstract class FurnitureModClient {

    public static FurnitureModClient INSTANCE;

    private final ModModelManager modelManager = FurnitureMod.INSTANCE.loadPlatformClass(ModModelManager.class);

    public void init() {
        INSTANCE = this;
        modelManager.init();
    }

    public ModModelManager getModelManager() {
        return modelManager;
    }

}
