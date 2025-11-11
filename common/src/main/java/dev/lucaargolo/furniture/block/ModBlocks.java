package dev.lucaargolo.furniture.block;

import com.mojang.datafixers.util.Pair;
import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.block.base.MetalBlock;
import dev.lucaargolo.furniture.block.base.StoneBlock;
import dev.lucaargolo.furniture.block.base.WoodBlock;
import dev.lucaargolo.furniture.block.impl.*;
import dev.lucaargolo.furniture.registry.ModBlockRegistry;
import dev.lucaargolo.furniture.utils.TintColor;
import dev.lucaargolo.furniture.utils.WeatheringEntry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.BlockFamilies;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class ModBlocks {

    public static final ModBlockRegistry REGISTRY = FurnitureMod.INSTANCE.blockRegistry();
    public static final List<WeatheringEntry> WEATHERING_ENTRIES = new ArrayList<>();

    public static final Map<WoodType, Supplier<TableBlock>> TABLE_MAP = registerForTable("table", TableBlock::new, ModBlockShapes.TABLE, ModBlockShapes.TABLE_FOOT, true, ModBlockTags.CONNECTING_TABLE, BlockTags.MINEABLE_WITH_AXE);
    public static final Map<WoodType, Supplier<TableBlock>> COFFEE_TABLE_MAP = registerForTable("coffee_table", TableBlock::new, ModBlockShapes.COFFEE_TABLE, ModBlockShapes.COFFEE_TABLE_FOOT, false, ModBlockTags.CONNECTING_COFFEE_TABLE, BlockTags.MINEABLE_WITH_AXE);

    public static final Map<WoodType, Supplier<WoodSeatFurnitureBlock>> SMALL_STOOL_MAP = registerForWood("small_stool", WoodBlock::getPlanks, WoodSeatFurnitureBlock::new, ModBlockShapes.SMALL_STOOL, BlockTags.MINEABLE_WITH_AXE);
    public static final Map<WoodType, Supplier<WoodSeatFurnitureBlock>> CHAIR_MAP = registerForWood("chair", WoodBlock::getPlanks, WoodSeatFurnitureBlock::new, ModBlockShapes.CHAIR, BlockTags.MINEABLE_WITH_AXE);

    public static final Map<WoodType, Supplier<OutdoorBenchBlock>> OUTDOOR_BENCH_MAP = registerForWood("outdoor_bench", WoodBlock::getPlanks, (base, wood, shapes) -> {
        return new OutdoorBenchBlock(base, MetalBlock.MetalType.CAST_IRON, WeatheringCopper.WeatherState.UNAFFECTED, wood, shapes, new Vec3(-0.5, 0.5, 0.0), new Vec3(0.5, 0.5, 0.0));
    }, ModBlockShapes.OUTDOOR_BENCH, BlockTags.MINEABLE_WITH_AXE);
    public static final Map<WoodType, Supplier<WoodSeatFurnitureBlock>> PICNIC_BENCH_MAP = registerForWood("picnic_bench", WoodBlock::getPlanks, (base, wood, shapes) -> {
        return new WoodSeatFurnitureBlock(base, wood, shapes, new Vec3(-1.0, 0.5, 1.0), new Vec3(0.0, 0.5, 1.0), new Vec3(1.0, 0.5, 1.0), new Vec3(-1.0, 0.5, -1.0), new Vec3(0.0, 0.5, -1.0), new Vec3(1.0, 0.5, -1.0));
    }, ModBlockShapes.PICNIC_BENCH, BlockTags.MINEABLE_WITH_AXE);

    public static Supplier<LampPostBlock> LAMP_POST = REGISTRY.register("cast_iron_lamp_post", () -> new LampPostBlock(MetalBlock.MetalType.CAST_IRON, ModBlockShapes.LAMP_POST), BlockTags.NEEDS_STONE_TOOL, BlockTags.MINEABLE_WITH_PICKAXE);
    public static Supplier<LampPostBlock> DUAL_LAMP_POST = REGISTRY.register("cast_iron_dual_lamp_post", () -> new LampPostBlock(MetalBlock.MetalType.CAST_IRON, ModBlockShapes.DUAL_LAMP_POST), BlockTags.NEEDS_STONE_TOOL, BlockTags.MINEABLE_WITH_PICKAXE);
    public static Supplier<LampPostBlock> TRIPLE_LAMP_POST = REGISTRY.register("cast_iron_triple_lamp_post", () -> new LampPostBlock(MetalBlock.MetalType.CAST_IRON, ModBlockShapes.TRIPLE_LAMP_POST), BlockTags.NEEDS_STONE_TOOL, BlockTags.MINEABLE_WITH_PICKAXE);

    public static final Map<Pair<StoneBlock.StoneType, WoodType>, Supplier<KitchenCounterBlock>> KITCHEN_COUNTER_MAP = registerForCounter("kitchen_counter", true, KitchenCounterBlock::new, ModBlockTags.CONNECTING_KITCHEN_COUNTER, BlockTags.MINEABLE_WITH_PICKAXE);

    public static Supplier<KitchenSinkBlock> IRON_KITCHEN_SINK = REGISTRY.register("iron_kitchen_sink", () -> new KitchenSinkBlock.Metal(MetalBlock.MetalType.IRON), ModBlockTags.TOP_FOR_KITCHEN_COUNTER, BlockTags.NEEDS_STONE_TOOL, BlockTags.MINEABLE_WITH_PICKAXE);
    public static Supplier<KitchenSinkBlock> CAST_IRON_KITCHEN_SINK = REGISTRY.register("cast_iron_kitchen_sink", () -> new KitchenSinkBlock.Metal(MetalBlock.MetalType.CAST_IRON), ModBlockTags.TOP_FOR_KITCHEN_COUNTER, BlockTags.NEEDS_STONE_TOOL, BlockTags.MINEABLE_WITH_PICKAXE);
    public static Map<WeatheringCopper.WeatherState, Pair<Supplier<KitchenSinkBlock>, Supplier<KitchenSinkBlock>>> COPPER_KITCHEN_SINK_MAP = registerWeathering("copper_kitchen_sink", KitchenSinkBlock.Weathering::new, KitchenSinkBlock.Metal::new, ModBlockShapes.KITCHEN_SINK, ModBlockTags.TOP_FOR_KITCHEN_COUNTER, BlockTags.NEEDS_STONE_TOOL, BlockTags.MINEABLE_WITH_PICKAXE);
    public static Supplier<KitchenSinkBlock> QUARTZ_KITCHEN_SINK = REGISTRY.register("quartz_block_kitchen_sink", () -> new KitchenSinkBlock.Stone(StoneBlock.StoneType.QUARTZ_BLOCK), ModBlockTags.TOP_FOR_KITCHEN_COUNTER, BlockTags.MINEABLE_WITH_PICKAXE);

    public static Supplier<FurnitureBlock> FRIDGE = REGISTRY.register("fridge", () -> new FurnitureBlock(Blocks.IRON_BLOCK, ModBlockShapes.FRIDGE), BlockTags.NEEDS_STONE_TOOL, BlockTags.MINEABLE_WITH_PICKAXE);
    public static Supplier<FurnitureBlock> BIG_FRIDGE = REGISTRY.register("big_fridge", () -> new FurnitureBlock(Blocks.IRON_BLOCK, ModBlockShapes.BIG_FRIDGE), BlockTags.NEEDS_STONE_TOOL, BlockTags.MINEABLE_WITH_PICKAXE);

    public static Supplier<FurnitureBlock> STOVE = REGISTRY.register("stove", () -> new FurnitureBlock(Blocks.IRON_BLOCK, ModBlockShapes.STOVE), BlockTags.NEEDS_STONE_TOOL, BlockTags.MINEABLE_WITH_PICKAXE);

    public static final Map<WoodType, Supplier<FurnitureFenceBlock.Hedge>> HEDGE_MAP = registerForHedge("hedge", WoodBlock::getLeaves, WoodBlock::getLeavesColor, FurnitureFenceBlock.Hedge::new, 4f, ModBlockTags.CONNECTING_HEDGE, BlockTags.MINEABLE_WITH_AXE);

    public static final Map<DyeColor, Supplier<SofaBlock>> SOFA_MAP = registerForSofa("sofa", Blocks.OAK_PLANKS, SofaBlock::new, ModBlockTags.CONNECTING_SOFA, BlockTags.MINEABLE_WITH_AXE);


    private static <T extends Block> Map<WoodType, Supplier<T>> registerForTable(String path, HexaFunction<Block, TagKey<Block>, WoodType, VoxelShape[], VoxelShape[], Boolean, T> furnitureConstructor, VoxelShape[] centerShapes, VoxelShape[] footShapes, boolean simple, TagKey<?>... tags) {
        return registerForWood(path, WoodBlock::getPlanks, (block, wood, shapes) -> furnitureConstructor.apply(block, tags[0].cast(Registries.BLOCK).orElseThrow(), wood, shapes, footShapes, simple), centerShapes, tags);
    }

    private static <T extends Block> Map<Pair<StoneBlock.StoneType, WoodType>, Supplier<T>> registerForCounter(String path, boolean polished, QuadFunction<Block, TagKey<Block>, StoneBlock.StoneType, WoodType, T> furnitureConstructor, TagKey<?>... tags) {
        return registerForStoneAndWood(path, polished, (block, stone, wood, shapes) -> furnitureConstructor.apply(block, tags[0].cast(Registries.BLOCK).orElseThrow(), stone, wood), ModBlockShapes.EMPTY, tags);
    }

    private static <T extends Block> Map<WoodType, Supplier<T>> registerForWood(String path, Function<WoodType, Optional<Block>> baseGetter, TriFunction<Block, WoodType, VoxelShape[], T> furnitureConstructor, VoxelShape[] shapes, TagKey<?>... tags) {
        return registerForWood(path, baseGetter, wood -> null, furnitureConstructor, shapes, tags);
    }

    private static <T extends Block> Map<WoodType, Supplier<T>> registerForHedge(String path, Function<WoodType, Optional<Block>> baseGetter, Function<WoodType, TintColor.Block> blockColorGetter, QuadFunction<Block, TagKey<Block>, WoodType, Float, T> furnitureConstructor, float size, TagKey<?>... tags) {
        return registerForWood(path, baseGetter, blockColorGetter, (block, wood, shapes) -> furnitureConstructor.apply(block, tags[0].cast(Registries.BLOCK).orElseThrow(), wood, size), ModBlockShapes.EMPTY, tags);
    }

    private static <T extends Block> Map<WoodType, Supplier<T>> registerForWood(String path, Function<WoodType, Optional<Block>> baseGetter, Function<WoodType, TintColor.Block> blockColorGetter, TriFunction<Block, WoodType, VoxelShape[], T> furnitureConstructor, VoxelShape[] shapes, TagKey<?>... tags) {
        Map<WoodType, Supplier<T>> map = new HashMap<>();
        getAllBaseWoodBlocks().forEach(location -> {
            WoodType.values().filter(t -> t.name().equals(location.getPath())).findFirst()
                    .ifPresent(wood -> baseGetter.apply(wood)
                            .ifPresent(base -> map.put(wood, REGISTRY.register(location.getPath() + "_" + path, () -> furnitureConstructor.apply(base, wood, shapes), tags).withTintColor(blockColorGetter.apply(wood)))));
        });
        return map;
    }

    private static <T extends Block> Map<Pair<StoneBlock.StoneType, WoodType>, Supplier<T>> registerForStoneAndWood(String path, boolean polished, QuadFunction<Block, StoneBlock.StoneType, WoodType, VoxelShape[], T> furnitureConstructor, VoxelShape[] shapes, TagKey<?>... tags) {
        Map<Pair<StoneBlock.StoneType, WoodType>, Supplier<T>> map = new HashMap<>();
        getAllBaseWoodBlocks().forEach(location -> {
            Optional<WoodType> wood = WoodType.values().filter(t -> t.name().equals(location.getPath())).findFirst();
            if(wood.isPresent()) {
                for(StoneBlock.StoneType stoneType : StoneBlock.StoneType.values()) {
                    if(polished == stoneType.isPolished()) {
                        map.put(Pair.of(stoneType, wood.get()), REGISTRY.register(location.getPath()+"_"+stoneType.getPath()+"_"+path, () -> furnitureConstructor.apply(stoneType.getBase(), stoneType, wood.get(), shapes), tags));
                    }
                }
            }
        });
        return map;
    }

    private static <T extends Block> Map<DyeColor, Supplier<T>> registerForSofa(String path, Block base, TriFunction<Block, TagKey<Block>, DyeColor, T> furnitureConstructor, TagKey<?>... tags) {
        return registerForColor(path, base, (block, color, shapes) -> furnitureConstructor.apply(base, tags[0].cast(Registries.BLOCK).orElseThrow(), color), ModBlockShapes.EMPTY, tags);
    }

    private static <T extends Block> Map<DyeColor, Supplier<T>> registerForColor(String path, Block base, TriFunction<Block, DyeColor, VoxelShape[], T> furnitureConstructor, VoxelShape[] shapes, TagKey<?>... tags) {
        Map<DyeColor, Supplier<T>> map = new HashMap<>();
        for(DyeColor color : DyeColor.values()) {
            map.put(color, REGISTRY.register(color.getSerializedName() + "_" + path, () -> furnitureConstructor.apply(base, color, shapes), tags));
        }
        return map;
    }

    private static <T extends Block> Map<WeatheringCopper.WeatherState, Pair<Supplier<T>, Supplier<T>>> registerWeathering(String path, TriFunction<MetalBlock.MetalType, WeatheringCopper.WeatherState, VoxelShape[], T> weathering, TriFunction<MetalBlock.MetalType, WeatheringCopper.WeatherState, VoxelShape[], T> waxed, VoxelShape[] shapes, TagKey<?>... tags) {
        Map<WeatheringCopper.WeatherState, Pair<Supplier<T>, Supplier<T>>> map = new HashMap<>();

        ModBlockRegistry.BlockEntry<T> unaffected = REGISTRY.register(path, () -> weathering.apply(MetalBlock.MetalType.COPPER, WeatheringCopper.WeatherState.UNAFFECTED, shapes), tags);
        ModBlockRegistry.BlockEntry<T> waxedUnaffected = REGISTRY.register("waxed_"+path, () -> waxed.apply(MetalBlock.MetalType.COPPER, WeatheringCopper.WeatherState.UNAFFECTED, shapes), tags);
        map.put(WeatheringCopper.WeatherState.UNAFFECTED, Pair.of(unaffected, waxedUnaffected));

        ModBlockRegistry.BlockEntry<T> exposed = REGISTRY.register("exposed_"+path, () -> weathering.apply(MetalBlock.MetalType.COPPER, WeatheringCopper.WeatherState.EXPOSED, shapes), tags);
        ModBlockRegistry.BlockEntry<T> waxedExposed = REGISTRY.register("waxed_exposed_"+path, () -> waxed.apply(MetalBlock.MetalType.COPPER, WeatheringCopper.WeatherState.EXPOSED, shapes), tags);
        map.put(WeatheringCopper.WeatherState.EXPOSED, Pair.of(exposed, waxedExposed));

        ModBlockRegistry.BlockEntry<T> weathered = REGISTRY.register("weathered_"+path, () -> weathering.apply(MetalBlock.MetalType.COPPER, WeatheringCopper.WeatherState.WEATHERED, shapes), tags);
        ModBlockRegistry.BlockEntry<T> waxedWeathered = REGISTRY.register("waxed_weathered_"+path, () -> waxed.apply(MetalBlock.MetalType.COPPER, WeatheringCopper.WeatherState.WEATHERED, shapes), tags);
        map.put(WeatheringCopper.WeatherState.WEATHERED, Pair.of(weathered, waxedWeathered));

        ModBlockRegistry.BlockEntry<T> oxidized = REGISTRY.register("oxidized_"+path, () -> weathering.apply(MetalBlock.MetalType.COPPER, WeatheringCopper.WeatherState.OXIDIZED, shapes), tags);
        ModBlockRegistry.BlockEntry<T> waxedOxidized = REGISTRY.register("waxed_oxidized_"+path, () -> waxed.apply(MetalBlock.MetalType.COPPER, WeatheringCopper.WeatherState.OXIDIZED, shapes), tags);
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
                .map(r -> ResourceLocation.fromNamespaceAndPath(r.getNamespace(), r.getPath().replace("_planks", "")))
                .filter(f -> f.getNamespace().equals("minecraft"))
                .sorted();
    }

}
