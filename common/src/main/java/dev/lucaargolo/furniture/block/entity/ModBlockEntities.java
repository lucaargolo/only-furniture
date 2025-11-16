package dev.lucaargolo.furniture.block.entity;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.block.ModBlocks;
import dev.lucaargolo.furniture.registry.ModBlockEntityRegistry;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public abstract class ModBlockEntities {

    public static final ModBlockEntityRegistry REGISTRY = FurnitureMod.blockEntityRegistry();

    public static final Supplier<BlockEntityType<PlantHolderBlockEntity>> PLANT_HOLDER = REGISTRY.register("plant_holder", PlantHolderBlockEntity::new, ModBlocks.PLANT_POT);

}
