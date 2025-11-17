package dev.lucaargolo.furniture.client.model.behaviour;

import dev.lucaargolo.furniture.attachment.ModDataAttachments;
import dev.lucaargolo.furniture.attachment.impl.PlantDataAttachment;
import dev.lucaargolo.furniture.block.entity.FurnitureBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PlantBehaviourBakedModel {

    public static List<BakedQuad> getBehaviourQuads(FurnitureBlockEntity blockEntity, int index, @Nullable Direction side, RandomSource rand, ModelData modelData, @Nullable RenderType renderType) {
        List<BakedQuad> quads = new ArrayList<>();
        PlantDataAttachment plantData = ModDataAttachments.PLANT_DATA.getOrCreate(blockEntity);
        Block plantBlock = plantData.getBlock(index);
        if(plantBlock != Blocks.FLOWER_POT) {
            BlockState plantState = plantBlock.defaultBlockState();
            BakedModel plantModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(plantState);
            ChunkRenderTypeSet plantRenderTypes = plantModel.getRenderTypes(plantState, rand, modelData);
            if (renderType == null || plantRenderTypes.contains(renderType)) {
                List<BakedQuad> plantQuads = plantModel.getQuads(plantState, side, rand, modelData, renderType);

                plantQuads.forEach(plantQuad -> {
                    SpriteContents contents = plantQuad.getSprite().contents();
                    if (!contents.name().toString().contains("flower_pot")) {
                        quads.add(plantQuad);
                    }
                });
            }
        }
        return quads;
    }

    public static ChunkRenderTypeSet getRenderTypes(FurnitureBlockEntity blockEntity, int index, RandomSource rand, ModelData modelData) {
        PlantDataAttachment plantData = ModDataAttachments.PLANT_DATA.getOrCreate(blockEntity);
        Block plantBlock = plantData.getBlock(index);
        if(plantBlock != Blocks.FLOWER_POT) {
            BlockState plantState = plantBlock.defaultBlockState();
            BakedModel plantModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(plantState);
            return plantModel.getRenderTypes(plantState, rand, modelData);
        }
        return ChunkRenderTypeSet.none();
    }

}
