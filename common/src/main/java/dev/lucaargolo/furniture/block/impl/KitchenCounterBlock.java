package dev.lucaargolo.furniture.block.impl;

import dev.lucaargolo.furniture.block.FurnitureConnectingBlock;
import dev.lucaargolo.furniture.block.base.StoneBlock;
import dev.lucaargolo.furniture.block.base.WoodBlock;
import dev.lucaargolo.furniture.utils.FurnitureData;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class KitchenCounterBlock extends FurnitureConnectingBlock implements StoneBlock, WoodBlock {

    private final StoneType stone;
    private final WoodType wood;

    public KitchenCounterBlock(Block base, VoxelShape[] shapes, TagKey<Block> connecting, StoneType stone, WoodType wood) {
        super(base, shapes, connecting);
        this.stone = stone;
        this.wood = wood;
    }

    @Override
    public ConnectionType getType() {
        return ConnectionType.COUNTER;
    }

    @Override
    protected VoxelShape getShapeForData(BlockState state, FurnitureData data) {
        return Shapes.block();
    }

    @Override
    public StoneType getStone() {
        return stone;
    }

    @Override
    public WoodType getWood() {
        return wood;
    }

}
