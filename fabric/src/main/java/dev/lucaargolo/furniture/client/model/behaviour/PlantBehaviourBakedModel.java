package dev.lucaargolo.furniture.client.model.behaviour;

import dev.lucaargolo.furniture.attachment.ModDataAttachments;
import dev.lucaargolo.furniture.attachment.impl.PlantDataAttachment;
import dev.lucaargolo.furniture.block.entity.FurnitureBlockEntity;
import net.fabricmc.fabric.api.renderer.v1.model.SpriteFinder;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

public class PlantBehaviourBakedModel {

    public static void emitBehaviourQuads(FurnitureBlockEntity blockEntity, int index, BlockAndTintGetter blockView, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
        PlantDataAttachment plantData = ModDataAttachments.PLANT_DATA.getOrCreate(blockEntity);
        Block plantBlock = plantData.getBlock(index);
        if(plantBlock != Blocks.FLOWER_POT) {
            BlockState plantState = plantBlock.defaultBlockState();
            BakedModel plantModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(plantState);
            TextureAtlas atlas = Minecraft.getInstance().getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS);
            SpriteFinder finder = SpriteFinder.get(atlas);

            context.pushTransform(quad -> {
                TextureAtlasSprite sprite = finder.find(quad);
                SpriteContents contents = sprite.contents();
                return !contents.name().toString().contains("flower_pot");
            });

            plantModel.emitBlockQuads(blockView, plantState, pos, randomSupplier, context);

            context.popTransform();
        }
    }

}
