package dev.lucaargolo.furniture.data;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.block.ModBlocks;
import dev.lucaargolo.furniture.block.WoodBlock;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.block.Block;

import java.util.concurrent.CompletableFuture;

public class ModLanguageProvider extends FabricLanguageProvider {

    protected ModLanguageProvider(FabricDataOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generateTranslations(HolderLookup.Provider provider, TranslationBuilder translationBuilder) {
        translationBuilder.add("itemGroup."+FurnitureMod.MOD_ID+".creative_tab", FurnitureMod.MOD_NAME);
        ModBlocks.BLOCKS.forEach((entry) -> {
            Block block = entry.get();
            if(block instanceof WoodBlock furniture) {
                translationBuilder.add(block, DataHelper.woodBlockTranslation(furniture.getWood(), entry.path()));
            }else{
                translationBuilder.add(block, DataHelper.defaultTranslation(entry.path()));
            }
        });
    }

}
