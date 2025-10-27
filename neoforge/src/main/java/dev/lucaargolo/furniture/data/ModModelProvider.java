package dev.lucaargolo.furniture.data;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.block.ModBlocks;
import dev.lucaargolo.furniture.block.WoodBlock;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModModelProvider extends BlockStateProvider {

    public ModModelProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, FurnitureMod.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        ModBlocks.BLOCKS.forEach((entry) -> {
            Block block = entry.get();
            if(block instanceof WoodBlock furniture) {
                ModelFile parentModel = this.models().getExistingFile(FurnitureMod.id("block/"+entry.path().replace(furniture.getWood().name()+"_", "")));
                this.models().getBuilder("block/"+entry.path())
                        .parent(parentModel)
                        .texture("log", DataHelper.getWoodLog(furniture.getWood()))
                        .texture("planks", DataHelper.getWoodPlanks(furniture.getWood()))
                        .texture("particle", DataHelper.getWoodLog(furniture.getWood()));
            }
            ModelFile model = this.models().getExistingFile(FurnitureMod.id("block/"+entry.path()));
            this.getVariantBuilder(block).forAllStates(state -> new ConfiguredModel[]{new ConfiguredModel(model)});
            this.models().getBuilder("item/" + entry.path()).parent(model);
        });
    }

}
