package dev.lucaargolo.furniture.block;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.utils.ModRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.BlockFamilies;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.shapes.Shapes;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class ModBlocks {

    public static final ModRegistry<Block> BLOCKS = FurnitureMod.INSTANCE.registry(Registries.BLOCK);

    public static final Map<WoodType, Supplier<SmallTableBlock>> SMALL_TABLE_MAP = registerForWoodType(SmallTableBlock::new);

    public static Supplier<FurnitureBlock> LAMP_POST = BLOCKS.register("lamp_post", () -> new FurnitureBlock(Blocks.IRON_BLOCK,
        Shapes.box(0.1875,0,0.25,0.25,0.0625,0.75),
        Shapes.box(0.25,0,0.1875,0.75,0.0625,0.8125),
        Shapes.box(0.75,0,0.25,0.8125,0.0625,0.75),
        Shapes.box(0.3125,0.0625,0.375,0.375,0.125,0.625),
        Shapes.box(0.375,0.0625,0.3125,0.625,0.125,0.6875),
        Shapes.box(0.625,0.0625,0.375,0.6875,0.125,0.625),
        Shapes.box(0.375,0.125,0.375,0.625,0.75,0.625),
        Shapes.box(0.4375,0.75,0.4375,0.5625,1,0.5625),
        Shapes.box(0.4375,1,0.4375,0.5625,2,0.5625),
        Shapes.box(0.3125,2,0.4375,0.4375,2.125,0.5625),
        Shapes.box(0.4375,2,0.4375,0.5625,2.375,0.5625),
        Shapes.box(0.5625,2,0.4375,0.6875,2.125,0.5625),
        Shapes.box(0.25,2.0625,0.4375,0.3125,2.125,0.5625),
        Shapes.box(0.6875,2.0625,0.4375,0.75,2.125,0.5625),
        Shapes.box(0.1875,2.125,0.4375,0.3125,2.25,0.5625),
        Shapes.box(0.3125,2.125,0.4375,0.375,2.1875,0.5625),
        Shapes.box(0.625,2.125,0.4375,0.6875,2.1875,0.5625),
        Shapes.box(0.6875,2.125,0.4375,0.8125,2.25,0.5625),
        Shapes.box(0.125,2.25,0.4375,0.4375,2.375,0.5625),
        Shapes.box(0.5625,2.25,0.4375,0.875,2.375,0.5625),
        Shapes.box(0.125,2.375,0.4375,0.25,2.4375,0.5625),
        Shapes.box(0.75,2.375,0.4375,0.875,2.4375,0.5625),
        Shapes.box(0,2.4375,0.3125,0.375,2.5,0.6875),
        Shapes.box(0.625,2.4375,0.3125,1,2.5,0.6875),
        Shapes.box(0,2.5,0.3125,0.0625,2.875,0.375),
        Shapes.box(0,2.5,0.625,0.0625,2.875,0.6875),
        Shapes.box(0.0625,2.5,0.375,0.3125,2.875,0.625),
        Shapes.box(0.3125,2.5,0.3125,0.375,2.875,0.375),
        Shapes.box(0.3125,2.5,0.625,0.375,2.875,0.6875),
        Shapes.box(0.625,2.5,0.3125,0.6875,2.875,0.375),
        Shapes.box(0.625,2.5,0.625,0.6875,2.875,0.6875),
        Shapes.box(0.6875,2.5,0.375,0.9375,2.875,0.625),
        Shapes.box(0.9375,2.5,0.3125,1,2.875,0.375),
        Shapes.box(0.9375,2.5,0.625,1,2.875,0.6875),
        Shapes.box(0,2.875,0.3125,0.375,2.9375,0.6875),
        Shapes.box(0.625,2.875,0.3125,1,2.9375,0.6875),
        Shapes.box(0.0625,2.9375,0.375,0.3125,3,0.625),
        Shapes.box(0.6875,2.9375,0.375,0.9375,3,0.625)
    ));

    private static <T extends Block> Map<WoodType, Supplier<T>> registerForWoodType(BiFunction<Block, WoodType, T> o) {
        Map<WoodType, Supplier<T>> map = new HashMap<>();
        BlockFamilies.getAllFamilies().filter(f -> f.getRecipeGroupPrefix().orElse("").equals("wooden")).forEach(family -> {
            ResourceLocation location = BuiltInRegistries.BLOCK.getKey(family.getBaseBlock());
            if(location.getNamespace().equals("minecraft")) {
                String woodType = location.withPath(s -> s.replace("_planks", "")).getPath();
                WoodType.values().filter(t -> t.name().equals(woodType)).findFirst().ifPresent(type -> {
                    map.put(type, BLOCKS.register(woodType+"_small_table", () -> o.apply(family.getBaseBlock(), type)));
                });
            }
        });
        return map;
    }



}
