package dev.lucaargolo.furniture.block.behaviour;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.attachment.ModDataAttachments;
import dev.lucaargolo.furniture.attachment.impl.PlantHolderDataAttachment;
import dev.lucaargolo.furniture.block.entity.FurnitureBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class PlantBehaviour extends Behaviour<PlantBehaviour> {

    public PlantBehaviour(Vec3 pos) {
        super(pos);
    }

    public PlantBehaviour(double x, double y, double z) {
        this(new Vec3(x, y, z));
    }

    @Override
    public PlantBehaviour positioned(Vec3 pos) {
        return new PlantBehaviour(pos);
    }

    @Override
    public boolean interact(Level level, BlockPos pos, BlockState state, @Nullable FurnitureBlockEntity blockEntity, Player player, int index) {
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

        Block pottedBlock = FurnitureMod.getInstance().getPottedBlock(blockItem.getBlock());
        if(pottedBlock == Blocks.FLOWER_POT) {
            return false;
        }

        if(!player.isCreative())
            stack.shrink(1);
        ModDataAttachments.PLANT_HOLDER_DATA.set(blockEntity, plantData.set(index, pottedBlock));
        return true;
    }

    @Override
    public void remove(Level level, BlockPos pos, BlockState state, @Nullable FurnitureBlockEntity blockEntity, Player player, int index) {

    }

    @Override
    public boolean isBlockEntityNeeded() {
        return true;
    }
}
