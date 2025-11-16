package dev.lucaargolo.furniture.block.impl;

import dev.lucaargolo.furniture.block.FurnitureBlock;
import dev.lucaargolo.furniture.block.entity.PlantHolderBlockEntity;
import dev.lucaargolo.furniture.block.interaction.Interaction;
import dev.lucaargolo.furniture.block.interaction.PlantInteraction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class PlantHolderBlock extends FurnitureBlock implements EntityBlock {

    private final List<? extends Interaction<?>> interactions;

    public PlantHolderBlock(Block base, VoxelShape[] shapes, Vec3... plants) {
        super(base, shapes);
        this.interactions = Arrays.stream(plants).map(PlantInteraction::new).toList();
    }

    @Override
    public List<? extends Interaction<?>> getInteractions() {
        return interactions;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new PlantHolderBlockEntity(pos, state);
    }

}
