package dev.lucaargolo.furniture;

import net.fabricmc.api.ModInitializer;

public class FabricFurnitureMod extends FurnitureMod implements ModInitializer {

    @Override
    public void onInitialize() {
        this.init();
    }

    @Override
    public String getPlatform() {
        return "Fabric";
    }

}
