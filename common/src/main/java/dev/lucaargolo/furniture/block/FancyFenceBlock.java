package dev.lucaargolo.furniture.block;

import dev.lucaargolo.furniture.block.base.WoodBlock;
import dev.lucaargolo.furniture.utils.FurnitureData;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class FancyFenceBlock extends FurnitureBlock {

    public static final EnumProperty<Connecting> CONNECTING = EnumProperty.create("connecting", Connecting.class);

    public FancyFenceBlock(Block base, VoxelShape[] shapes) {
        super(base, shapes);
        this.registerDefaultState(this.defaultBlockState().setValue(CONNECTING, Connecting.NONE));
    }

    @Override
    public void createBlockStateDefinition(@NotNull StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(CONNECTING);
    }

    @Override
    protected VoxelShape getShapeForData(BlockState state, FurnitureData data) {
        return Shapes.block();
    }

    public static class Hedge extends FancyFenceBlock implements WoodBlock.LeafBlock {

        private final WoodType wood;

        public Hedge(Block base, WoodType wood, VoxelShape[] shapes) {
            super(base, shapes);
            this.wood = wood;
        }

        @Override
        public WoodType getWood() {
            return wood;
        }

    }

    public enum Connecting implements StringRepresentable {
        NONE,
        NORTH,
        NORTHEAST,
        EAST,
        SOUTHEAST,
        SOUTH,
        SOUTHWEST,
        WEST,
        NORTHWEST,;

        @Override
        @NotNull
        public String getSerializedName() {
            return name().toLowerCase(Locale.US);
        }
    }

}
