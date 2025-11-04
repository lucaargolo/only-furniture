package dev.lucaargolo.furniture.data;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.block.*;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.model.generators.*;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.Locale;

public class ModModelProvider extends BlockStateProvider {

    public ModModelProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, FurnitureMod.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        ModBlocks.BLOCKS.forEach((entry) -> {
            Block block = entry.get();
            if(block instanceof TableBlock furniture) {
                String centerPath = "block/"+entry.path()+"_center";
                String feetPath = "block/"+entry.path()+"_feet";
                String itemPath = "item/"+entry.path();

                this.models().getBuilder(centerPath)
                        .parent(this.models().getExistingFile(FurnitureMod.id(centerPath.replace(furniture.getWood().name()+"_", ""))))
                        .texture("particle", DataHelper.getWoodLog(furniture.getWood()))
                        .texture("planks", DataHelper.getWoodPlanks(furniture.getWood()));
                this.models().getBuilder(feetPath)
                        .parent(this.models().getExistingFile(FurnitureMod.id(feetPath.replace(furniture.getWood().name()+"_", ""))))
                        .texture("particle", DataHelper.getWoodLog(furniture.getWood()))
                        .texture("log", DataHelper.getWoodLog(furniture.getWood()));
                this.models().getBuilder(itemPath)
                        .parent(this.models().getExistingFile(FurnitureMod.id(itemPath.replace(furniture.getWood().name()+"_", ""))))
                        .texture("particle", DataHelper.getWoodLog(furniture.getWood()))
                        .texture("planks", DataHelper.getWoodPlanks(furniture.getWood()))
                        .texture("log", DataHelper.getWoodLog(furniture.getWood()));

                MultiPartBlockStateBuilder tableBuilder = this.getMultipartBuilder(furniture);
                addDirectionPart(tableBuilder.part(), this.modLoc(centerPath), 0, null, null, null, null);
                addDirectionPart(tableBuilder.part(), this.modLoc(feetPath), 0, false, false, null, null);
                addDirectionPart(tableBuilder.part(), this.modLoc(feetPath), 90, null, false, false, null);
                addDirectionPart(tableBuilder.part(), this.modLoc(feetPath), 180, null, null, false, false);
                addDirectionPart(tableBuilder.part(), this.modLoc(feetPath), 270, false, null, null, false);
            }else if(block instanceof KitchenCounterBlock furniture) {
                String defaultPath = "block/"+entry.path();
                String innerPath = "block/"+entry.path()+"_inner";
                String outerPath = "block/"+entry.path()+"_outer";
                String itemPath = "item/"+entry.path();

                this.models().getBuilder(defaultPath)
                        .parent(this.models().getExistingFile(FurnitureMod.id(defaultPath.replace(furniture.getStone().getPath()+"_", "").replace(furniture.getWood().name()+"_", ""))))
                        .texture("particle", DataHelper.getStone(furniture.getStone()))
                        .texture("stone", DataHelper.getStone(furniture.getStone()))
                        .texture("planks", DataHelper.getWoodPlanks(furniture.getWood()))
                        .texture("doors", DataHelper.getWoodDoors(furniture.getWood()));

                this.models().getBuilder(innerPath)
                        .parent(this.models().getExistingFile(FurnitureMod.id(innerPath.replace(furniture.getStone().getPath()+"_", "").replace(furniture.getWood().name()+"_", ""))))
                        .texture("particle", DataHelper.getStone(furniture.getStone()))
                        .texture("stone", DataHelper.getStone(furniture.getStone()))
                        .texture("planks", DataHelper.getWoodPlanks(furniture.getWood()));

                this.models().getBuilder(outerPath)
                        .parent(this.models().getExistingFile(FurnitureMod.id(outerPath.replace(furniture.getStone().getPath()+"_", "").replace(furniture.getWood().name()+"_", ""))))
                        .texture("particle", DataHelper.getStone(furniture.getStone()))
                        .texture("stone", DataHelper.getStone(furniture.getStone()))
                        .texture("planks", DataHelper.getWoodPlanks(furniture.getWood()))
                        .texture("doors", DataHelper.getWoodDoors(furniture.getWood()));

                this.models().getBuilder(itemPath).parent(this.models().getExistingFile(FurnitureMod.id(defaultPath)));

                VariantBlockStateBuilder counterBuilder = this.getVariantBuilder(furniture);
                for (int i = 0; i < 1 << 3; i++) {
                    boolean east  = (i & (1)) != 0;
                    boolean west  = (i & (1 << 1)) != 0;
                    boolean outer = (i & (1 << 2)) != 0;

                    ModelFile defaultModel = this.models().getExistingFile(FurnitureMod.id(defaultPath));
                    ModelFile innerModel = this.models().getExistingFile(FurnitureMod.id(innerPath));
                    ModelFile outerModel = this.models().getExistingFile(FurnitureMod.id(outerPath));

                    ModelFile model = (east && !west) || (!east && west) ? outer ? outerModel : innerModel : defaultModel;
                    if((east && !west && outer) || (!east && west && !outer)) {
                        counterBuilder.partialState()
                                .with(KitchenCounterBlock.EAST, east)
                                .with(KitchenCounterBlock.WEST, west)
                                .with(KitchenCounterBlock.OUTER, outer)
                                .modelForState()
                                .modelFile(model)
                                .rotationY(270)
                                .addModel();
                    }else{
                        counterBuilder.partialState()
                                .with(KitchenCounterBlock.EAST, east)
                                .with(KitchenCounterBlock.WEST, west)
                                .with(KitchenCounterBlock.OUTER, outer)
                                .modelForState()
                                .modelFile(model)
                                .addModel();
                    }
                }
            }else{
                if(block instanceof MetalWoodFurnitureSeatBlock furniture) {
                    ModelFile parentModel = this.models().getExistingFile(FurnitureMod.id("block/" + entry.path().replace(furniture.getWood().name() + "_", "")));
                    this.models().getBuilder("block/" + entry.path())
                            .parent(parentModel)
                            .texture("planks", DataHelper.getWoodPlanks(furniture.getWood()))
                            .texture("metal", DataHelper.getMetal(furniture.getMetal(), furniture.getAge()))
                            .texture("particle", DataHelper.getWoodLog(furniture.getWood()));
                }else if (block instanceof WoodBlock furniture) {
                    ModelFile parentModel = this.models().getExistingFile(FurnitureMod.id("block/" + entry.path().replace(furniture.getWood().name() + "_", "")));
                    this.models().getBuilder("block/" + entry.path())
                            .parent(parentModel)
                            .texture("log", DataHelper.getWoodLog(furniture.getWood()))
                            .texture("planks", DataHelper.getWoodPlanks(furniture.getWood()))
                            .texture("particle", DataHelper.getWoodLog(furniture.getWood()));
                } else if (block instanceof MetalBlock furniture) {
                    String path = entry.path()
                            .replace(furniture.getMetal().name().toLowerCase(Locale.US) + "_", "")
                            .replace(furniture.getAge().getSerializedName() + "_", "")
                            .replace("waxed_", "");
                    ModelFile parentModel = this.models().getExistingFile(FurnitureMod.id("block/" + path));
                    this.models().getBuilder("block/" + entry.path())
                            .parent(parentModel)
                            .texture("metal", DataHelper.getMetal(furniture.getMetal(), furniture.getAge()))
                            .texture("particle", DataHelper.getMetal(furniture.getMetal(), furniture.getAge()));
                }
                ModelFile model = this.models().getExistingFile(FurnitureMod.id("block/" + entry.path()));
                this.getVariantBuilder(block).forAllStates(state -> new ConfiguredModel[]{new ConfiguredModel(model)});
                this.models().getBuilder("item/" + entry.path()).parent(model);
            }
        });
    }

    private void addDirectionPart(ConfiguredModel.Builder<MultiPartBlockStateBuilder.PartBuilder> builder, ResourceLocation modelPath, int rotationY, Boolean north, Boolean east, Boolean south, Boolean west) {
        builder.modelFile(this.models().getExistingFile(modelPath));
        builder.rotationY(rotationY);
        MultiPartBlockStateBuilder.PartBuilder model = builder.addModel();
        if(north != null)
            model.condition(BlockStateProperties.NORTH, north);
        if(east != null)
            model.condition(BlockStateProperties.EAST, east);
        if(south != null)
            model.condition(BlockStateProperties.SOUTH, south);
        if(west != null)
            model.condition(BlockStateProperties.WEST, west);
    }

}
