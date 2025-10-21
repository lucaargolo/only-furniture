package dev.lucaargolo.furniture.client.model;

import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

public class FabricFurnitureBakedModel extends FurnitureBakedModel implements FabricBakedModel {

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
        BakedModel bakedModel = getBakedModel(state);
        if(bakedModel != null) {
            bakedModel.emitBlockQuads(blockView, state, pos, randomSupplier, context);
        }
    }

}
