package dev.lucaargolo.furniture.client.model;

import com.mojang.math.Axis;
import com.mojang.math.Transformation;
import dev.lucaargolo.furniture.data.FurnitureData;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.IQuadTransformer;
import net.neoforged.neoforge.client.model.QuadTransformers;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.List;

public class NeoForgeFurnitureBakedModel extends FurnitureBakedModel {

    private static final ModelProperty<FurnitureData> FURNITURE_DATA_PROPERTY = new ModelProperty<>();

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData modelData, @Nullable RenderType renderType) {
        FurnitureData data = modelData.get(FURNITURE_DATA_PROPERTY);
        if(data != null) {
            Quaternionf rotation = Axis.YP.rotationDegrees(data.getRotation());
            Matrix4f transform = new Matrix4f()
                    .translate(data.getX(), 0f, data.getZ())
                    .translate(0.5f, 0.5f, 0.5f)
                    .rotate(rotation)
                    .translate(-0.5f, -0.5f, -0.5f);
            Transformation transformation = new Transformation(transform);
            IQuadTransformer transformer = QuadTransformers.applying(transformation);
            return transformer.process(super.getQuads(state, side, rand, modelData, renderType));
        }else{
            return super.getQuads(state, side, rand, modelData, renderType);
        }
    }

    @Override
    public @NotNull ModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ModelData modelData) {
        return modelData.derive().with(FURNITURE_DATA_PROPERTY, FurnitureData.get(level, pos)).build();
    }

}
