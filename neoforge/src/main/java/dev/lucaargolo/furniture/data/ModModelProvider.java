package dev.lucaargolo.furniture.data;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.block.ModBlocks;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.VariantBlockStateBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModModelProvider extends BlockStateProvider {

    public ModModelProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, FurnitureMod.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        ModBlocks.BLOCKS.forEach((path, block) -> {
            ModelFile parentModel = this.models().getExistingFile(FurnitureMod.id("block/"+path));
            ConfiguredModel model = new ConfiguredModel(parentModel);
            VariantBlockStateBuilder builder = this.getVariantBuilder(block.get());
            builder.forAllStates(state -> new ConfiguredModel[] { model });
            this.models().getBuilder("item/"+path).parent(parentModel);
        });
    }

}
