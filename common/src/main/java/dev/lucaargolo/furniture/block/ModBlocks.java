package dev.lucaargolo.furniture.block;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.utils.ModRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Supplier;

public class ModBlocks {

    public static final ModRegistry<Block> BLOCKS = FurnitureMod.INSTANCE.registry(Registries.BLOCK);

    public static Supplier<FurnitureBlock> FURNITURE = BLOCKS.register("furniture", () -> new FurnitureBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)));

}
