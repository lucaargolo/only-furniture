package dev.lucaargolo.furniture.data;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.block.base.MetalBlock;
import dev.lucaargolo.furniture.block.base.StoneBlock;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.properties.WoodType;

import java.util.Arrays;
import java.util.Set;

public class DataHelper {

    public static ResourceLocation getStone(StoneBlock.StoneType stone) {
        String path = switch (stone) {
            case QUARTZ_BLOCK -> "quartz_block_top";
            case SMOOTH_QUARTZ -> "quartz_block_bottom";
            case SMOOTH_SANDSTONE -> "sandstone_top";
            //case SMOOTH_RED_SANDSTONE -> "red_sandstone_top";
            default -> stone.getPath();
        };
        return ResourceLocation.withDefaultNamespace("block/"+path);
    }

    public static ResourceLocation getWoodLeaves(WoodType wood) {
        return ResourceLocation.withDefaultNamespace("block/" + wood.name() + "_leaves");
    }

    public static ResourceLocation getWoodPlanks(WoodType wood) {
        return ResourceLocation.withDefaultNamespace("block/" + wood.name() + "_planks");
    }

    public static ResourceLocation getWoodLog(WoodType wood) {
        if (wood == WoodType.BAMBOO) {
            return ResourceLocation.withDefaultNamespace("block/" + wood.name() + "_stalk");
        } else if (wood == WoodType.CRIMSON || wood == WoodType.WARPED) {
            return ResourceLocation.withDefaultNamespace("block/" + wood.name() + "_stem");
        } else {
            return ResourceLocation.withDefaultNamespace("block/" + wood.name() + "_log");
        }
    }

    public static ResourceLocation getWoodDoors(WoodType wood) {
        return FurnitureMod.id("block/" + wood.name() + "_doors");
    }

    public static ResourceLocation getMetal(MetalBlock.MetalType metal, WeatheringCopper.WeatherState age) {
        Block block = metal.getTexture(age);
        ResourceLocation location = BuiltInRegistries.BLOCK.getKey(block);
        return location.withPrefix("block/");
    }

    public static String defaultTranslation(String string) {
        if(!string.isBlank()) {
            String[] words = string.replace("_", " ").split(" ");
            for (int i = 0; i < words.length; i++) {
                words[i] = words[i].substring(0, 1).toUpperCase() + words[i].substring(1);
            }
            return String.join(" ", words);
        }else{
            return string;
        }
    }

    private static final Set<String> ADJECTIVES = Set.of("small", "large", "medium", "outdoor", "indoor");

    public static String woodBlockTranslation(WoodType wood, String string) {
        String[] words = string.replace(wood.name()+"_", "").replace("_", " ").split(" ");
        String[] adjectives = Arrays.stream(words).filter(ADJECTIVES::contains).toArray(String[]::new);
        String[] nouns = Arrays.stream(words).filter(s -> !ADJECTIVES.contains(s)).toArray(String[]::new);

        String first = defaultTranslation(String.join(" ", adjectives));
        String middle = defaultTranslation(wood.name());
        String last = defaultTranslation(String.join(" ", nouns));

        return !first.isBlank() ? first + " " + middle + " " + last : middle + " " + last;
    }


}
