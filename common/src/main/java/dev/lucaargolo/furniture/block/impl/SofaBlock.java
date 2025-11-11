package dev.lucaargolo.furniture.block.impl;

import dev.lucaargolo.furniture.block.FurnitureConnectingBlock;
import dev.lucaargolo.furniture.block.ModBlockShapes;
import dev.lucaargolo.furniture.block.base.ColorBlock;
import dev.lucaargolo.furniture.utils.FurnitureData;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SofaBlock extends FurnitureConnectingBlock implements ColorBlock {

    private final DyeColor color;

    public SofaBlock(Block base, TagKey<Block> connecting, DyeColor color) {
        super(base, ModBlockShapes.EMPTY, connecting);
        this.color = color;
    }

    @Override
    public VoxelShape getShapeForData(BlockGetter level, BlockPos pos, BlockState state, FurnitureData data) {
        return Shapes.block();
    }

    @Override
    public ConnectionType getType() {
        return ConnectionType.SOFA;
    }

    @Override
    public DyeColor getColor() {
        return color;
    }

}
