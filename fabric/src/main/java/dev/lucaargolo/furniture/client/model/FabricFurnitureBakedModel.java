package dev.lucaargolo.furniture.client.model;

import dev.lucaargolo.furniture.data.FurnitureData;
import dev.lucaargolo.furniture.data.LocalFurnitureData;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

public class FabricFurnitureBakedModel extends FurnitureBakedModel implements FabricBakedModel {

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
        FurnitureData data = LocalFurnitureData.get(Level.OVERWORLD, pos);

        context.pushTransform((quad) -> {
            quad.pos(0, quad.x(0) + data.getX(), quad.y(0), quad.z(0) + data.getZ());
            quad.pos(1, quad.x(1) + data.getX(), quad.y(1), quad.z(1) + data.getZ());
            quad.pos(2, quad.x(2) + data.getX(), quad.y(2), quad.z(2) + data.getZ());
            quad.pos(3, quad.x(3) + data.getX(), quad.y(3), quad.z(3) + data.getZ());
            return true;
        });

        BakedModel bakedModel = getBakedModel(state);
        if(bakedModel != null) {
            bakedModel.emitBlockQuads(blockView, state, pos, randomSupplier, context);
        }

        context.popTransform();
    }

}
