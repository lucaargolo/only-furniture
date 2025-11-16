package dev.lucaargolo.furniture.block.base;

import dev.lucaargolo.furniture.utils.ColorProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.WoodType;

import java.util.Optional;

public interface WoodBlock {

    WoodType getWood();

    static Optional<Block> getPlanks(WoodType wood) {
        return BuiltInRegistries.BLOCK.getOptional(ResourceLocation.withDefaultNamespace(wood.name() + "_planks"));
    }

    static Optional<Block> getLeaves(WoodType wood) {
        return BuiltInRegistries.BLOCK.getOptional(ResourceLocation.withDefaultNamespace(wood.name() + "_leaves"));
    }

    static ColorProvider.Block getLeavesColor(WoodType wood) {
        Optional<Block> leaves = getLeaves(wood);
        return leaves.map(block -> (ColorProvider.Block) (blockState, blockAndTintGetter, blockPos, i) -> {
            BlockColors colors = Minecraft.getInstance().getBlockColors();
            return colors.getColor(block.defaultBlockState(), blockAndTintGetter, blockPos, i);
        }).orElse(null);
    }

    interface LeafBlock extends WoodBlock { }

}
