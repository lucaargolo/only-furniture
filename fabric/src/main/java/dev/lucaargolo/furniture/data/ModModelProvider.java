package dev.lucaargolo.furniture.data;

import dev.lucaargolo.furniture.block.ModBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;
import org.jetbrains.annotations.NotNull;

public class ModModelProvider extends FabricModelProvider {

    public ModModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators blockModelGenerators) {
        ModBlocks.BLOCKS.forEach((path, block) -> {
            blockModelGenerators.createNonTemplateModelBlock(block.get());
        });
    }

    @Override
    public void generateItemModels(ItemModelGenerators itemModelGenerators) {

    }

    @Override
    public @NotNull String getName() {
        return "FurnitureModModelProvider";
    }

}
