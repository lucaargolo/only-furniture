package dev.lucaargolo.furniture.data;

import com.google.gson.JsonElement;
import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.block.FurnitureBlock;
import dev.lucaargolo.furniture.block.ModBlocks;
import dev.lucaargolo.furniture.block.base.ColorBlock;
import dev.lucaargolo.furniture.block.base.MetalBlock;
import dev.lucaargolo.furniture.block.base.StoneBlock;
import dev.lucaargolo.furniture.block.base.WoodBlock;
import dev.lucaargolo.furniture.block.impl.*;
import dev.lucaargolo.furniture.mixin.BlockModelGeneratorsAccessor;
import dev.lucaargolo.furniture.mixin.TextureSlotAccessor;
import dev.lucaargolo.furniture.registry.ModBlockRegistry;
import net.minecraft.core.Direction;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.blockstates.*;
import net.minecraft.data.models.model.ModelTemplate;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.WoodType;
import org.jetbrains.annotations.Nullable;

import java.util.*;
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
    private static final TextureSlot PILLOW = TextureSlotAccessor.invokeCreate("pillow");

    private static final HashSet<ResourceLocation> generatedModels = new HashSet<>();

    public static void generate(BlockModelGenerators generators) {
        ModBlocks.REGISTRY.getEntries().forEach((entry) -> {
            switch (entry.get()) {
                case TableBlock table -> createTableBlockState(generators, entry, table.isSimple());
                case KitchenCounterBlock counter -> createCounterBlockState(generators, entry, counter.getWood(), counter.getStone());
                case KitchenSinkBlock ignored -> createSinkBlockState(generators, entry);
                case SofaBlock ignored -> createSofaBlockState(generators, entry);
                case FridgeBlock ignored -> createFridgeBlockState(generators, entry);
                default -> createBaseBlockState(generators, entry);
            }
        });
    }

    private static ResourceLocation computeModel(BlockModelGenerators generators, ModBlockRegistry.BlockEntry<?> entry, String prefix) {
        return computeModel(generators, entry, null, null, prefix, "", new ArrayList<>());
    }

    private static ResourceLocation computeModel(BlockModelGenerators generators, ModBlockRegistry.BlockEntry<?> entry, String prefix, TextureSlot... slots) {
        return computeModel(generators, entry, null, null, prefix, "", List.of(slots));
    }

    private static ResourceLocation computeModel(BlockModelGenerators generators, ModBlockRegistry.BlockEntry<?> entry, String prefix, String suffix) {
        return computeModel(generators, entry, null, null, prefix, suffix, new ArrayList<>());
    }

    private static ResourceLocation computeModel(BlockModelGenerators generators, ModBlockRegistry.BlockEntry<?> entry, String prefix, String suffix, TextureSlot... slots) {
        return computeModel(generators, entry, null, null, prefix, suffix, List.of(slots));
    }

    private static ResourceLocation computeModel(BlockModelGenerators generators, ModBlockRegistry.BlockEntry<?> entry, ResourceLocation model, ResourceLocation parent, TextureSlot... slots) {
        return computeModel(generators, entry, model, parent, "", "", List.of(slots));
    }

    private static ResourceLocation computeModel(BlockModelGenerators generators, ModBlockRegistry.BlockEntry<?> entry, @Nullable ResourceLocation model, @Nullable ResourceLocation parent, String prefix, String suffix, List<TextureSlot> slots) {
        if(generatedModels.contains(model)) {
           return model;
        }

        BiConsumer<ResourceLocation, Supplier<JsonElement>> modelOutput = ((BlockModelGeneratorsAccessor) generators).getModelOutput();

        TextureMapping mapping = new TextureMapping();
        tryAddSlot(slots, PARTICLE);

        String path = entry.path();
        Block block = entry.get();
        if (block instanceof WoodBlock woodBlock) {
            WoodType wood = woodBlock.getWood();
            path = path.replace(wood.name() + "_", "");
            tryAddSlot(slots, LOG);
            tryAddSlot(slots, PLANKS);
            if(slots.contains(DOORS))
                mapping.put(DOORS, DataHelper.getDoors(wood));
            mapping.put(LOG, DataHelper.getLog(wood));
            mapping.put(slots.contains(BASE) ? BASE : PLANKS, DataHelper.getPlanks(wood));
            mapping.put(PARTICLE, DataHelper.getLog(wood));
            if(block instanceof WoodBlock.LeafBlock) {
                tryAddSlot(slots, LEAVES);
                mapping.put(slots.contains(BASE) ? BASE : LEAVES, DataHelper.getLeaves(wood));
                mapping.put(PARTICLE, DataHelper.getLeaves(wood));
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

        if(block instanceof ColorBlock colorBlock) {
            DyeColor color = colorBlock.getColor();
            path = path.replace(color.getSerializedName() + "_", "");
            if(slots.contains(PILLOW))
                mapping.put(PILLOW, DataHelper.getPillow(color));
        }

        model = model == null ? FurnitureMod.id(prefix+entry.path()+suffix) : model;
        parent = parent == null ? FurnitureMod.id(prefix+path+suffix) : parent;
        if(slots.isEmpty() || (slots.size() == 1 && slots.contains(PARTICLE))) {
            return parent;
        }else{
            ModelTemplate template = new ModelTemplate(Optional.of(parent), Optional.empty(), slots.toArray(new TextureSlot[0]));
            template.create(model, mapping, modelOutput);
            generatedModels.add(model);
            return model;
        }
    }

    private static void createBaseBlockState(BlockModelGenerators generators, ModBlockRegistry.BlockEntry<?> entry) {
        Consumer<BlockStateGenerator> blockStateOutput = ((BlockModelGeneratorsAccessor) generators).getBlockStateOutput();
        ResourceLocation path = computeModel(generators, entry, "block/");

        MultiVariantGenerator generator;
        if(entry.get() instanceof FurnitureBlock furniture && furniture.isWallBlock()) {
            generator = MultiVariantGenerator.multiVariant(entry.get());
            PropertyDispatch.C1<Direction> dispatch = PropertyDispatch.property(FurnitureBlock.FACING);
            dispatch.select(Direction.NORTH, Variant.variant().with(VariantProperties.MODEL, path));
            dispatch.select(Direction.EAST, Variant.variant().with(VariantProperties.MODEL, path).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90));
            dispatch.select(Direction.SOUTH, Variant.variant().with(VariantProperties.MODEL, path).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180));
            dispatch.select(Direction.WEST, Variant.variant().with(VariantProperties.MODEL, path).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270));
            generator.with(dispatch);
        }else{
            generator = MultiVariantGenerator.multiVariant(entry.get(), Variant.variant().with(VariantProperties.MODEL, path));
        }

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
        addDirectionPart(generator, topPath, false, VariantProperties.Rotation.R0, null, null, null, null);

        if(!simple) {
            ResourceLocation sidePath = computeModel(generators, entry, "block/", "_side", PARTICLE, PLANKS);
            addDirectionPart(generator, sidePath, true, VariantProperties.Rotation.R0, null, true, null, null);
            addDirectionPart(generator, sidePath, true, VariantProperties.Rotation.R90, null, null, true, null);
            addDirectionPart(generator, sidePath, true, VariantProperties.Rotation.R180, null, null, null, true);
            addDirectionPart(generator, sidePath, true, VariantProperties.Rotation.R270, true, null, null, null);
        }

        ResourceLocation footPath = computeModel(generators, entry, "block/", "_foot", PARTICLE, LOG);
        addDirectionPart(generator, footPath, true, VariantProperties.Rotation.R0, false, false, null, null);
        addDirectionPart(generator, footPath, true, VariantProperties.Rotation.R90, null, false, false, null);
        addDirectionPart(generator, footPath, true, VariantProperties.Rotation.R180, null, null, false, false);
        addDirectionPart(generator, footPath, true, VariantProperties.Rotation.R270, false, null, null, false);

        computeModel(generators, entry, "item/");
        blockStateOutput.accept(generator);
    }

    private static void createCounterBlockState(BlockModelGenerators generators, ModBlockRegistry.BlockEntry<?> entry, WoodType woodType, StoneBlock.StoneType stoneType) {
        Consumer<BlockStateGenerator> blockStateOutput = ((BlockModelGeneratorsAccessor) generators).getBlockStateOutput();

        ResourceLocation basePath = computeModel(generators, entry, FurnitureMod.id("block/kitchen_counter/"+woodType.name()+"_base"), FurnitureMod.id("block/kitchen_counter/base"), PARTICLE, PLANKS, DOORS);
        ResourceLocation baseHollowOpenPath = computeModel(generators, entry, FurnitureMod.id("block/kitchen_counter/"+woodType.name()+"_base_hollow_open"), FurnitureMod.id("block/kitchen_counter/base_hollow_open"), PARTICLE, PLANKS, DOORS);
        ResourceLocation baseOpenPath = computeModel(generators, entry, FurnitureMod.id("block/kitchen_counter/"+woodType.name()+"_base_open"), FurnitureMod.id("block/kitchen_counter/base_open"), PARTICLE, PLANKS, DOORS);
        ResourceLocation baseInnerPath = computeModel(generators, entry, FurnitureMod.id("block/kitchen_counter/"+woodType.name()+"_base_inner"), FurnitureMod.id("block/kitchen_counter/base_inner"), PARTICLE, PLANKS);
        ResourceLocation baseOuterPath = computeModel(generators, entry, FurnitureMod.id("block/kitchen_counter/"+woodType.name()+"_base_outer"), FurnitureMod.id("block/kitchen_counter/base_outer"), PARTICLE, PLANKS, DOORS);

        ResourceLocation topPath = computeModel(generators, entry, FurnitureMod.id("block/kitchen_counter/"+stoneType.getPath()+"_top"), FurnitureMod.id("block/kitchen_counter/top"), PARTICLE, STONE);
        ResourceLocation topHollowPath = computeModel(generators, entry, FurnitureMod.id("block/kitchen_counter/"+stoneType.getPath()+"_top_hollow"), FurnitureMod.id("block/kitchen_counter/top_hollow"), PARTICLE, STONE);
        ResourceLocation topInnerPath = computeModel(generators, entry, FurnitureMod.id("block/kitchen_counter/"+stoneType.getPath()+"_top_inner"), FurnitureMod.id("block/kitchen_counter/top_inner"), PARTICLE, STONE);
        ResourceLocation topOuterPath = computeModel(generators, entry, FurnitureMod.id("block/kitchen_counter/"+stoneType.getPath()+"_top_outer"), FurnitureMod.id("block/kitchen_counter/top_outer"), PARTICLE, STONE);

        MultiPartGenerator generator = MultiPartGenerator.multiPart(entry.get());
        //TODO: Optimize this so it doesnt use generate condition for every possibility.
        for (int i = 0; i < 1 << 5; i++) {
            boolean north = (i & (1)) != 0;
            boolean south = (i & (1 << 1)) != 0;
            boolean outer = (i & (1 << 2)) != 0;
            boolean hollow = (i & (1 << 3)) != 0;
            boolean open = (i & (1 << 4)) != 0;

            boolean isCorner = (north && !south) || (!north && south);

            Variant baseVariant  = Variant.variant().with(VariantProperties.MODEL, basePath);
            Variant baseHollowOpenVariant = Variant.variant().with(VariantProperties.MODEL, baseHollowOpenPath);
            Variant baseOpenVariant = Variant.variant().with(VariantProperties.MODEL, baseOpenPath);
            Variant baseInnerVariant = Variant.variant().with(VariantProperties.MODEL, baseInnerPath);
            Variant baseOuterVariant = Variant.variant().with(VariantProperties.MODEL, baseOuterPath);

            Variant topVariant = Variant.variant().with(VariantProperties.MODEL, topPath);
            Variant topHollowVariant = Variant.variant().with(VariantProperties.MODEL, topHollowPath);
            Variant topInnerVariant = Variant.variant().with(VariantProperties.MODEL, topInnerPath);
            Variant topOuterVariant = Variant.variant().with(VariantProperties.MODEL, topOuterPath);

            Variant top = isCorner ? outer ? topOuterVariant : topInnerVariant : hollow ? topHollowVariant : topVariant;
            Variant base = isCorner ? outer ? baseOuterVariant : baseInnerVariant : hollow && open ? baseHollowOpenVariant : open ? baseOpenVariant : baseVariant;

            if ((north && !south && outer) || (!north && south && !outer)) {
                top = top.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270);
                base = base.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270);
            }

            Condition.TerminalCondition condition = Condition.condition();
            condition.term(KitchenCounterBlock.NORTH, north);
            condition.term(KitchenCounterBlock.SOUTH, south);
            condition.term(KitchenCounterBlock.OUTER, outer);
            condition.term(KitchenCounterBlock.HOLLOW, hollow);
            condition.term(KitchenCounterBlock.OPEN, open);

            generator.with(condition, top);
            generator.with(condition, base);
        }

        computeModel(generators, entry, "item/", PARTICLE, PLANKS, DOORS, STONE);
        blockStateOutput.accept(generator);
    }

    private static void createSofaBlockState(BlockModelGenerators generators, ModBlockRegistry.BlockEntry<?> entry) {
        Consumer<BlockStateGenerator> blockStateOutput = ((BlockModelGeneratorsAccessor) generators).getBlockStateOutput();

        ResourceLocation defaultPath = computeModel(generators, entry, "block/", "", PILLOW);
        ResourceLocation centerPath = computeModel(generators, entry, "block/", "_center", PILLOW);
        ResourceLocation rightPath = computeModel(generators, entry, "block/", "_right", PILLOW);
        ResourceLocation leftPath = computeModel(generators, entry, "block/", "_left", PILLOW);
        ResourceLocation innerPath = computeModel(generators, entry, "block/", "_inner", PILLOW);
        ResourceLocation outerPath = computeModel(generators, entry, "block/", "_outer", PILLOW);

        MultiVariantGenerator generator = MultiVariantGenerator.multiVariant(entry.get());
        PropertyDispatch.C5<Boolean, Boolean, Boolean, Boolean, Boolean> dispatch = PropertyDispatch.properties(SofaBlock.NORTH, SofaBlock.EAST, SofaBlock.SOUTH, SofaBlock.WEST, SofaBlock.OUTER);
        for (int i = 0; i < 1 << 5; i++) {
            boolean north = (i & (1)) != 0;
            boolean east = (i & (1 << 1)) != 0;
            boolean south = (i & (1 << 2)) != 0;
            boolean west = (i & (1 << 3)) != 0;
            boolean outer = (i & (1 << 4)) != 0;

            Variant defaultVariant = Variant.variant().with(VariantProperties.MODEL, defaultPath);
            Variant centerVariant = Variant.variant().with(VariantProperties.MODEL, centerPath);
            Variant rightVariant = Variant.variant().with(VariantProperties.MODEL, rightPath);
            Variant leftVariant = Variant.variant().with(VariantProperties.MODEL, leftPath);
            Variant normalVariant = (east && west) ? centerVariant : east ? rightVariant : west ? leftVariant : defaultVariant;

            Variant innerVariant = Variant.variant().with(VariantProperties.MODEL, innerPath);
            Variant outerVariant = Variant.variant().with(VariantProperties.MODEL, outerPath);
            Variant cornerVariant = outer ? outerVariant : innerVariant;

            Variant variant = (north && !south) || (!north && south) ? cornerVariant : normalVariant;
            if ((north && !south && outer) || (!north && south && !outer)) {
                variant = variant.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270);
            }

            dispatch.select(north, east, south, west, outer, variant);
        }
        generator.with(dispatch);
        blockStateOutput.accept(generator);
    }

    private static void createFridgeBlockState(BlockModelGenerators generators, ModBlockRegistry.BlockEntry<? extends Block> entry) {
        Consumer<BlockStateGenerator> blockStateOutput = ((BlockModelGeneratorsAccessor) generators).getBlockStateOutput();

        ResourceLocation defaultPath = computeModel(generators, entry, "block/", "");
        ResourceLocation openPath = computeModel(generators, entry, "block/", "_open");
        ResourceLocation openTopPath = computeModel(generators, entry, "block/", "_open_top");
        ResourceLocation openBottomPath = computeModel(generators, entry, "block/", "_open_bottom");

        MultiVariantGenerator generator = MultiVariantGenerator.multiVariant(entry.get());
        PropertyDispatch.C2<Boolean, Boolean> dispatch = PropertyDispatch.properties(FridgeBlock.TOP_OPEN, FridgeBlock.BOTTOM_OPEN);
        for (int i = 0; i < 1 << 2; i++) {
            boolean top = (i & (1)) != 0;
            boolean bottom = (i & (1 << 1)) != 0;

            Variant defaultVariant = Variant.variant().with(VariantProperties.MODEL, defaultPath);
            Variant openVariant = Variant.variant().with(VariantProperties.MODEL, openPath);
            Variant openTopVariant = Variant.variant().with(VariantProperties.MODEL, openTopPath);
            Variant openBottomVariant = Variant.variant().with(VariantProperties.MODEL, openBottomPath);

            Variant variant = top && bottom ? openVariant : top ? openTopVariant : bottom ? openBottomVariant : defaultVariant;
            dispatch.select(top, bottom, variant);
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
