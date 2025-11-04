package dev.lucaargolo.furniture.data;

import dev.lucaargolo.furniture.data.fabric.FabricLikeDataOutput;
import dev.lucaargolo.furniture.data.fabric.FabricLikeModelProvider;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;

public class NeoForgeModModelProvider extends FabricLikeModelProvider {

    public NeoForgeModModelProvider(FabricLikeDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators generators) {
        ModBlockModelProvider.generate(generators);
    }

    @Override
    public void generateItemModels(ItemModelGenerators generators) {

    }

}
