package dev.lucaargolo.furniture.utils;

import dev.lucaargolo.furniture.ModRegistry;
import net.minecraft.world.level.block.Block;

public record WeatheringEntry(
        ModRegistry.ModEntry<? extends Block> unaffected,
        ModRegistry.ModEntry<? extends Block> exposed,
        ModRegistry.ModEntry<? extends Block> weathered,
        ModRegistry.ModEntry<? extends Block> oxidized,
        ModRegistry.ModEntry<? extends Block> waxedUnaffected,
        ModRegistry.ModEntry<? extends Block> waxedExposed,
        ModRegistry.ModEntry<? extends Block> waxedWeathered,
        ModRegistry.ModEntry<? extends Block> waxedOxidized
) { }
