package dev.lucaargolo.furniture.block.entity;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.block.FurnitureBlock;
import dev.lucaargolo.furniture.block.ModBlocks;
import dev.lucaargolo.furniture.block.behaviour.Behaviour;
import dev.lucaargolo.furniture.registry.ModBlockEntityTypeRegistry;
import dev.lucaargolo.furniture.registry.minecraft.MinecraftEntry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.Arrays;
import java.util.function.Supplier;

public class ModBlockEntityTypes {

    private static final Supplier<Block[]> FURNITURE_BLOCKS = () -> ModBlocks.REGISTRY.getEntries()
            .stream()
            .map(MinecraftEntry::get)
            .filter(block -> block instanceof FurnitureBlock furniture && Arrays.stream(furniture.getBehaviours()).anyMatch(Behaviour::isBlockEntityNeeded))
            .toArray(Block[]::new);

    public static final ModBlockEntityTypeRegistry REGISTRY = FurnitureMod.blockEntityTypeRegistry();

    public static final Supplier<BlockEntityType<FurnitureBlockEntity>> FURNITURE = REGISTRY.register("plant_holder", FurnitureBlockEntity::new, FURNITURE_BLOCKS);

}
