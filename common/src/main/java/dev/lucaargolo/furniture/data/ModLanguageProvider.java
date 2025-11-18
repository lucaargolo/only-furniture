package dev.lucaargolo.furniture.data;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.block.ModBlocks;
import dev.lucaargolo.furniture.block.base.WoodBlock;
import dev.lucaargolo.furniture.data.builder.ModLanguageBuilder;
import net.minecraft.world.level.block.Block;

public class ModLanguageProvider {

    public static void generate(ModLanguageBuilder builder) {
        builder.add("itemGroup."+FurnitureMod.MOD_ID+".creative_tab", FurnitureMod.MOD_NAME);
        ModBlocks.REGISTRY.getEntries().forEach((entry) -> {
            Block block = entry.get();
            if(block instanceof WoodBlock furniture) {
                builder.add(block, DataHelper.woodBlockTranslation(furniture.getWood(), entry.path()));
            }else{
                builder.add(block, DataHelper.defaultTranslation(entry.path()));
            }
        });
        builder.add("storage.onlyfurniture.kitchen_counter", "Kitchen Counter");
        builder.add("storage.onlyfurniture.fridge", "Fridge");
        builder.add("storage.onlyfurniture.freezer", "Freezer");
    }

}
