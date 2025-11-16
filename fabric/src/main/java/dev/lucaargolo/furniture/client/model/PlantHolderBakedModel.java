package dev.lucaargolo.furniture.client.model;

import dev.lucaargolo.furniture.FurnitureData;
import dev.lucaargolo.furniture.attachment.ModDataAttachments;
import dev.lucaargolo.furniture.attachment.impl.PlantHolderDataAttachment;
import dev.lucaargolo.furniture.block.FurnitureBlock;
import dev.lucaargolo.furniture.block.entity.ModBlockEntities;
import dev.lucaargolo.furniture.block.entity.PlantHolderBlockEntity;
import dev.lucaargolo.furniture.block.interaction.Interaction;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class PlantHolderBakedModel extends FurnitureBakedModel {

    public PlantHolderBakedModel(BakedModel wrapped) {
        super(wrapped);
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context, FurnitureData data) {
        super.emitBlockQuads(blockView, state, pos, randomSupplier, context, data);
        if(!RendererAccess.INSTANCE.hasRenderer()) {
            return;
        }
        Renderer renderer = RendererAccess.INSTANCE.getRenderer();
        assert renderer != null;
        Optional<PlantHolderBlockEntity> optional = blockView.getBlockEntity(pos, ModBlockEntities.PLANT_HOLDER.get());
        if (optional.isPresent() && state.getBlock() instanceof FurnitureBlock furniture) {
            PlantHolderDataAttachment plantData = ModDataAttachments.PLANT_HOLDER_DATA.getOrCreate(optional.get());
            for(int index = 0; index < plantData.size(); index++) {
                List<? extends Interaction<?>> interactions = furniture.getInteractions();
                Vec3 plantPos = index < interactions.size() ? interactions.get(index).pos() : Vec3.ZERO;
                Block plantBlock = plantData.getBlock(index);
                if(plantPos == null || plantBlock == Blocks.FLOWER_POT)
                    continue;

                BlockState plantState = plantBlock.defaultBlockState();
                BakedModel plantModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(plantState);

                float offset = ((((pos.getX() & 1) << 2) | ((pos.getY() & 1) << 1) | (pos.getZ() & 1)) - 3.5f) * 0.001f;
                Matrix4f transform = new Matrix4f()
                        .translate((float) plantPos.x, (float) plantPos.y, (float) plantPos.z)
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

                //TODO: Change this to use FAPI methods whenever I learn how to use them.
                List<BakedQuad> plantQuads = new ArrayList<>();
                for (Direction direction : Direction.values()) {
                    plantQuads.addAll(plantModel.getQuads(plantState, direction, randomSupplier.get()));

                }
                plantQuads.addAll(plantModel.getQuads(plantState, null, randomSupplier.get()));

                QuadEmitter emitter = context.getEmitter();
                plantQuads.forEach(plantQuad -> {
                    SpriteContents contents = plantQuad.getSprite().contents();
                    if(!contents.name().toString().contains("flower_pot")) {
                        emitter.fromVanilla(plantQuad.getVertices(), 0);
                        emitter.emit();
                    }
                });

                context.popTransform();
            }
        }
    }

}
