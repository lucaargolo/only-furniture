package dev.lucaargolo.furniture.client.model.behaviour;

import dev.lucaargolo.furniture.attachment.ModDataAttachments;
import dev.lucaargolo.furniture.attachment.impl.PlantHolderDataAttachment;
import dev.lucaargolo.furniture.block.entity.FurnitureBlockEntity;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PlantBehaviourBakedModel {

    public static void emitBehaviourQuads(FurnitureBlockEntity blockEntity, int index, Supplier<RandomSource> randomSupplier, RenderContext context) {
        PlantHolderDataAttachment plantData = ModDataAttachments.PLANT_HOLDER_DATA.getOrCreate(blockEntity);
        Block plantBlock = plantData.getBlock(index);
        if(plantBlock != Blocks.FLOWER_POT) {
            BlockState plantState = plantBlock.defaultBlockState();
            BakedModel plantModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(plantState);

            //TODO: Change this to use FAPI methods whenever I learn how to use them.
            List<BakedQuad> plantQuads = new ArrayList<>();
            for (Direction direction : Direction.values()) {
                plantQuads.addAll(plantModel.getQuads(plantState, direction, randomSupplier.get()));

            }
            plantQuads.addAll(plantModel.getQuads(plantState, null, randomSupplier.get()));

            QuadEmitter emitter = context.getEmitter();
            plantQuads.forEach(plantQuad -> {
                SpriteContents contents = plantQuad.getSprite().contents();
                if (!contents.name().toString().contains("flower_pot")) {
                    emitter.fromVanilla(plantQuad.getVertices(), 0);
                    emitter.emit();
                }
            });
        }
    }

}
