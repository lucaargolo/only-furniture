package dev.lucaargolo.furniture.client.model;

import com.mojang.math.Axis;
import dev.lucaargolo.furniture.utils.FurnitureData;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector4f;

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

    public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context, FurnitureData data) {
        Matrix4f transform = this.getDataTransform(pos, data);

        context.pushTransform((quad) -> {
            for (int i = 0; i < 4; i++) {
                Vector4f vector = new Vector4f(quad.x(i), quad.y(i), quad.z(i), 1.0f);
                vector.mul(transform);
                quad.pos(i, vector.x, vector.y, vector.z);
            }
            return true;
        });

        this.wrapped.emitBlockQuads(blockView, state, pos, randomSupplier, context);

        context.popTransform();
    }

    protected Matrix4f getDataTransform(BlockPos pos, FurnitureData data) {
        float offset = ((((pos.getX() & 1) << 2) | ((pos.getY() & 1) << 1) | (pos.getZ() & 1)) - 3.5f) * 0.001f;
        Quaternionf rotation = Axis.YN.rotationDegrees(data.getRotation());
        return new Matrix4f()
                .translate(data.getX(), 0f, data.getZ())
                .translate(0.5f, 0.5f, 0.5f)
                .rotate(rotation)
                .scale(1f + offset, 1f + offset, 1f + offset)
                .translate(-0.5f, -0.5f, -0.5f);
    }

}
