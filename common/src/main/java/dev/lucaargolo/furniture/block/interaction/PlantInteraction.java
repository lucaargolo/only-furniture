package dev.lucaargolo.furniture.block.interaction;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.attachment.ModDataAttachments;
import dev.lucaargolo.furniture.attachment.impl.PlantHolderDataAttachment;
import dev.lucaargolo.furniture.block.entity.ModBlockEntities;
import dev.lucaargolo.furniture.block.entity.PlantHolderBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class PlantInteraction extends Interaction<PlantInteraction> {

    public PlantInteraction(Vec3 pos) {
        super(pos);
    }

    @Override
    public PlantInteraction positioned(Vec3 pos) {
        return new PlantInteraction(pos);
    }

    @Override
    public boolean interact(int index, Level level, Player player, BlockHitResult hitResult) {
        BlockPos pos = hitResult.getBlockPos();
        Optional<PlantHolderBlockEntity> optional = level.getBlockEntity(pos, ModBlockEntities.PLANT_HOLDER.get());
        if(optional.isEmpty()) {
            return false;
        }

        PlantHolderBlockEntity blockEntity = optional.get();
        PlantHolderDataAttachment plantData = ModDataAttachments.PLANT_HOLDER_DATA.getOrCreate(blockEntity);
        if(plantData.getBlock(index) instanceof FlowerPotBlock potBlock && potBlock != Blocks.FLOWER_POT) {
            Block pottedBlock = potBlock.getPotted();
            ItemStack pottedStack = pottedBlock.asItem().getDefaultInstance();
            player.getInventory().placeItemBackInInventory(pottedStack);
            ModDataAttachments.PLANT_HOLDER_DATA.set(blockEntity, plantData.set(index, Blocks.FLOWER_POT));
            return true;
        }

        ItemStack stack = player.getMainHandItem();
        if(stack.isEmpty() || !(stack.getItem() instanceof BlockItem blockItem)) {
            return false;
        }

        Block pottedBlock = FurnitureMod.INSTANCE.getPottedBlock(blockItem.getBlock());
        if(pottedBlock == Blocks.FLOWER_POT) {
            return false;
        }

        if(!player.isCreative())
            stack.shrink(1);
        ModDataAttachments.PLANT_HOLDER_DATA.set(blockEntity, plantData.set(index, pottedBlock));
        return true;
    }

}
