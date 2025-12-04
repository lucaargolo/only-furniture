package dev.lucaargolo.furniture.block.impl;

import com.mojang.datafixers.util.Pair;
import dev.lucaargolo.furniture.FurnitureData;
import dev.lucaargolo.furniture.block.FurnitureBlock;
import dev.lucaargolo.furniture.block.ModBlockShapes;
import dev.lucaargolo.furniture.block.behaviour.StorageBehaviour;
import dev.lucaargolo.furniture.utils.Rotation;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;
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

    public static final AnimationDefinition open_top = AnimationDefinition.Builder.withLength(0.25F)
            .addAnimation("door2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 135.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.25F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .build();

    public static final AnimationDefinition close_top = AnimationDefinition.Builder.withLength(0.25F)
            .addAnimation("door2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.25F, KeyframeAnimations.degreeVec(0.0F, 135.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .build();


    public FridgeBlock() {
        super(Blocks.IRON_BLOCK, ModBlockShapes.FRIDGE,
                new StorageBehaviour(new Vec3(0.0, 1.2, 0.0), 9, Component.translatable("storage.onlyfurniture.freezer"), TOP_OPEN),
                new StorageBehaviour(new Vec3(0.0, 0.2, 0.0), 27, Component.translatable("storage.onlyfurniture.fridge"), BOTTOM_OPEN)
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
