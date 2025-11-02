package dev.lucaargolo.furniture.data;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.block.ModBlocks;
import dev.lucaargolo.furniture.block.WoodBlock;
import net.minecraft.world.level.block.Block;

public interface ModLanguageProvider {

    static void generate(ModLanguageBuilder builder) {
        builder.add("itemGroup."+FurnitureMod.MOD_ID+".creative_tab", FurnitureMod.MOD_NAME);
        ModBlocks.BLOCKS.forEach((entry) -> {
            Block block = entry.get();
            if(block instanceof WoodBlock furniture) {
                builder.add(block, DataHelper.woodBlockTranslation(furniture.getWood(), entry.path()));
            }else{
                builder.add(block, DataHelper.defaultTranslation(entry.path()));
            }
        });
    }

}
