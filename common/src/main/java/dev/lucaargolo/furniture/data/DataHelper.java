package dev.lucaargolo.furniture.data;

import dev.lucaargolo.furniture.block.MetalBlock;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;
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

    public static ResourceLocation getMetal(MetalBlock.MetalType metal, WeatheringCopper.WeatherState age) {
        Block block = metal.getTexture(age);
        ResourceLocation location = BuiltInRegistries.BLOCK.getKey(block);
        return location.withPrefix("block/");
    }

    public static String defaultTranslation(String string) {
        String[] words = string.replace("_", " ").split(" ");
        for (int i = 0; i < words.length; i++) {
            words[i] = words[i].substring(0, 1).toUpperCase() + words[i].substring(1);
        }
        return String.join(" ", words);
    }

    public static String woodBlockTranslation(String string) {
        String[] words = string.replace("_", " ").split(" ");
        if(words.length < 2) {
            return defaultTranslation(string);
        }else{
            String wood = words[0];
            words[0] = words[1];
            words[1] = wood;
            return defaultTranslation(String.join(" ", words));
        }
    }


}
