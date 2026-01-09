package dev.lucaargolo.furniture.client.utils;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.concurrent.LinkedBlockingDeque;

public class FurnitureRenderContext implements RenderContext {

    public static final FurnitureRenderContext INSTANCE = new FurnitureRenderContext();

    private final LinkedBlockingDeque<QuadTransform> stack = new LinkedBlockingDeque<>();
    private final FurnitureQuadEmitter emitter = new FurnitureQuadEmitter(this);

    private PoseStack poseStack = null;
    private VertexConsumer consumer = null;
    private boolean lightPipelineAware = false;

    private FurnitureRenderContext() {

    }

    public void prepare(Level level, BlockState state, BlockPos pos, BakedModel model, PoseStack poseStack, VertexConsumer consumer, boolean lightPipelineAware) {
        this.poseStack = poseStack;
        this.consumer = consumer;
        this.lightPipelineAware = lightPipelineAware;
        if(this.lightPipelineAware) {
            FurnitureAoCalculator.INSTANCE.prepare(level, state, pos, model);
        }
    }

    public void release() {
        if(this.lightPipelineAware) {
            FurnitureAoCalculator.INSTANCE.release();
        }
    }

    public LinkedBlockingDeque<QuadTransform> stack() {
        return this.stack;
    }

    public PoseStack poseStack() {
        return this.poseStack;
    }

    public VertexConsumer consumer() {
        return this.consumer;
    }

    public boolean lightPipelineAware() {
        return lightPipelineAware;
    }

    @Override
    public QuadEmitter getEmitter() {
        return emitter;
    }

    @Override
    public void pushTransform(QuadTransform transform) {
        stack.push(transform);
    }

    @Override
    public void popTransform() {
        stack.pop();
    }

    @Override
    @SuppressWarnings("removal")
    public BakedModelConsumer bakedModelConsumer() {
        throw new IllegalStateException("Baked model consumer is not supported");
    }

}
