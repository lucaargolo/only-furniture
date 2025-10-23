package dev.lucaargolo.furniture.block;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.utils.ModRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.shapes.Shapes;

import java.util.function.Supplier;

public class ModBlocks {

    public static final ModRegistry<Block> BLOCKS = FurnitureMod.INSTANCE.registry(Registries.BLOCK);

    public static Supplier<FurnitureBlock> SMALL_TABLE = BLOCKS.register("small_table", () -> new FurnitureBlock(Blocks.OAK_PLANKS,
        Shapes.box(0.125, 0, 0.75, 0.25, 0.625, 0.875),
        Shapes.box(0.125, 0, 0.125, 0.25, 0.625, 0.25),
        Shapes.box(0.75, 0, 0.125, 0.875, 0.625, 0.25),
        Shapes.box(0.75, 0, 0.75, 0.875, 0.625, 0.875),
        Shapes.box(0, 0.625, 0.125, 1, 0.75, 0.875),
        Shapes.box(0.125, 0.625, 0, 0.875, 0.75, 0.125),
        Shapes.box(0.125, 0.625, 0.875, 0.875, 0.75, 1)
    ));

}
