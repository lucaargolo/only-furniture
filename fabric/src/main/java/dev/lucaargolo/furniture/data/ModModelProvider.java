package dev.lucaargolo.furniture.data;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.block.MetalBlock;
import dev.lucaargolo.furniture.block.ModBlocks;
import dev.lucaargolo.furniture.block.TableBlock;
import dev.lucaargolo.furniture.block.WoodBlock;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.blockstates.Condition;
import net.minecraft.data.models.blockstates.MultiPartGenerator;
import net.minecraft.data.models.blockstates.Variant;
import net.minecraft.data.models.blockstates.VariantProperties;
import net.minecraft.data.models.model.ModelTemplate;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Optional;

public class ModModelProvider extends FabricModelProvider {

    private static final TextureSlot LOG = TextureSlot.create("log");
    private static final TextureSlot PLANKS = TextureSlot.create("planks");
    private static final TextureSlot METAL = TextureSlot.create("metal");

    public ModModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators blockModelGenerators) {
        ModBlocks.BLOCKS.forEach((entry) -> {
            Block block = entry.get();
            if(block instanceof TableBlock furniture) {
                ResourceLocation centerPath = FurnitureMod.id("block/"+entry.path()+"_center");
                ResourceLocation feetPath = FurnitureMod.id("block/"+entry.path()+"_feet");
                ResourceLocation itemPath = FurnitureMod.id("item/"+entry.path());

                ModelTemplate centerTemplate = new ModelTemplate(Optional.of(FurnitureMod.id(centerPath.getPath().replace(furniture.getWood().name()+"_", ""))), Optional.empty(), PLANKS, TextureSlot.PARTICLE);
                ModelTemplate feetTemplate = new ModelTemplate(Optional.of(FurnitureMod.id(feetPath.getPath().replace(furniture.getWood().name()+"_", ""))), Optional.empty(), LOG, TextureSlot.PARTICLE);
                ModelTemplate itemTemplate = new ModelTemplate(Optional.of(FurnitureMod.id(itemPath.getPath().replace(furniture.getWood().name()+"_", ""))), Optional.empty(), LOG, PLANKS, TextureSlot.PARTICLE);

                TextureMapping mapping = new TextureMapping();
                mapping.put(LOG, DataHelper.getWoodLog(furniture.getWood()));
                mapping.put(PLANKS, DataHelper.getWoodPlanks(furniture.getWood()));
                mapping.put(TextureSlot.PARTICLE, DataHelper.getWoodLog(furniture.getWood()));

                centerTemplate.create(centerPath, mapping, blockModelGenerators.modelOutput);
                feetTemplate.create(feetPath, mapping, blockModelGenerators.modelOutput);
                itemTemplate.create(itemPath, mapping, blockModelGenerators.modelOutput);

                MultiPartGenerator tableSupplier = MultiPartGenerator.multiPart(furniture);
                addDirectionPart(tableSupplier, centerPath, VariantProperties.Rotation.R0, null, null, null, null);
                addDirectionPart(tableSupplier, feetPath, VariantProperties.Rotation.R0, false, false, null, null);
                addDirectionPart(tableSupplier, feetPath, VariantProperties.Rotation.R90, null, false, false, null);
                addDirectionPart(tableSupplier, feetPath, VariantProperties.Rotation.R180, null, null, false, false);
                addDirectionPart(tableSupplier, feetPath, VariantProperties.Rotation.R270, false, null, null, false);
                blockModelGenerators.blockStateOutput.accept(tableSupplier);


            }else if(block instanceof WoodBlock furniture) {
                ModelTemplate template = new ModelTemplate(Optional.of(FurnitureMod.id("block/"+entry.path().replace(furniture.getWood().name()+"_", ""))), Optional.empty(), LOG, PLANKS, TextureSlot.PARTICLE);
                TextureMapping mapping = new TextureMapping();
                mapping.put(LOG, DataHelper.getWoodLog(furniture.getWood()));
                mapping.put(PLANKS, DataHelper.getWoodPlanks(furniture.getWood()));
                mapping.put(TextureSlot.PARTICLE, DataHelper.getWoodLog(furniture.getWood()));
                blockModelGenerators.createTrivialBlock(block, mapping, template);
            }else if(block instanceof MetalBlock furniture) {
                String path = entry.path()
                        .replace(furniture.getMetal().name().toLowerCase(Locale.US)+"_", "")
                        .replace(furniture.getAge().getSerializedName()+"_", "")
                        .replace("waxed_", "");
                ModelTemplate template = new ModelTemplate(Optional.of(FurnitureMod.id("block/"+path)), Optional.empty(), METAL, TextureSlot.PARTICLE);
                TextureMapping mapping = new TextureMapping();
                mapping.put(METAL, DataHelper.getMetal(furniture.getMetal(), furniture.getAge()));
                mapping.put(TextureSlot.PARTICLE, DataHelper.getMetal(furniture.getMetal(), furniture.getAge()));
                blockModelGenerators.createTrivialBlock(block, mapping, template);
            }else {
                blockModelGenerators.createNonTemplateModelBlock(block);
            }
        });
    }

    @Override
    public void generateItemModels(ItemModelGenerators itemModelGenerators) {

    }

    private void addDirectionPart(MultiPartGenerator generator, ResourceLocation modelPath, VariantProperties.Rotation rotationY, Boolean north, Boolean east, Boolean south, Boolean west) {
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

    @Override
    public @NotNull String getName() {
        return "FurnitureModModelProvider";
    }

}
