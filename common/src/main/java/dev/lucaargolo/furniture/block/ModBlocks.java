package dev.lucaargolo.furniture.block;

import com.mojang.datafixers.util.Pair;
import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.ModRegistry;
import dev.lucaargolo.furniture.block.base.MetalBlock;
import dev.lucaargolo.furniture.block.base.StoneBlock;
import dev.lucaargolo.furniture.block.base.impl.MetalFurnitureLightBlock;
import dev.lucaargolo.furniture.block.base.impl.MetalWoodFurnitureSeatBlock;
import dev.lucaargolo.furniture.block.base.impl.WoodFurnitureBlock;
import dev.lucaargolo.furniture.block.base.impl.WoodFurnitureSeatBlock;
import dev.lucaargolo.furniture.block.impl.CounterBlock;
import dev.lucaargolo.furniture.block.impl.TableBlock;
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

public class ModBlocks {

    public static final ModRegistry<Block> BLOCKS = FurnitureMod.INSTANCE.registry(Registries.BLOCK);
    public static final List<WeatheringEntry> WEATHERING_ENTRIES = new ArrayList<>();

    public static final Map<WoodType, Supplier<TableBlock>> TABLE_MAP = registerForConnectingWoodType("table", TableBlock::new, ModBlockTags.CONNECTING_TABLE, BlockTags.MINEABLE_WITH_AXE);
    public static final Map<WoodType, Supplier<WoodFurnitureBlock>> COFFEE_TABLE_MAP = registerForWoodType("coffee_table", WoodFurnitureBlock::new, ModBlockShapes.COFFEE_TABLE, BlockTags.MINEABLE_WITH_AXE);
    public static final Map<WoodType, Supplier<WoodFurnitureSeatBlock>> SMALL_STOOL_MAP = registerForWoodType("small_stool", WoodFurnitureSeatBlock::new, ModBlockShapes.SMALL_STOOL, BlockTags.MINEABLE_WITH_AXE);
    public static final Map<WoodType, Supplier<WoodFurnitureSeatBlock>> CHAIR_MAP = registerForWoodType("chair", WoodFurnitureSeatBlock::new, ModBlockShapes.CHAIR, BlockTags.MINEABLE_WITH_AXE);

    public static final Map<WoodType, Supplier<MetalWoodFurnitureSeatBlock>> OUTDOOR_BENCH_MAP = registerForWoodType("outdoor_bench", (base, wood, shapes) -> {
        return new MetalWoodFurnitureSeatBlock(base, MetalBlock.MetalType.IRON, WeatheringCopper.WeatherState.UNAFFECTED, wood, shapes, new Vec3(-0.5, 0.5, 0.0), new Vec3(0.5, 0.5, 0.0));
    }, ModBlockShapes.OUTDOOR_BENCH, BlockTags.MINEABLE_WITH_AXE);
    public static final Map<WoodType, Supplier<WoodFurnitureSeatBlock>> PICNIC_BENCH_MAP = registerForWoodType("picnic_bench", (base, wood, shapes) -> {
        return new WoodFurnitureSeatBlock(base, wood, shapes, new Vec3(-1.0, 0.5, 1.0), new Vec3(0.0, 0.5, 1.0), new Vec3(1.0, 0.5, 1.0), new Vec3(-1.0, 0.5, -1.0), new Vec3(0.0, 0.5, -1.0), new Vec3(1.0, 0.5, -1.0));
    }, ModBlockShapes.PICNIC_BENCH, BlockTags.MINEABLE_WITH_AXE);

    public static Supplier<MetalFurnitureLightBlock> LAMP_POST = BLOCKS.register("iron_lamp_post", () -> new MetalFurnitureLightBlock(MetalBlock.MetalType.IRON, ModBlockShapes.LAMP_POST), BlockTags.NEEDS_STONE_TOOL, BlockTags.MINEABLE_WITH_PICKAXE);
    public static Supplier<MetalFurnitureLightBlock> DUAL_LAMP_POST = BLOCKS.register("iron_dual_lamp_post", () -> new MetalFurnitureLightBlock(MetalBlock.MetalType.IRON, ModBlockShapes.DUAL_LAMP_POST), BlockTags.NEEDS_STONE_TOOL, BlockTags.MINEABLE_WITH_PICKAXE);
    public static Supplier<MetalFurnitureLightBlock> TRIPLE_LAMP_POST = BLOCKS.register("iron_triple_lamp_post", () -> new MetalFurnitureLightBlock(MetalBlock.MetalType.IRON, ModBlockShapes.TRIPLE_LAMP_POST), BlockTags.NEEDS_STONE_TOOL, BlockTags.MINEABLE_WITH_PICKAXE);

    public static final Map<Pair<StoneBlock.StoneType, WoodType>, Supplier<CounterBlock>> KITCHEN_COUNTER_MAP = registerForConnectingStoneAndWoodType("kitchen_counter", true, CounterBlock::new, ModBlockTags.CONNECTING_KITCHEN_COUNTER, BlockTags.MINEABLE_WITH_PICKAXE);

    private static <T extends Block> Map<WoodType, Supplier<T>> registerForConnectingWoodType(String path, QuadFunction<Block, VoxelShape[], TagKey<Block>, WoodType, T> o, TagKey<?>... tags) {
        return registerForWoodType(path, (block, wood, shapes) -> o.apply(block, shapes, tags[0].cast(Registries.BLOCK).orElseThrow(), wood), ModBlockShapes.EMPTY, tags);
    }

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

    private static <T extends Block> Map<Pair<StoneBlock.StoneType, WoodType>, Supplier<T>> registerForConnectingStoneAndWoodType(String path, boolean polished, PentaFunction<Block, VoxelShape[], TagKey<Block>, StoneBlock.StoneType, WoodType, T> o, TagKey<?>... tags) {
        return registerForStoneAndWoodType(path, polished, (block, stone, wood, shapes) -> o.apply(block, shapes, tags[0].cast(Registries.BLOCK).orElseThrow(), stone, wood), ModBlockShapes.EMPTY, tags);
    }

    private static <T extends Block> Map<Pair<StoneBlock.StoneType, WoodType>, Supplier<T>> registerForStoneAndWoodType(String path, boolean polished, QuadFunction<Block, StoneBlock.StoneType, WoodType, VoxelShape[], T> o, VoxelShape[] shapes, TagKey<?>... tags) {
        Map<Pair<StoneBlock.StoneType, WoodType>, Supplier<T>> map = new HashMap<>();
        BlockFamilies.getAllFamilies().filter(f -> f.getRecipeGroupPrefix().orElse("").equals("wooden")).forEach(family -> {
            ResourceLocation woodLocation = BuiltInRegistries.BLOCK.getKey(family.getBaseBlock());
            if(woodLocation.getNamespace().equals("minecraft")) {
                String woodPath = woodLocation.withPath(s -> s.replace("_planks", "")).getPath();
                WoodType.values().filter(t -> t.name().equals(woodPath)).findFirst().ifPresent(woodType -> {
                    for(StoneBlock.StoneType stoneType : StoneBlock.StoneType.values()) {
                        if(polished == stoneType.isPolished()) {
                            map.put(Pair.of(stoneType, woodType), BLOCKS.register(woodPath+"_"+stoneType.getPath()+"_"+path, () -> o.apply(stoneType.getBase(), stoneType, woodType, shapes), tags));
                        }
                    }
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

    @FunctionalInterface
    public interface PentaFunction<P1, P2, P3, P4, P5, R> {
        R apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5);
    }

    @FunctionalInterface
    public interface QuadFunction<P1, P2, P3, P4, R> {
        R apply(P1 p1, P2 p2, P3 p3, P4 p4);
    }

    @FunctionalInterface
    public interface TriFunction<P1, P2, P3, R> {
        R apply(P1 p1, P2 p2, P3 p3);
    }



}
