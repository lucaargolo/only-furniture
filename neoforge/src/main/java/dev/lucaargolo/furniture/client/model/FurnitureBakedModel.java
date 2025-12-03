package dev.lucaargolo.furniture.client.model;

import com.mojang.math.Transformation;
import dev.lucaargolo.furniture.FurnitureData;
import dev.lucaargolo.furniture.block.FurnitureBlock;
import dev.lucaargolo.furniture.block.behaviour.Behaviour;
import dev.lucaargolo.furniture.block.behaviour.PlantBehaviour;
import dev.lucaargolo.furniture.block.entity.FurnitureBlockEntity;
import dev.lucaargolo.furniture.block.entity.ModBlockEntityTypes;
import dev.lucaargolo.furniture.client.model.behaviour.PlantBehaviourBakedModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
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

public class FurnitureBakedModel extends BakedModelWrapper<BakedModel> {

    public static final ModelProperty<BlockPos> POS_PROPERTY = new ModelProperty<>();
    public static final ModelProperty<FurnitureData> DATA_PROPERTY = new ModelProperty<>();
    public static final ModelProperty<Boolean> HAS_DATA_PROPERTY = new ModelProperty<>();
    public static final ModelProperty<FurnitureBlockEntity> BLOCK_ENTITY_PROPERTY = new ModelProperty<>();
    public static final ModelProperty<Integer> COLOR = new ModelProperty<>();

    public FurnitureBakedModel(BakedModel originalModel) {
        super(originalModel);
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData modelData, @Nullable RenderType renderType) {
        BlockPos pos = modelData.get(POS_PROPERTY);
        FurnitureData data = modelData.get(DATA_PROPERTY);
        Boolean hasData = modelData.get(HAS_DATA_PROPERTY);
        if(state != null && pos != null && data != null) {
            float offset = ((((pos.getX() & 1) << 2) | ((pos.getY() & 1) << 1) | (pos.getZ() & 1)) - 3.5f) * 0.001f;
            Matrix4f transform = new Matrix4f()
                    .translate(data.getX(state), data.getY(state), data.getZ(state))
                    .translate(0.5f, 0.5f, 0.5f)
                    .rotate(data.getRotation(state))
                    .scale(1f + offset, 1f + offset, 1f + offset)
                    .translate(-0.5f, -0.5f, -0.5f);
            Transformation transformation = new Transformation(transform);
            IQuadTransformer transformer = QuadTransformers.applying(transformation);

            List<BakedQuad> quads = new ArrayList<>(super.getQuads(state, side, rand, modelData, renderType));

            Optional<FurnitureBlockEntity> optional = Optional.ofNullable(modelData.get(BLOCK_ENTITY_PROPERTY));
            if(state.getBlock() instanceof FurnitureBlock furniture) {
                Behaviour<?>[] behaviours = furniture.getBehaviours();
                for(int index = 0; index < behaviours.length; index++) {
                    Behaviour<?> behaviour = behaviours[index];

                    Transformation behaviourTransformation = new Transformation(behaviour.pos().toVector3f(), null, null, null);
                    IQuadTransformer behaviourTransformer = QuadTransformers.applying(behaviourTransformation);

                    List<BakedQuad> behaviourQuads = new ArrayList<>();

                    if(behaviour instanceof PlantBehaviour && optional.isPresent()) {
                        behaviourQuads.addAll(PlantBehaviourBakedModel.getBehaviourQuads(optional.get(), index, side, rand, modelData, renderType));
                    }

                    quads.addAll(behaviourTransformer.process(behaviourQuads));
                }
            }

            return transformer.process(quads);
        }else if(hasData != Boolean.TRUE) {
            return super.getQuads(state, side, rand, modelData, renderType);
        }else{
            return List.of();
        }
    }

    @Override
    public @NotNull ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand, @NotNull ModelData modelData) {
        ChunkRenderTypeSet renderTypes = super.getRenderTypes(state, rand, modelData);
        Optional<FurnitureBlockEntity> optional = Optional.ofNullable(modelData.get(BLOCK_ENTITY_PROPERTY));
        if(state.getBlock() instanceof FurnitureBlock furniture) {
            Behaviour<?>[] behaviours = furniture.getBehaviours();
            for (int index = 0; index < behaviours.length; index++) {
                Behaviour<?> behaviour = behaviours[index];

                if(behaviour instanceof PlantBehaviour && optional.isPresent()) {
                    renderTypes = ChunkRenderTypeSet.union(renderTypes, PlantBehaviourBakedModel.getRenderTypes(optional.get(), index, rand, modelData));
                }
            }
        }
        return renderTypes;
    }

    @Override
    public @NotNull ModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ModelData modelData) {
        modelData = super.getModelData(level, pos, state, modelData);
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
        ModelData.Builder builder = modelData.derive();
        builder.with(POS_PROPERTY, pos);
        level.getBlockEntity(pos, ModBlockEntityTypes.FURNITURE.get()).ifPresent(blockEntity -> {
            builder.with(BLOCK_ENTITY_PROPERTY, blockEntity);
        });
        Block block = state.getBlock();
        if(block instanceof FurnitureBlock furniture && !furniture.shouldRenderBlockEntity(level, pos, state) && data != null && !modelData.has(DATA_PROPERTY)) {
            return builder.with(DATA_PROPERTY, data).with(HAS_DATA_PROPERTY, true).build();
        }else if(hasData && !modelData.has(HAS_DATA_PROPERTY)) {
            return builder.with(HAS_DATA_PROPERTY, true).build();
        }else{
            return builder.build();
        }
    }

}
