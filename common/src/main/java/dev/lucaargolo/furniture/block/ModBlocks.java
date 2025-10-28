package dev.lucaargolo.furniture.block;

import com.mojang.datafixers.util.Pair;
import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.ModRegistry;
import dev.lucaargolo.furniture.utils.WeatheringEntry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.BlockFamilies;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.function.TriFunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ModBlocks {

    public static final ModRegistry<Block> BLOCKS = FurnitureMod.INSTANCE.registry(Registries.BLOCK);
    public static final List<WeatheringEntry> WEATHERING_ENTRIES = new ArrayList<>();

    public static final Map<WoodType, Supplier<WoodFurnitureBlock>> SMALL_TABLE_MAP = registerForWoodType("small_table", WoodFurnitureBlock::new, ModBlockShapes.SMALL_TABLE, BlockTags.MINEABLE_WITH_AXE);
    public static final Map<WoodType, Supplier<SmallStoolBlock>> SMALL_STOOL_MAP = registerForWoodType("small_stool", SmallStoolBlock::new, ModBlockShapes.SMALL_STOOL, BlockTags.MINEABLE_WITH_AXE);

    public static Supplier<MetalLightFurnitureBlock> LAMP_POST = BLOCKS.register("iron_lamp_post", () -> new MetalLightFurnitureBlock(MetalBlock.MetalType.IRON, ModBlockShapes.LAMP_POST), BlockTags.NEEDS_STONE_TOOL, BlockTags.MINEABLE_WITH_PICKAXE);
    public static Supplier<MetalLightFurnitureBlock> DUAL_LAMP_POST = BLOCKS.register("iron_dual_lamp_post", () -> new MetalLightFurnitureBlock(MetalBlock.MetalType.IRON, ModBlockShapes.DUAL_LAMP_POST), BlockTags.NEEDS_STONE_TOOL, BlockTags.MINEABLE_WITH_PICKAXE);
    public static Supplier<MetalLightFurnitureBlock> TRIPLE_LAMP_POST = BLOCKS.register("iron_triple_lamp_post", () -> new MetalLightFurnitureBlock(MetalBlock.MetalType.IRON, ModBlockShapes.TRIPLE_LAMP_POST), BlockTags.NEEDS_STONE_TOOL, BlockTags.MINEABLE_WITH_PICKAXE);

    public static Map<WeatheringCopper.WeatherState, Pair<Supplier<MetalLightFurnitureBlock>, Supplier<MetalLightFurnitureBlock>>> COPPER_LAMP_POST = registerWeatheringCopper("copper_lamp_post", WeatheringMetalLightFurnitureBlock::new, MetalLightFurnitureBlock::new, ModBlockShapes.LAMP_POST, BlockTags.NEEDS_STONE_TOOL, BlockTags.MINEABLE_WITH_PICKAXE);
    public static Map<WeatheringCopper.WeatherState, Pair<Supplier<MetalLightFurnitureBlock>, Supplier<MetalLightFurnitureBlock>>> COPPER_DUAL_LAMP_POST = registerWeatheringCopper("copper_dual_lamp_post", WeatheringMetalLightFurnitureBlock::new, MetalLightFurnitureBlock::new, ModBlockShapes.DUAL_LAMP_POST, BlockTags.NEEDS_STONE_TOOL, BlockTags.MINEABLE_WITH_PICKAXE);
    public static Map<WeatheringCopper.WeatherState, Pair<Supplier<MetalLightFurnitureBlock>, Supplier<MetalLightFurnitureBlock>>> COPPER_TRIPLE_LAMP_POST = registerWeatheringCopper("copper_triple_lamp_post", WeatheringMetalLightFurnitureBlock::new, MetalLightFurnitureBlock::new, ModBlockShapes.TRIPLE_LAMP_POST, BlockTags.NEEDS_STONE_TOOL, BlockTags.MINEABLE_WITH_PICKAXE);

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

    private static <T extends Block> Map<WeatheringCopper.WeatherState, Pair<Supplier<T>, Supplier<T>>> registerWeatheringCopper(String path, TriFunction<MetalBlock.MetalType, WeatheringCopper.WeatherState, VoxelShape[], T> weathering, TriFunction<MetalBlock.MetalType, WeatheringCopper.WeatherState, VoxelShape[], T> waxed, VoxelShape[] shapes, TagKey<?>... tags) {
        Map<WeatheringCopper.WeatherState, Pair<Supplier<T>, Supplier<T>>> map = new HashMap<>();

        ModRegistry.ModEntry<T> unaffected = BLOCKS.register(path, () -> weathering.apply(MetalBlock.MetalType.COPPER, WeatheringCopper.WeatherState.UNAFFECTED, shapes), tags);
        ModRegistry.ModEntry<T> waxedUnaffected = BLOCKS.register("waxed_"+path, () -> waxed.apply(MetalBlock.MetalType.COPPER, WeatheringCopper.WeatherState.UNAFFECTED, shapes), tags);
        map.put(WeatheringCopper.WeatherState.UNAFFECTED, Pair.of(unaffected, waxedUnaffected));

        ModRegistry.ModEntry<T> exposed = BLOCKS.register("exposed_"+path, () -> weathering.apply(MetalBlock.MetalType.COPPER, WeatheringCopper.WeatherState.EXPOSED, shapes), tags);
        ModRegistry.ModEntry<T> waxedExposed = BLOCKS.register("waxed_exposed_"+path, () -> waxed.apply(MetalBlock.MetalType.COPPER, WeatheringCopper.WeatherState.EXPOSED, shapes), tags);
        map.put(WeatheringCopper.WeatherState.EXPOSED, Pair.of(exposed, waxedExposed));

        ModRegistry.ModEntry<T> weathered = BLOCKS.register("weathered_"+path, () -> weathering.apply(MetalBlock.MetalType.COPPER, WeatheringCopper.WeatherState.WEATHERED, shapes), tags);
        ModRegistry.ModEntry<T> waxedWeathered = BLOCKS.register("waxed_weathered_"+path, () -> waxed.apply(MetalBlock.MetalType.COPPER, WeatheringCopper.WeatherState.WEATHERED, shapes), tags);
        map.put(WeatheringCopper.WeatherState.WEATHERED, Pair.of(weathered, waxedWeathered));

        ModRegistry.ModEntry<T> oxidized = BLOCKS.register("oxidized_"+path, () -> weathering.apply(MetalBlock.MetalType.COPPER, WeatheringCopper.WeatherState.OXIDIZED, shapes), tags);
        ModRegistry.ModEntry<T> waxedOxidized = BLOCKS.register("waxed_oxidized_"+path, () -> waxed.apply(MetalBlock.MetalType.COPPER, WeatheringCopper.WeatherState.OXIDIZED, shapes), tags);
        map.put(WeatheringCopper.WeatherState.OXIDIZED, Pair.of(oxidized, waxedOxidized));

        WEATHERING_ENTRIES.add(new WeatheringEntry(unaffected, exposed, weathered, oxidized, waxedUnaffected, waxedExposed, waxedWeathered, waxedOxidized));
        return map;
    }



}
