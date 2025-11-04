package dev.lucaargolo.furniture.data;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.data.fabric.FabricLikeDataOutput;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Set;

@EventBusSubscriber(modid = FurnitureMod.MOD_ID)
public class FurnitureModData {

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper exFileHelper = event.getExistingFileHelper();
        DatapackBuiltinEntriesProvider builtinProvider = new DatapackBuiltinEntriesProvider(output, event.getLookupProvider(), bootstrapRegistries(), Set.of(FurnitureMod.MOD_ID));
        generator.addProvider(true, builtinProvider);
        generator.addProvider(event.includeClient(), new NeoForgeModLanguageProvider(output));
        generator.addProvider(event.includeClient(), new NeoForgeModModelProvider(new FabricLikeDataOutput(output.getOutputFolder(), event.validate())));
        generator.addProvider(event.includeServer(), NeoForgeModTagProvider.item(output, event.getLookupProvider(), exFileHelper));
        generator.addProvider(event.includeServer(), NeoForgeModTagProvider.block(output, event.getLookupProvider(), exFileHelper));
        generator.addProvider(event.includeServer(), new ModLootProvider(output, event.getLookupProvider()));
        generator.addProvider(event.includeServer(), new ModDataMapProvider(output, event.getLookupProvider()));
    }

    public static RegistrySetBuilder bootstrapRegistries() {
        return new RegistrySetBuilder();
    }

}
