package dev.lucaargolo.furniture.data;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.block.MetalBlock;
import dev.lucaargolo.furniture.block.ModBlocks;
import dev.lucaargolo.furniture.block.WoodBlock;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.model.ModelTemplate;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.world.level.block.Block;
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
            if(block instanceof WoodBlock furniture) {
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

    @Override
    public @NotNull String getName() {
        return "FurnitureModModelProvider";
    }

}
