package dev.lucaargolo.furniture.client.model;

import dev.lucaargolo.furniture.FurnitureData;
import dev.lucaargolo.furniture.block.FurnitureBlock;
import dev.lucaargolo.furniture.block.behaviour.Behaviour;
import dev.lucaargolo.furniture.block.behaviour.PlantBehaviour;
import dev.lucaargolo.furniture.block.entity.FurnitureBlockEntity;
import dev.lucaargolo.furniture.block.entity.ModBlockEntityTypes;
import dev.lucaargolo.furniture.client.model.behaviour.PlantBehaviourBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.Optional;
import java.util.function.Supplier;

public class FurnitureBakedModel extends ForwardingBakedModel {

    public FurnitureBakedModel(BakedModel wrapped) {
        super(wrapped);
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public final void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
        Block block = state.getBlock();
        if(block instanceof FurnitureBlock furniture && !furniture.shouldRenderBlockEntity(blockView, pos, state)) {
            FurnitureData[] layers = FurnitureData.get(blockView, pos);
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
                this.emitBlockQuads(blockView, state, pos, randomSupplier, context, data);
            }else if(!hasData){
                super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
            }
        }
    }

    public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context, FurnitureData data) {
        float offset = ((((pos.getX() & 1) << 2) | ((pos.getY() & 1) << 1) | (pos.getZ() & 1)) - 3.5f) * 0.001f;
        Matrix4f transform =  new Matrix4f()
                .translate(data.getX(state), data.getY(state), data.getZ(state))
                .translate(0.5f, 0.5f, 0.5f)
                .rotate(data.getRotation(state))
                .scale(1f + offset, 1f + offset, 1f + offset)
                .translate(-0.5f, -0.5f, -0.5f);

        context.pushTransform((quad) -> {
            for (int i = 0; i < 4; i++) {
                Vector4f vector = new Vector4f(quad.x(i), quad.y(i), quad.z(i), 1.0f);
                vector.mul(transform);
                quad.pos(i, vector.x, vector.y, vector.z);
            }
            return true;
        });

        Optional<FurnitureBlockEntity> optional = blockView.getBlockEntity(pos, ModBlockEntityTypes.FURNITURE.get());
        if(state.getBlock() instanceof FurnitureBlock furniture) {
            Behaviour<?>[] behaviours = furniture.getBehaviours();
            for(int index = 0; index < behaviours.length; index++) {
                Behaviour<?> behaviour = behaviours[index];

                context.pushTransform((quad) -> {
                    for (int i = 0; i < 4; i++) {
                        quad.pos(i, (float) (quad.x(i) + behaviour.pos().x()), (float) (quad.y(i) + behaviour.pos().y()), (float) (quad.z(i) + behaviour.pos().z()));
                    }
                    return true;
                });

                if(behaviour instanceof PlantBehaviour && optional.isPresent()) {
                    PlantBehaviourBakedModel.emitBehaviourQuads(optional.get(), index, blockView, pos, randomSupplier, context);
                }

                context.popTransform();
            }
        }

        this.wrapped.emitBlockQuads(blockView, state, pos, randomSupplier, context);

        context.popTransform();
    }

}
