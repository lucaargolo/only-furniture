package dev.lucaargolo.furniture.client;

import net.fabricmc.api.ClientModInitializer;

public class FabricFurnitureModClient extends FurnitureModClient implements ClientModInitializer {

    public static FabricFurnitureModClient INSTANCE;

    @Override
    public void init() {
        INSTANCE = this;
        super.init();
    }

    @Override
    public void onInitializeClient() {
        this.init();
    }

}
