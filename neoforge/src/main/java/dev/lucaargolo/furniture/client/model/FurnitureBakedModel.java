package dev.lucaargolo.furniture.client.model;

import com.mojang.math.Axis;
import com.mojang.math.Transformation;
import dev.lucaargolo.furniture.utils.FurnitureData;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.IQuadTransformer;
import net.neoforged.neoforge.client.model.QuadTransformers;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.List;

public class FurnitureBakedModel extends BakedModelWrapper<BakedModel> {

    private static final ModelProperty<BlockPos> POS_PROPERTY = new ModelProperty<>();
    private static final ModelProperty<FurnitureData> FURNITURE_DATA_PROPERTY = new ModelProperty<>();
    private static final ModelProperty<Boolean> HAS_DATA_PROPERTY = new ModelProperty<>();

    public FurnitureBakedModel(BakedModel originalModel) {
        super(originalModel);
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData modelData, @Nullable RenderType renderType) {
        BlockPos pos = modelData.get(POS_PROPERTY);
        if(pos == null) {
            pos = BlockPos.ZERO;
        }
        FurnitureData data = modelData.get(FURNITURE_DATA_PROPERTY);
        Boolean hasData = modelData.get(HAS_DATA_PROPERTY);
        if(data != null) {
            float offset = ((((pos.getX() & 1) << 2) | ((pos.getY() & 1) << 1) | (pos.getZ() & 1)) - 3.5f) * 0.001f;
            Quaternionf rotation = Axis.YN.rotationDegrees(data.getRotation());
            Matrix4f transform = new Matrix4f()
                    .translate(data.getX(), 0f, data.getZ())
                    .translate(0.5f, 0.5f, 0.5f)
                    .rotate(rotation)
                    .scale(1f + offset, 1f + offset, 1f + offset)
                    .translate(-0.5f, -0.5f, -0.5f);
            Transformation transformation = new Transformation(transform);
            IQuadTransformer transformer = QuadTransformers.applying(transformation);
            return transformer.process(super.getQuads(state, side, rand, modelData, renderType));
        }else if(hasData != Boolean.TRUE) {
            return super.getQuads(state, side, rand, modelData, renderType);
        }else{
            return List.of();
        }
    }

    @Override
    public @NotNull ModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ModelData modelData) {
        FurnitureData[] layers = FurnitureData.get(level, pos);
        FurnitureData data = null;
        boolean hasData = false;
        for (FurnitureData furnitureData : layers) {
            data = furnitureData;
            if (data.hasOriginal()) {
                hasData = true;
                break;
            }else{
                hasData = hasData || furnitureData.getDirectionToOriginal() != null;
                data = null;
            }
        }
        if(data != null) {
            return modelData.derive().with(POS_PROPERTY, pos).with(FURNITURE_DATA_PROPERTY, data).with(HAS_DATA_PROPERTY, true).build();
        }else{
            return modelData.derive().with(POS_PROPERTY, pos).with(HAS_DATA_PROPERTY, hasData).build();
        }
    }

}
