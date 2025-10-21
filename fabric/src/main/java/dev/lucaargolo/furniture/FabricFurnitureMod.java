package dev.lucaargolo.furniture;

import net.fabricmc.api.ModInitializer;

public class FabricFurnitureMod extends FurnitureMod implements ModInitializer {

    public static FabricFurnitureMod INSTANCE;

    @Override
    public void onInitialize() {
        INSTANCE = this;
        LOG.info("Hello from Fabric!");
        this.init();
    }

    @Override
    public String getPlatform() {
        return "Fabric";
    }

}
