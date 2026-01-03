package dev.lucaargolo.furniture.block.impl;

import com.mojang.datafixers.util.Pair;
import dev.lucaargolo.furniture.FurnitureData;
import dev.lucaargolo.furniture.block.FurnitureBlock;
import dev.lucaargolo.furniture.block.ModBlockShapes;
import dev.lucaargolo.furniture.block.behaviour.StorageBehaviour;
import dev.lucaargolo.furniture.utils.Animation;
import dev.lucaargolo.furniture.utils.Rotation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class FridgeBlock extends FurnitureBlock {

    public static final BooleanProperty TOP_OPEN = BooleanProperty.create("top_open");
    public static final BooleanProperty BOTTOM_OPEN = BooleanProperty.create("bottom_open");

    protected final Map<Pair<Direction, Rotation>, VoxelShape> openShapes;
    protected final Map<Pair<Direction, Rotation>, VoxelShape> openTopShapes;
    protected final Map<Pair<Direction, Rotation>, VoxelShape> openBottomShapes;

    private static final Animation OPEN_TOP_DOOR = new Animation("top.door", 20, -135f, 0f, Animation.Easing.EASE_IN_OUT_SINE, Animation.Type.ROTATE_Y, state -> state.setValue(TOP_OPEN, true), state -> state.setValue(TOP_OPEN, true));
    private static final Animation CLOSE_TOP_DOOR = new Animation("top.door", 20, 0f, -135f, Animation.Easing.EASE_IN_OUT_SINE, Animation.Type.ROTATE_Y, state -> state.setValue(TOP_OPEN, true), state -> state.setValue(TOP_OPEN, false));

    private static final Animation OPEN_BOTTOM_DOOR = new Animation("bottom.door", 20, -135f, 0f, Animation.Easing.EASE_IN_OUT_SINE, Animation.Type.ROTATE_Y, state -> state.setValue(BOTTOM_OPEN, true), state -> state.setValue(BOTTOM_OPEN, true));
    private static final Animation CLOSE_BOTTOM_DOOR = new Animation("bottom.door", 20, 0f, -135f, Animation.Easing.EASE_IN_OUT_SINE, Animation.Type.ROTATE_Y, state -> state.setValue(BOTTOM_OPEN, true), state -> state.setValue(BOTTOM_OPEN, false));

    public FridgeBlock() {
        super(Blocks.IRON_BLOCK, ModBlockShapes.FRIDGE,
                new StorageBehaviour(new Vec3(0.0, 1.2, 0.0), 9, Component.translatable("storage.onlyfurniture.freezer"), OPEN_TOP_DOOR, CLOSE_TOP_DOOR),
                new StorageBehaviour(new Vec3(0.0, 0.2, 0.0), 27, Component.translatable("storage.onlyfurniture.fridge"), OPEN_BOTTOM_DOOR, CLOSE_BOTTOM_DOOR)
        );
        this.openShapes = computeVoxelShapes(ModBlockShapes.FRIDGE_OPEN, false);
        this.openTopShapes = computeVoxelShapes(ModBlockShapes.FRIDGE_OPEN_TOP, false);
        this.openBottomShapes = computeVoxelShapes(ModBlockShapes.FRIDGE_OPEN_BOTTOM, false);
        BlockState state = this.defaultBlockState();
        state = state.setValue(TOP_OPEN, false);
        state = state.setValue(BOTTOM_OPEN, false);
        this.registerDefaultState(state);
    }

    @Override
    public void createBlockStateDefinition(@NotNull StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(TOP_OPEN, BOTTOM_OPEN);
    }

    @Override
    public VoxelShape getShapeForFurniture(BlockGetter level, BlockPos pos, BlockState state, FurnitureData data, int layer) {
        boolean top = state.getValue(TOP_OPEN);
        boolean bottom = state.getValue(BOTTOM_OPEN);

        Direction facing = data.getFacing(state);
        Rotation rotation = data.getRotation();

        Map<Pair<Direction, Rotation>, VoxelShape> shapes = top && bottom ? this.openShapes : top ? this.openTopShapes : bottom ? this.openBottomShapes : this.shapes;
        return shapes.get(Pair.of(facing, rotation));
    }

}
