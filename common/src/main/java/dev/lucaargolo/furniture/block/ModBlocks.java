package dev.lucaargolo.furniture.block;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.utils.ModRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.BlockFamilies;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.function.TriFunction;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ModBlocks {

    public static final ModRegistry<Block> BLOCKS = FurnitureMod.INSTANCE.registry(Registries.BLOCK);

    public static final Map<WoodType, Supplier<WoodFurnitureBlock>> SMALL_TABLE_MAP = registerForWoodType("small_table", WoodFurnitureBlock::new, ModBlockShapes.SMALL_TABLE, BlockTags.MINEABLE_WITH_AXE);
    public static final Map<WoodType, Supplier<SmallStoolBlock>> SMALL_STOOL_MAP = registerForWoodType("small_stool", SmallStoolBlock::new, ModBlockShapes.SMALL_STOOL, BlockTags.MINEABLE_WITH_AXE);

    public static Supplier<FurnitureBlock> LAMP_POST = BLOCKS.register("lamp_post", () -> new LightFurnitureBlock(Blocks.IRON_BLOCK, ModBlockShapes.LAMP_POST), BlockTags.NEEDS_STONE_TOOL, BlockTags.MINEABLE_WITH_PICKAXE);

    private static <T extends Block> Map<WoodType, Supplier<T>> registerForWoodType(String path, TriFunction<Block, WoodType, VoxelShape[], T> o, VoxelShape[] shapes, TagKey<?>... tags) {
        Map<WoodType, Supplier<T>> map = new HashMap<>();
        BlockFamilies.getAllFamilies().filter(f -> f.getRecipeGroupPrefix().orElse("").equals("wooden")).forEach(family -> {
            ResourceLocation location = BuiltInRegistries.BLOCK.getKey(family.getBaseBlock());
            if(location.getNamespace().equals("minecraft")) {
                String woodType = location.withPath(s -> s.replace("_planks", "")).getPath();
                WoodType.values().filter(t -> t.name().equals(woodType)).findFirst().ifPresent(type -> {
                    map.put(type, BLOCKS.register(woodType+"_"+path, () -> o.apply(family.getBaseBlock(), type, shapes), tags));
                });
            }
        });
        return map;
    }



}
