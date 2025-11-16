package dev.lucaargolo.furniture.client.model;

import com.mojang.math.Transformation;
import dev.lucaargolo.furniture.FurnitureData;
import dev.lucaargolo.furniture.attachment.ModDataAttachments;
import dev.lucaargolo.furniture.attachment.impl.PlantHolderDataAttachment;
import dev.lucaargolo.furniture.block.entity.ModBlockEntities;
import dev.lucaargolo.furniture.block.entity.PlantHolderBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.IQuadTransformer;
import net.neoforged.neoforge.client.model.QuadTransformers;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class PlantHolderBakedModel extends FurnitureBakedModel {

    private static final ModelProperty<PlantHolderDataAttachment> PLANT_HOLDER_DATA = new ModelProperty<>();

    public PlantHolderBakedModel(BakedModel originalModel) {
        super(originalModel);
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData modelData, @Nullable RenderType renderType) {
        List<BakedQuad> quads = new ArrayList<>(super.getQuads(state, side, rand, modelData, renderType));
        BlockPos pos = modelData.get(POS_PROPERTY);
        FurnitureData data = modelData.get(DATA_PROPERTY);
        PlantHolderDataAttachment plantData = modelData.get(PLANT_HOLDER_DATA);
        if(state != null && pos != null && data != null && plantData != null && renderType != null) {
            plantData.forEach((plantPos, plantBlock) -> {
                BlockState plantState = plantBlock.defaultBlockState();
                BakedModel plantModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(plantState);
                ChunkRenderTypeSet plantRenderTypes = plantModel.getRenderTypes(plantState, rand, modelData);
                if(plantRenderTypes.contains(renderType)) {
                    List<BakedQuad> plantQuads = plantModel.getQuads(plantState, side, rand, modelData, renderType);
                    float offset = ((((pos.getX() & 1) << 2) | ((pos.getY() & 1) << 1) | (pos.getZ() & 1)) - 3.5f) * 0.001f;
                    Matrix4f transform = new Matrix4f()
                            .translate((float) plantPos.x, (float) plantPos.y, (float) plantPos.z)
                            .translate(data.getX(state), data.getY(state), data.getZ(state))
                            .translate(0.5f, 0.5f, 0.5f)
                            .rotate(data.getRotation(state))
                            .scale(1f + offset, 1f + offset, 1f + offset)
                            .translate(-0.5f, -0.5f, -0.5f);
                    Transformation transformation = new Transformation(transform);
                    IQuadTransformer transformer = QuadTransformers.applying(transformation);
                    plantQuads.forEach(plantQuad -> {
                        SpriteContents contents = plantQuad.getSprite().contents();
                        if(!contents.name().toString().contains("flower_pot")) {
                            quads.add(transformer.process(plantQuad));
                        }
                    });
                }
            });
        }
        return quads;
    }

    @Override
    public @NotNull ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand, @NotNull ModelData modelData) {
        AtomicReference<ChunkRenderTypeSet> renderTypes = new AtomicReference<>(super.getRenderTypes(state, rand, modelData));
        PlantHolderDataAttachment plantData = modelData.get(PLANT_HOLDER_DATA);
        if(plantData != null) {
            plantData.forEach((plantPos, plantBlock) -> {
                BlockState plantState = plantBlock.defaultBlockState();
                BakedModel plantModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(plantState);
                ChunkRenderTypeSet plantRenderTypes = plantModel.getRenderTypes(plantState, rand, modelData);
                renderTypes.set(ChunkRenderTypeSet.union(renderTypes.get(), plantRenderTypes));
            });
        }
        return renderTypes.get();
    }

    @Override
    public @NotNull ModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ModelData modelData) {
        modelData = super.getModelData(level, pos, state, modelData);
        Optional<PlantHolderBlockEntity> optional = level.getBlockEntity(pos, ModBlockEntities.PLANT_HOLDER.get());
        if (optional.isPresent()) {
            return modelData.derive().with(PLANT_HOLDER_DATA, ModDataAttachments.PLANT_HOLDER_DATA.getOrCreate(optional.get())).build();
        }
        return modelData;
    }
}
