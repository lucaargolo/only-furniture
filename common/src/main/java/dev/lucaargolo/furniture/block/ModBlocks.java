package dev.lucaargolo.furniture.block;

import com.mojang.datafixers.util.Pair;
import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.ModRegistry;
import dev.lucaargolo.furniture.block.base.MetalBlock;
import dev.lucaargolo.furniture.block.base.StoneBlock;
import dev.lucaargolo.furniture.block.impl.*;
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
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ModBlocks {

    public static final ModRegistry<Block> BLOCKS = FurnitureMod.INSTANCE.registry(Registries.BLOCK);
    public static final List<WeatheringEntry> WEATHERING_ENTRIES = new ArrayList<>();

    public static final Map<WoodType, Supplier<TableBlock>> TABLE_MAP = registerForTable("table", TableBlock::new, ModBlockShapes.TABLE, ModBlockShapes.TABLE_FOOT, true, ModBlockTags.CONNECTING_TABLE, BlockTags.MINEABLE_WITH_AXE);
    public static final Map<WoodType, Supplier<TableBlock>> COFFEE_TABLE_MAP = registerForTable("coffee_table", TableBlock::new, ModBlockShapes.COFFEE_TABLE, ModBlockShapes.COFFEE_TABLE_FOOT, false, ModBlockTags.CONNECTING_COFFEE_TABLE, BlockTags.MINEABLE_WITH_AXE);
    public static final Map<WoodType, Supplier<WoodFurnitureSeatBlock>> SMALL_STOOL_MAP = registerForWoodType("small_stool", WoodFurnitureSeatBlock::new, ModBlockShapes.SMALL_STOOL, BlockTags.MINEABLE_WITH_AXE);
    public static final Map<WoodType, Supplier<WoodFurnitureSeatBlock>> CHAIR_MAP = registerForWoodType("chair", WoodFurnitureSeatBlock::new, ModBlockShapes.CHAIR, BlockTags.MINEABLE_WITH_AXE);

    public static final Map<WoodType, Supplier<OutdoorBenchBlock>> OUTDOOR_BENCH_MAP = registerForWoodType("outdoor_bench", (base, wood, shapes) -> {
        return new OutdoorBenchBlock(base, MetalBlock.MetalType.CAST_IRON, WeatheringCopper.WeatherState.UNAFFECTED, wood, shapes, new Vec3(-0.5, 0.5, 0.0), new Vec3(0.5, 0.5, 0.0));
    }, ModBlockShapes.OUTDOOR_BENCH, BlockTags.MINEABLE_WITH_AXE);
    public static final Map<WoodType, Supplier<WoodFurnitureSeatBlock>> PICNIC_BENCH_MAP = registerForWoodType("picnic_bench", (base, wood, shapes) -> {
        return new WoodFurnitureSeatBlock(base, wood, shapes, new Vec3(-1.0, 0.5, 1.0), new Vec3(0.0, 0.5, 1.0), new Vec3(1.0, 0.5, 1.0), new Vec3(-1.0, 0.5, -1.0), new Vec3(0.0, 0.5, -1.0), new Vec3(1.0, 0.5, -1.0));
    }, ModBlockShapes.PICNIC_BENCH, BlockTags.MINEABLE_WITH_AXE);

    public static Supplier<LampPostBlock> LAMP_POST = BLOCKS.register("cast_iron_lamp_post", () -> new LampPostBlock(MetalBlock.MetalType.CAST_IRON, ModBlockShapes.LAMP_POST), BlockTags.NEEDS_STONE_TOOL, BlockTags.MINEABLE_WITH_PICKAXE);
    public static Supplier<LampPostBlock> DUAL_LAMP_POST = BLOCKS.register("cast_iron_dual_lamp_post", () -> new LampPostBlock(MetalBlock.MetalType.CAST_IRON, ModBlockShapes.DUAL_LAMP_POST), BlockTags.NEEDS_STONE_TOOL, BlockTags.MINEABLE_WITH_PICKAXE);
    public static Supplier<LampPostBlock> TRIPLE_LAMP_POST = BLOCKS.register("cast_iron_triple_lamp_post", () -> new LampPostBlock(MetalBlock.MetalType.CAST_IRON, ModBlockShapes.TRIPLE_LAMP_POST), BlockTags.NEEDS_STONE_TOOL, BlockTags.MINEABLE_WITH_PICKAXE);

    public static final Map<Pair<StoneBlock.StoneType, WoodType>, Supplier<KitchenCounterBlock>> KITCHEN_COUNTER_MAP = registerForCounter("kitchen_counter", true, KitchenCounterBlock::new, ModBlockTags.CONNECTING_KITCHEN_COUNTER, BlockTags.MINEABLE_WITH_PICKAXE);

    public static Supplier<KitchenSinkBlock> IRON_KITCHEN_SINK = BLOCKS.register("iron_kitchen_sink", () -> new KitchenSinkBlock.Metal(MetalBlock.MetalType.IRON), ModBlockTags.TOP_FOR_KITCHEN_COUNTER, BlockTags.NEEDS_STONE_TOOL, BlockTags.MINEABLE_WITH_PICKAXE);
    public static Supplier<KitchenSinkBlock> CAST_IRON_KITCHEN_SINK = BLOCKS.register("cast_iron_kitchen_sink", () -> new KitchenSinkBlock.Metal(MetalBlock.MetalType.CAST_IRON), ModBlockTags.TOP_FOR_KITCHEN_COUNTER, BlockTags.NEEDS_STONE_TOOL, BlockTags.MINEABLE_WITH_PICKAXE);
    public static Map<WeatheringCopper.WeatherState, Pair<Supplier<KitchenSinkBlock>, Supplier<KitchenSinkBlock>>> COPPER_KITCHEN_SINK_MAP = registerWeatheringCopper("copper_kitchen_sink", KitchenSinkBlock.Weathering::new, KitchenSinkBlock.Metal::new, ModBlockShapes.KITCHEN_SINK, ModBlockTags.TOP_FOR_KITCHEN_COUNTER, BlockTags.NEEDS_STONE_TOOL, BlockTags.MINEABLE_WITH_PICKAXE);
    public static Supplier<KitchenSinkBlock> QUARTZ_KITCHEN_SINK = BLOCKS.register("quartz_block_kitchen_sink", () -> new KitchenSinkBlock.Stone(StoneBlock.StoneType.QUARTZ_BLOCK), ModBlockTags.TOP_FOR_KITCHEN_COUNTER, BlockTags.MINEABLE_WITH_PICKAXE);

    private static <T extends Block> Map<WoodType, Supplier<T>> registerForTable(String path, HexaFunction<Block, TagKey<Block>, WoodType, VoxelShape[], VoxelShape[], Boolean, T> o, VoxelShape[] centerShapes, VoxelShape[] footShapes, boolean simple, TagKey<?>... tags) {
        return registerForWoodType(path, (block, wood, shapes) -> o.apply(block, tags[0].cast(Registries.BLOCK).orElseThrow(), wood, shapes, footShapes, simple), centerShapes, tags);
    }

    private static <T extends Block> Map<Pair<StoneBlock.StoneType, WoodType>, Supplier<T>> registerForCounter(String path, boolean polished, QuadFunction<Block, TagKey<Block>, StoneBlock.StoneType, WoodType, T> o, TagKey<?>... tags) {
        return registerForStoneAndWoodType(path, polished, (block, stone, wood, shapes) -> o.apply(block, tags[0].cast(Registries.BLOCK).orElseThrow(), stone, wood), ModBlockShapes.EMPTY, tags);
    }

    private static <T extends Block> Map<WoodType, Supplier<T>> registerForWoodType(String path, TriFunction<Block, WoodType, VoxelShape[], T> o, VoxelShape[] shapes, TagKey<?>... tags) {
        Map<WoodType, Supplier<T>> map = new HashMap<>();
        getAllBaseWoodBlocks().forEach(location -> {
            String woodPath = location.withPath(s -> s.replace("_planks", "")).getPath();
            WoodType.values().filter(t -> t.name().equals(woodPath)).findFirst().ifPresent(woodType -> {
                map.put(woodType, BLOCKS.register(woodPath+"_"+path, () -> o.apply(BuiltInRegistries.BLOCK.get(location), woodType, shapes), tags));
            });
        });
        return map;
    }


    private static <T extends Block> Map<Pair<StoneBlock.StoneType, WoodType>, Supplier<T>> registerForStoneAndWoodType(String path, boolean polished, QuadFunction<Block, StoneBlock.StoneType, WoodType, VoxelShape[], T> o, VoxelShape[] shapes, TagKey<?>... tags) {
        Map<Pair<StoneBlock.StoneType, WoodType>, Supplier<T>> map = new HashMap<>();
        getAllBaseWoodBlocks().forEach(location -> {
            String woodPath = location.withPath(s -> s.replace("_planks", "")).getPath();
            WoodType.values().filter(t -> t.name().equals(woodPath)).findFirst().ifPresent(woodType -> {
                for(StoneBlock.StoneType stoneType : StoneBlock.StoneType.values()) {
                    if(polished == stoneType.isPolished()) {
                        map.put(Pair.of(stoneType, woodType), BLOCKS.register(woodPath+"_"+stoneType.getPath()+"_"+path, () -> o.apply(stoneType.getBase(), stoneType, woodType, shapes), tags));
                    }
                }
            });
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

    @FunctionalInterface
    private interface HexaFunction<P1, P2, P3, P4, P5, P6, R> {
        R apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6);
    }

    @FunctionalInterface
    private interface PentaFunction<P1, P2, P3, P4, P5, R> {
        R apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5);
    }

    @FunctionalInterface
    private interface QuadFunction<P1, P2, P3, P4, R> {
        R apply(P1 p1, P2 p2, P3 p3, P4 p4);
    }

    @FunctionalInterface
    private interface TriFunction<P1, P2, P3, R> {
        R apply(P1 p1, P2 p2, P3 p3);
    }

    private static Stream<ResourceLocation> getAllBaseWoodBlocks() {
        return BlockFamilies.getAllFamilies()
                .filter(f -> f.getRecipeGroupPrefix().orElse("").equals("wooden"))
                .map(f -> BuiltInRegistries.BLOCK.getKey(f.getBaseBlock()))
                .filter(f -> f.getNamespace().equals("minecraft"))
                .sorted();
    }

}
