package dev.lucaargolo.furniture.client.model;

import dev.lucaargolo.furniture.FurnitureData;
import dev.lucaargolo.furniture.attachment.impl.AnimationDataAttachment;
import dev.lucaargolo.furniture.block.FurnitureBlock;
import dev.lucaargolo.furniture.block.behaviour.Behaviour;
import dev.lucaargolo.furniture.block.behaviour.PlantBehaviour;
import dev.lucaargolo.furniture.block.entity.FurnitureBlockEntity;
import dev.lucaargolo.furniture.block.entity.ModBlockEntityTypes;
import dev.lucaargolo.furniture.client.FabricFurnitureModClient;
import dev.lucaargolo.furniture.client.model.behaviour.PlantBehaviourBakedModel;
import dev.lucaargolo.furniture.client.utils.FurnitureQuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.impl.renderer.VanillaModelEncoder;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
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
        if(block instanceof FurnitureBlock furniture && furniture.shouldRenderModel(blockView, pos)) {
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
                this.emitBlockQuads(blockView, state, pos, randomSupplier, context, data, null, 1f);
            }else if(!hasData){
                super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
            }
        }
    }

    public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context, FurnitureData data, @Nullable AnimationDataAttachment animations, float partialTick) {
        float offset = ((((pos.getX() & 1) << 2) | ((pos.getY() & 1) << 1) | (pos.getZ() & 1)) - 3.5f) * 0.001f;
        Matrix4f globalTransform =  new Matrix4f()
                .translate(data.getX(state), data.getY(state), data.getZ(state))
                .translate(0.5f, 0.5f, 0.5f)
                .rotate(data.getRotation(state))
                .scale(1f + offset, 1f + offset, 1f + offset)
                .translate(-0.5f, -0.5f, -0.5f);

        context.pushTransform((quad) -> {
            Matrix4f localTransform = new Matrix4f();
            if(animations != null && quad instanceof FurnitureQuadEmitter furnitureQuad) {
                localTransform = localTransform.translate(furnitureQuad.pivot());
                localTransform = animations.animate(furnitureQuad.groupName(), localTransform, partialTick);
                localTransform = localTransform.translate(new Vector3f(furnitureQuad.pivot()).mul(-1f));
            }
            for (int i = 0; i < 4; i++) {
                Vector4f position = new Vector4f(quad.x(i), quad.y(i), quad.z(i), 1.0f);
                position.mul(localTransform);
                position.mul(globalTransform);
                quad.pos(i, position.x, position.y, position.z);
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

        if(FabricFurnitureModClient.getSodiumCompat().isPresent()) {
            if(this.wrapped.isVanillaAdapter()) {
                //noinspection UnstableApiUsage
                VanillaModelEncoder.emitBlockQuads(this, state, randomSupplier, context);
            }
        }else{
            this.wrapped.emitBlockQuads(blockView, state, pos, randomSupplier, context);
        }

        context.popTransform();
    }

}
