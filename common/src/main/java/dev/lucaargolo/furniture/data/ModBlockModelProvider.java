package dev.lucaargolo.furniture.data;

import com.google.gson.JsonElement;
import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.block.*;
import dev.lucaargolo.furniture.mixin.BlockModelGeneratorsAccessor;
import dev.lucaargolo.furniture.mixin.TextureSlotAccessor;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.blockstates.*;
import net.minecraft.data.models.model.ModelTemplate;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.Locale;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ModBlockModelProvider {

    private static final TextureSlot LOG = TextureSlotAccessor.invokeCreate("log");
    private static final TextureSlot PLANKS = TextureSlotAccessor.invokeCreate("planks");
    private static final TextureSlot DOORS = TextureSlotAccessor.invokeCreate("doors");
    private static final TextureSlot STONE = TextureSlotAccessor.invokeCreate("stone");
    private static final TextureSlot METAL = TextureSlotAccessor.invokeCreate("metal");

    public static void generate(BlockModelGenerators generators) {
        Consumer<BlockStateGenerator> blockStateOutput = ((BlockModelGeneratorsAccessor) generators).getBlockStateOutput();
        BiConsumer<ResourceLocation, Supplier<JsonElement>> modelOutput = ((BlockModelGeneratorsAccessor) generators).getModelOutput();
        ModBlocks.BLOCKS.forEach((entry) -> {
            Block block = entry.get();
            switch (block) {
                case TableBlock furniture -> {
                    ResourceLocation centerPath = FurnitureMod.id("block/" + entry.path() + "_center");
                    ResourceLocation feetPath = FurnitureMod.id("block/" + entry.path() + "_feet");
                    ResourceLocation itemPath = FurnitureMod.id("item/" + entry.path());

                    ModelTemplate centerTemplate = new ModelTemplate(Optional.of(FurnitureMod.id(centerPath.getPath().replace(furniture.getWood().name() + "_", ""))), Optional.empty(), PLANKS, TextureSlot.PARTICLE);
                    ModelTemplate feetTemplate = new ModelTemplate(Optional.of(FurnitureMod.id(feetPath.getPath().replace(furniture.getWood().name() + "_", ""))), Optional.empty(), LOG, TextureSlot.PARTICLE);
                    ModelTemplate itemTemplate = new ModelTemplate(Optional.of(FurnitureMod.id(itemPath.getPath().replace(furniture.getWood().name() + "_", ""))), Optional.empty(), LOG, PLANKS, TextureSlot.PARTICLE);

                    TextureMapping mapping = new TextureMapping();
                    mapping.put(LOG, DataHelper.getWoodLog(furniture.getWood()));
                    mapping.put(PLANKS, DataHelper.getWoodPlanks(furniture.getWood()));
                    mapping.put(TextureSlot.PARTICLE, DataHelper.getWoodLog(furniture.getWood()));

                    centerTemplate.create(centerPath, mapping, modelOutput);
                    feetTemplate.create(feetPath, mapping, modelOutput);
                    itemTemplate.create(itemPath, mapping, modelOutput);

                    MultiPartGenerator tableSupplier = MultiPartGenerator.multiPart(furniture);
                    addDirectionPart(tableSupplier, centerPath, VariantProperties.Rotation.R0, null, null, null, null);
                    addDirectionPart(tableSupplier, feetPath, VariantProperties.Rotation.R0, false, false, null, null);
                    addDirectionPart(tableSupplier, feetPath, VariantProperties.Rotation.R90, null, false, false, null);
                    addDirectionPart(tableSupplier, feetPath, VariantProperties.Rotation.R180, null, null, false, false);
                    addDirectionPart(tableSupplier, feetPath, VariantProperties.Rotation.R270, false, null, null, false);
                    blockStateOutput.accept(tableSupplier);
                }
                case KitchenCounterBlock furniture -> {
                    ResourceLocation defaultPath = FurnitureMod.id("block/" + entry.path());
                    ResourceLocation innerPath = FurnitureMod.id("block/" + entry.path() + "_inner");
                    ResourceLocation outerPath = FurnitureMod.id("block/" + entry.path() + "_outer");

                    ModelTemplate defaultTemplate = new ModelTemplate(Optional.of(FurnitureMod.id(defaultPath.getPath().replace(furniture.getStone().getPath() + "_", "").replace(furniture.getWood().name() + "_", ""))), Optional.empty(), STONE, PLANKS, DOORS, TextureSlot.PARTICLE);
                    ModelTemplate innerTemplate = new ModelTemplate(Optional.of(FurnitureMod.id(innerPath.getPath().replace(furniture.getStone().getPath() + "_", "").replace(furniture.getWood().name() + "_", ""))), Optional.empty(), STONE, PLANKS, TextureSlot.PARTICLE);
                    ModelTemplate outerTemplate = new ModelTemplate(Optional.of(FurnitureMod.id(outerPath.getPath().replace(furniture.getStone().getPath() + "_", "").replace(furniture.getWood().name() + "_", ""))), Optional.empty(), STONE, PLANKS, DOORS, TextureSlot.PARTICLE);

                    TextureMapping mapping = new TextureMapping();
                    mapping.put(STONE, DataHelper.getStone(furniture.getStone()));
                    mapping.put(PLANKS, DataHelper.getWoodPlanks(furniture.getWood()));
                    mapping.put(DOORS, DataHelper.getWoodDoors(furniture.getWood()));
                    mapping.put(TextureSlot.PARTICLE, DataHelper.getStone(furniture.getStone()));

                    defaultTemplate.create(defaultPath, mapping, modelOutput);
                    innerTemplate.create(innerPath, mapping, modelOutput);
                    outerTemplate.create(outerPath, mapping, modelOutput);

                    MultiVariantGenerator counterSupplier = MultiVariantGenerator.multiVariant(furniture);
                    PropertyDispatch.C3<Boolean, Boolean, Boolean> dispatch = PropertyDispatch.properties(KitchenCounterBlock.EAST, KitchenCounterBlock.WEST, KitchenCounterBlock.OUTER);
                    for (int i = 0; i < 1 << 3; i++) {
                        boolean east = (i & (1)) != 0;
                        boolean west = (i & (1 << 1)) != 0;
                        boolean outer = (i & (1 << 2)) != 0;

                        Variant defaulVariant = Variant.variant().with(VariantProperties.MODEL, defaultPath);
                        Variant innerVariant = Variant.variant().with(VariantProperties.MODEL, innerPath);
                        Variant outerVariant = Variant.variant().with(VariantProperties.MODEL, outerPath);

                        Variant variant = (east && !west) || (!east && west) ? outer ? outerVariant : innerVariant : defaulVariant;
                        if ((east && !west && outer) || (!east && west && !outer)) {
                            variant = variant.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270);
                        }

                        dispatch.select(east, west, outer, variant);
                    }
                    counterSupplier.with(dispatch);
                    blockStateOutput.accept(counterSupplier);
                }
                case MetalWoodFurnitureSeatBlock furniture -> {
                    ModelTemplate template = new ModelTemplate(Optional.of(FurnitureMod.id("block/" + entry.path().replace(furniture.getWood().name() + "_", ""))), Optional.empty(), PLANKS, METAL, TextureSlot.PARTICLE);
                    TextureMapping mapping = new TextureMapping();
                    mapping.put(PLANKS, DataHelper.getWoodPlanks(furniture.getWood()));
                    mapping.put(METAL, DataHelper.getMetal(furniture.getMetal(), furniture.getAge()));
                    mapping.put(TextureSlot.PARTICLE, DataHelper.getWoodLog(furniture.getWood()));
                    createTrivialBlock(generators, block, mapping, template);
                }
                case WoodBlock furniture -> {
                    ModelTemplate template = new ModelTemplate(Optional.of(FurnitureMod.id("block/" + entry.path().replace(furniture.getWood().name() + "_", ""))), Optional.empty(), LOG, PLANKS, TextureSlot.PARTICLE);
                    TextureMapping mapping = new TextureMapping();
                    mapping.put(LOG, DataHelper.getWoodLog(furniture.getWood()));
                    mapping.put(PLANKS, DataHelper.getWoodPlanks(furniture.getWood()));
                    mapping.put(TextureSlot.PARTICLE, DataHelper.getWoodLog(furniture.getWood()));
                    createTrivialBlock(generators, block, mapping, template);
                }
                case MetalBlock furniture -> {
                    String path = entry.path()
                            .replace(furniture.getMetal().name().toLowerCase(Locale.US) + "_", "")
                            .replace(furniture.getAge().getSerializedName() + "_", "")
                            .replace("waxed_", "");
                    ModelTemplate template = new ModelTemplate(Optional.of(FurnitureMod.id("block/" + path)), Optional.empty(), METAL, TextureSlot.PARTICLE);
                    TextureMapping mapping = new TextureMapping();
                    mapping.put(METAL, DataHelper.getMetal(furniture.getMetal(), furniture.getAge()));
                    mapping.put(TextureSlot.PARTICLE, DataHelper.getMetal(furniture.getMetal(), furniture.getAge()));
                    createTrivialBlock(generators, block, mapping, template);
                }
                default -> createNonTemplateModelBlock(generators, block);
            }
        });
    }

    private static void addDirectionPart(MultiPartGenerator generator, ResourceLocation modelPath, VariantProperties.Rotation rotationY, Boolean north, Boolean east, Boolean south, Boolean west) {
        Variant variant = Variant.variant().with(VariantProperties.MODEL, modelPath);
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

    private static void createTrivialBlock(BlockModelGenerators generators, Block block, TextureMapping mapping, ModelTemplate template) {
        ((BlockModelGeneratorsAccessor) generators).invokeCreateTrivialBlock(block, mapping, template);
    }

    private static void createNonTemplateModelBlock(BlockModelGenerators generators, Block block) {
        ((BlockModelGeneratorsAccessor) generators).invokeCreateNonTemplateModelBlock(block);
    }

}
