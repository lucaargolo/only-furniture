package dev.lucaargolo.furniture.client.utils;

import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.impl.client.indigo.renderer.aocalc.AoCalculator;
import net.fabricmc.fabric.impl.client.indigo.renderer.aocalc.AoLuminanceFix;
import net.fabricmc.fabric.impl.client.indigo.renderer.helper.ColorHelper;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.EncodingFormat;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.MutableQuadViewImpl;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.BlockRenderInfo;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

@SuppressWarnings("UnstableApiUsage")
public class FurnitureAoCalculator extends AoCalculator {

    public static final FurnitureAoCalculator INSTANCE = new FurnitureAoCalculator();

    private FurnitureAoCalculator() {
        super(new BlockRenderInfo());
    }

    public void prepare(BlockAndTintGetter blockView, BlockState blockState, BlockPos blockPos, BakedModel model) {
        this.clear();
        this.blockInfo.prepareForWorld(blockView, false);
        this.blockInfo.prepareForBlock(blockState, blockPos, model.useAmbientOcclusion());
    }

    public void compute(MutableQuadView quad) {
        MutableQuadViewImpl indigoQuad = new MutableQuadViewImpl() {
            {
                data = new int[EncodingFormat.TOTAL_STRIDE];
                clear();
            }

            @Override
            public void emitDirectly() {}
        };
        FurnitureQuadEmitter.copy(quad, indigoQuad);
        super.compute(indigoQuad, true);
        for (int i = 0; i < 4; i++) {
            quad.color(i, ColorHelper.multiplyRGB(quad.color(i), this.ao[i]));
            quad.lightmap(i, ColorHelper.maxBrightness(quad.lightmap(i), this.light[i]));
        }
    }

    public void release() {
        this.blockInfo.release();
    }

    public int light(BlockPos pos, BlockState state) {
        return AoCalculator.getLightmapCoordinates(this.blockInfo.blockView, state, pos);
    }

    public float ao(BlockPos pos, BlockState state) {
        return AoLuminanceFix.INSTANCE.apply(this.blockInfo.blockView, pos, state);
    }

}
