package dev.lucaargolo.furniture.data;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.block.ModBlocks;
import dev.lucaargolo.furniture.block.WoodBlock;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ModLanguageProvider extends LanguageProvider {

    public ModLanguageProvider(PackOutput output) {
        super(output, FurnitureMod.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        ModBlocks.BLOCKS.forEach((entry) -> {
            Block block = entry.get();
            if(block instanceof WoodBlock furniture) {
                add(block, DataHelper.woodBlockTranslation(furniture.getWood(), entry.path()));
            }else{
                add(block, DataHelper.defaultTranslation(entry.path()));
            }
        });
    }

}
