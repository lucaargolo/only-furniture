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
        ModBlocks.BLOCKS.forEach((path, supplier) -> {
            Block block = supplier.get();
            if(block instanceof WoodBlock) {
                add(block, DataHelper.woodBlockTranslation(path));
            }else{
                add(block, DataHelper.defaultTranslation(path));
            }
        });
    }

}
