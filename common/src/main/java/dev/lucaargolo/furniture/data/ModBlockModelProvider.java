package dev.lucaargolo.furniture.data;

import com.google.gson.JsonElement;
import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.block.ModBlocks;
import dev.lucaargolo.furniture.block.base.MetalBlock;
import dev.lucaargolo.furniture.block.base.StoneBlock;
import dev.lucaargolo.furniture.block.base.WoodBlock;
import dev.lucaargolo.furniture.block.impl.KitchenCounterBlock;
import dev.lucaargolo.furniture.block.impl.KitchenSinkBlock;
import dev.lucaargolo.furniture.block.impl.TableBlock;
import dev.lucaargolo.furniture.mixin.BlockModelGeneratorsAccessor;
import dev.lucaargolo.furniture.mixin.TextureSlotAccessor;
import dev.lucaargolo.furniture.registry.ModBlockRegistry;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.blockstates.*;
import net.minecraft.data.models.model.ModelTemplate;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.WoodType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ModBlockModelProvider {

    private static final TextureSlot PARTICLE = TextureSlot.PARTICLE;
    private static final TextureSlot PLANKS = TextureSlotAccessor.invokeCreate("planks");
    private static final TextureSlot LOG = TextureSlotAccessor.invokeCreate("log");
    private static final TextureSlot DOORS = TextureSlotAccessor.invokeCreate("doors");
    private static final TextureSlot STONE = TextureSlotAccessor.invokeCreate("stone");
    private static final TextureSlot METAL = TextureSlotAccessor.invokeCreate("metal");
    private static final TextureSlot BASE = TextureSlotAccessor.invokeCreate("base");
    private static final TextureSlot LEAVES = TextureSlotAccessor.invokeCreate("leaves");

    public static void generate(BlockModelGenerators generators) {
        ModBlocks.REGISTRY.forEach((entry) -> {
            switch (entry.get()) {
                case TableBlock table -> createTableBlockState(generators, entry, table.isSimple());
                case KitchenCounterBlock ignored -> createCounterBlockState(generators, entry);
                case KitchenSinkBlock ignored ->createSinkBlockState(generators, entry);
                default -> createBaseBlockState(generators, entry);
            }
        });
    }

    private static ResourceLocation computeModel(BlockModelGenerators generators, ModBlockRegistry.BlockEntry<?> entry, String prefix) {
        return computeModel(generators, entry, prefix, "", new ArrayList<>());
    }

    private static ResourceLocation computeModel(BlockModelGenerators generators, ModBlockRegistry.BlockEntry<?> entry, String prefix, String suffix) {
        return computeModel(generators, entry, prefix, suffix, new ArrayList<>());
    }

    private static ResourceLocation computeModel(BlockModelGenerators generators, ModBlockRegistry.BlockEntry<?> entry, String prefix, String suffix, TextureSlot... slots) {
        return computeModel(generators, entry, prefix, suffix, List.of(slots));
    }

    private static ResourceLocation computeModel(BlockModelGenerators generators, ModBlockRegistry.BlockEntry<?> entry, String prefix, String suffix, List<TextureSlot> slots) {
        BiConsumer<ResourceLocation, Supplier<JsonElement>> modelOutput = ((BlockModelGeneratorsAccessor) generators).getModelOutput();

        Block block = entry.get();
        String path = entry.path();

        TextureMapping mapping = new TextureMapping();
        tryAddSlot(slots, PARTICLE);

        if (block instanceof WoodBlock woodBlock) {
            WoodType wood = woodBlock.getWood();
            path = path.replace(wood.name() + "_", "");
            tryAddSlot(slots, LOG);
            tryAddSlot(slots, PLANKS);
            if(slots.contains(DOORS))
                mapping.put(DOORS, DataHelper.getWoodDoors(wood));
            mapping.put(LOG, DataHelper.getWoodLog(wood));
            mapping.put(slots.contains(BASE) ? BASE : PLANKS, DataHelper.getWoodPlanks(wood));
            mapping.put(PARTICLE, DataHelper.getWoodLog(wood));
            if(block instanceof WoodBlock.LeafBlock) {
                tryAddSlot(slots, LEAVES);
                mapping.put(slots.contains(BASE) ? BASE : LEAVES, DataHelper.getWoodLeaves(wood));
                mapping.put(PARTICLE, DataHelper.getWoodLeaves(wood));
            }
        }

        if (block instanceof StoneBlock stoneBlock) {
            StoneBlock.StoneType stone = stoneBlock.getStone();
            path = path.replace(stone.getPath() + "_", "");
            tryAddSlot(slots, STONE);
            mapping.put(slots.contains(BASE) ? BASE : STONE, DataHelper.getStone(stone));
            mapping.put(PARTICLE, DataHelper.getStone(stone));
        }

        if(block instanceof MetalBlock metalBlock) {
            MetalBlock.MetalType metal = metalBlock.getMetal();
            WeatheringCopper.WeatherState age = metalBlock.getAge();
            path = path.replace(metal.name().toLowerCase(Locale.US) + "_", "");
            path = path.replace(age.getSerializedName() + "_", "");
            path = path.replace("waxed_", "");
            tryAddSlot(slots, METAL);
            mapping.put(slots.contains(BASE) ? BASE : METAL, DataHelper.getMetal(metal, age));
            mapping.put(PARTICLE, DataHelper.getMetal(metal, age));
        }

        ResourceLocation parent = FurnitureMod.id(prefix+path+suffix);
        ResourceLocation child = FurnitureMod.id(prefix+entry.path()+suffix);

        if(slots.isEmpty() || (slots.size() == 1 && slots.contains(PARTICLE))) {
            return parent;
        }else{
            ModelTemplate template = new ModelTemplate(Optional.of(parent), Optional.empty(), slots.toArray(new TextureSlot[0]));
            template.create(child, mapping, modelOutput);
            return child;
        }

    }

    private static void createBaseBlockState(BlockModelGenerators generators, ModBlockRegistry.BlockEntry<?> entry) {
        Consumer<BlockStateGenerator> blockStateOutput = ((BlockModelGeneratorsAccessor) generators).getBlockStateOutput();
        ResourceLocation path = computeModel(generators, entry, "block/");
        MultiVariantGenerator generator = MultiVariantGenerator.multiVariant(entry.get(), Variant.variant().with(VariantProperties.MODEL, path));
        blockStateOutput.accept(generator);
    }

    private static void createSinkBlockState(BlockModelGenerators generators, ModBlockRegistry.BlockEntry<?> entry) {
        Consumer<BlockStateGenerator> blockStateOutput = ((BlockModelGeneratorsAccessor) generators).getBlockStateOutput();

        ResourceLocation defaultPath = computeModel(generators, entry, "block/", "", PARTICLE, BASE);
        ResourceLocation droppedPath = computeModel(generators, entry, "block/", "_dropped", PARTICLE, BASE);

        Variant defaultVariant = Variant.variant().with(VariantProperties.MODEL, defaultPath);
        Variant droppedVariant = Variant.variant().with(VariantProperties.MODEL, droppedPath);

        MultiVariantGenerator generator = MultiVariantGenerator.multiVariant(entry.get());
        PropertyDispatch.C1<Boolean> dispatch = PropertyDispatch.property(KitchenSinkBlock.DROPPED);
        dispatch.select(false, defaultVariant);
        dispatch.select(true, droppedVariant);
        generator.with(dispatch);
        blockStateOutput.accept(generator);
    }

    private static void createTableBlockState(BlockModelGenerators generators, ModBlockRegistry.BlockEntry<?> entry, boolean simple) {
        Consumer<BlockStateGenerator> blockStateOutput = ((BlockModelGeneratorsAccessor) generators).getBlockStateOutput();

        MultiPartGenerator generator = MultiPartGenerator.multiPart(entry.get());

        ResourceLocation topPath = computeModel(generators, entry, "block/", "_top", PARTICLE, PLANKS);
        if(simple) {
            addDirectionPart(generator, topPath, false, VariantProperties.Rotation.R0, null, null, null, null);
        }else{
            addDirectionPart(generator, topPath, true, VariantProperties.Rotation.R0, false, false, false, false);

            ResourceLocation centerPath = computeModel(generators, entry, "block/", "_top_center", PARTICLE, PLANKS);
            addDirectionPart(generator, centerPath, true, VariantProperties.Rotation.R0, true, true, true, true);

            addDirectionPart(generator, centerPath, true, VariantProperties.Rotation.R0, true, true, true, false);
            addDirectionPart(generator, centerPath, true, VariantProperties.Rotation.R0, false, true, true, true);
            addDirectionPart(generator, centerPath, true, VariantProperties.Rotation.R0, true, false, true, true);
            addDirectionPart(generator, centerPath, true, VariantProperties.Rotation.R0, true, true, false, true);

            addDirectionPart(generator, centerPath, true, VariantProperties.Rotation.R0, true, false, true, false);
            addDirectionPart(generator, centerPath, true, VariantProperties.Rotation.R0, false, true, false, true);

            ResourceLocation cornerPath = computeModel(generators, entry, "block/", "_top_corner", PARTICLE, PLANKS);
            addDirectionPart(generator, cornerPath, true, VariantProperties.Rotation.R0, false, true, true, false);
            addDirectionPart(generator, cornerPath, true, VariantProperties.Rotation.R90, false, false, true, true);
            addDirectionPart(generator, cornerPath, true, VariantProperties.Rotation.R180, true, false, false, true);
            addDirectionPart(generator, cornerPath, true, VariantProperties.Rotation.R270, true, true, false, false);

            ResourceLocation sidePath = computeModel(generators, entry, "block/", "_top_side", PARTICLE, PLANKS);
            addDirectionPart(generator, sidePath, true, VariantProperties.Rotation.R0, false, true, false, false);
            addDirectionPart(generator, sidePath, true, VariantProperties.Rotation.R90, false, false, true, false);
            addDirectionPart(generator, sidePath, true, VariantProperties.Rotation.R180, false, false, false, true);
            addDirectionPart(generator, sidePath, true, VariantProperties.Rotation.R270, true, false, false, false);
        }
        ResourceLocation footPath = computeModel(generators, entry, "block/", "_foot", PARTICLE, LOG);
        addDirectionPart(generator, footPath, true, VariantProperties.Rotation.R0, false, false, null, null);
        addDirectionPart(generator, footPath, true, VariantProperties.Rotation.R90, null, false, false, null);
        addDirectionPart(generator, footPath, true, VariantProperties.Rotation.R180, null, null, false, false);
        addDirectionPart(generator, footPath, true, VariantProperties.Rotation.R270, false, null, null, false);

        computeModel(generators, entry, "item/");
        blockStateOutput.accept(generator);
    }

    private static void createCounterBlockState(BlockModelGenerators generators, ModBlockRegistry.BlockEntry<?> entry) {
        Consumer<BlockStateGenerator> blockStateOutput = ((BlockModelGeneratorsAccessor) generators).getBlockStateOutput();

        ResourceLocation defaultPath = computeModel(generators, entry, "block/", "", PARTICLE, PLANKS, DOORS, STONE);
        ResourceLocation hollowPath = computeModel(generators, entry, "block/", "_hollow", PARTICLE, PLANKS, DOORS, STONE);
        ResourceLocation innerPath = computeModel(generators, entry, "block/", "_inner", PARTICLE, PLANKS, STONE);
        ResourceLocation outerPath = computeModel(generators, entry, "block/", "_outer", PARTICLE, PLANKS, DOORS, STONE);

        MultiVariantGenerator generator = MultiVariantGenerator.multiVariant(entry.get());
        PropertyDispatch.C4<Boolean, Boolean, Boolean, Boolean> dispatch = PropertyDispatch.properties(KitchenCounterBlock.EAST, KitchenCounterBlock.WEST, KitchenCounterBlock.OUTER, KitchenCounterBlock.HOLLOW);
        for (int i = 0; i < 1 << 4; i++) {
            boolean east = (i & (1)) != 0;
            boolean west = (i & (1 << 1)) != 0;
            boolean outer = (i & (1 << 2)) != 0;
            boolean hollow = (i & (1 << 3)) != 0;

            Variant defaultVariant = Variant.variant().with(VariantProperties.MODEL, defaultPath);
            Variant hollowVariant = Variant.variant().with(VariantProperties.MODEL, hollowPath);
            Variant innerVariant = Variant.variant().with(VariantProperties.MODEL, innerPath);
            Variant outerVariant = Variant.variant().with(VariantProperties.MODEL, outerPath);

            Variant variant = (east && !west) || (!east && west) ? outer ? outerVariant : innerVariant : hollow ? hollowVariant : defaultVariant;
            if ((east && !west && outer) || (!east && west && !outer)) {
                variant = variant.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270);
            }

            dispatch.select(east, west, outer, hollow, variant);
        }
        generator.with(dispatch);
        blockStateOutput.accept(generator);
    }

    private static void addDirectionPart(MultiPartGenerator generator, ResourceLocation modelPath, boolean uvLock, VariantProperties.Rotation rotationY, Boolean north, Boolean east, Boolean south, Boolean west) {
        Variant variant = Variant.variant().with(VariantProperties.MODEL, modelPath);
        if(uvLock) {
            variant.with(VariantProperties.UV_LOCK, true);
        }
        if(rotationY != VariantProperties.Rotation.R0) {
            variant.with(VariantProperties.Y_ROT, rotationY);
        }

        Condition.TerminalCondition condition = Condition.condition();
        if (north != null) condition.term(BlockStateProperties.NORTH, north);
        if (east != null) condition.term(BlockStateProperties.EAST, east);
        if (south != null) condition.term(BlockStateProperties.SOUTH, south);
        if (west != null) condition.term(BlockStateProperties.WEST, west);

        if(condition.get().getAsJsonObject().isEmpty()) {
            generator.with(variant);
        }else{
            generator.with(condition, variant);
        }
    }

    private static void tryAddSlot(List<TextureSlot> slots, TextureSlot slot) {
        try {
            slots.add(slot);
        }catch (UnsupportedOperationException ignored) { }
    }

}
