package dev.lucaargolo.furniture.block;

import dev.lucaargolo.furniture.block.base.WoodBlock;
import dev.lucaargolo.furniture.item.FancyFenceBlockItem;
import dev.lucaargolo.furniture.utils.FurnitureData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class FancyFenceBlock extends FurnitureBlock {

    public static final EnumProperty<Connecting> CONNECTING = EnumProperty.create("connecting", Connecting.class);

    private final float size;

    public FancyFenceBlock(Block base, float size) {
        super(base, new VoxelShape[]{
            Block.box(8.0-(size/2.0), 0, 8.0-(size/2.0), 8.0+(size/2.0), 16, 8.0+(size/2.0))
        });
        this.size = size;
        this.registerDefaultState(this.defaultBlockState().setValue(CONNECTING, Connecting.NONE));
    }

    public float getSize() {
        return size;
    }

    @Override
    public void createBlockStateDefinition(@NotNull StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(CONNECTING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context, FurnitureData data, int layer) {
        BlockState state = super.getStateForPlacement(context, data, layer);
        BlockPos lastPosition = FancyFenceBlockItem.getLastPosition(context.getPlayer());
        if(lastPosition != null) {
            Vec3i offset = lastPosition.subtract(context.getClickedPos());
            for(Connecting c: Connecting.values()) {
                if(offset.equals(c.getOffset())) {
                    return state.setValue(CONNECTING, c);
                }
            }
        }
        return state.setValue(CONNECTING, Connecting.NONE);
    }

    @Override
    protected VoxelShape getShapeForData(BlockState state, FurnitureData data) {
        return super.getShapeForData(state, data);
    }

    public static class Hedge extends FancyFenceBlock implements WoodBlock.LeafBlock {

        private final WoodType wood;

        public Hedge(Block base, WoodType wood, float size) {
            super(base, size);
            this.wood = wood;
        }

        @Override
        public WoodType getWood() {
            return wood;
        }

    }

    public enum Connecting implements StringRepresentable {
        NONE(Vec3i.ZERO),
        NORTH(Direction.NORTH.getNormal()),
        NORTHEAST(Direction.NORTH.getNormal().relative(Direction.EAST)),
        EAST(Direction.EAST.getNormal()),
        SOUTHEAST(Direction.SOUTH.getNormal().relative(Direction.EAST)),
        SOUTH(Direction.SOUTH.getNormal()),
        SOUTHWEST(Direction.SOUTH.getNormal().relative(Direction.WEST)),
        WEST(Direction.WEST.getNormal()),
        NORTHWEST(Direction.NORTH.getNormal().relative(Direction.WEST));

        private final Vec3i offset;

        Connecting(Vec3i offset) {
            this.offset = offset;
        }

        @Override
        @NotNull
        public String getSerializedName() {
            return name().toLowerCase(Locale.US);
        }

        public Vec3i getOffset() {
            return offset;
        }

        public BlockPos getConnectedPos(BlockPos pos) {
            return pos.offset(offset);
        }

    }

}
