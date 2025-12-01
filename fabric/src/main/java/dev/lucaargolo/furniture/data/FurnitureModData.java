package dev.lucaargolo.furniture.data;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.data.DataProvider;

public class FurnitureModData implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator dataGenerator) {
        FabricDataGenerator.Pack pack = dataGenerator.createPack();
        pack.addProvider(FabricModLanguageProvider::new);
        pack.addProvider(FabricModModelProvider::new);
        pack.addProvider(FabricModTagProvider::item);
        pack.addProvider(FabricModTagProvider::block);
        pack.addProvider(FabricModBlockLootProvider::new);
        pack.addProvider((DataProvider.Factory<ModSoundsProvider>) ModSoundsProvider::new);
    }

}
