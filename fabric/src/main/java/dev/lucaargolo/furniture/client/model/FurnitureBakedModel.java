package dev.lucaargolo.furniture.client.model;

import com.mojang.math.Axis;
import dev.lucaargolo.furniture.data.FurnitureData;
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
    public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
        FurnitureData data = FurnitureData.get(blockView, pos, 0);
        if(data.getDirectionToOriginal() == null) {
            Quaternionf rotation = Axis.YP.rotationDegrees(data.getRotation());
            Matrix4f transform = new Matrix4f()
                    .translate(data.getX(), 0f, data.getZ())
                    .translate(0.5f, 0.5f, 0.5f)
                    .rotate(rotation)
                    .translate(-0.5f, -0.5f, -0.5f);

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
    }

}
