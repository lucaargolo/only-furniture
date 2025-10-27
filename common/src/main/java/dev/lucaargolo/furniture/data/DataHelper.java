package dev.lucaargolo.furniture.data;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.WoodType;

public class DataHelper {

    public static ResourceLocation getWoodPlanks(WoodType type) {
        return ResourceLocation.withDefaultNamespace("block/" + type.name() + "_planks");
    }

    public static ResourceLocation getWoodLog(WoodType type) {
        if (type == WoodType.BAMBOO) {
            return ResourceLocation.withDefaultNamespace("block/" + type.name() + "_stalk");
        } else if (type == WoodType.CRIMSON || type == WoodType.WARPED) {
            return ResourceLocation.withDefaultNamespace("block/" + type.name() + "_stem");
        } else {
            return ResourceLocation.withDefaultNamespace("block/" + type.name() + "_log");
        }
    }

}
