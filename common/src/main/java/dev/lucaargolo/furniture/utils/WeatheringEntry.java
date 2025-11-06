package dev.lucaargolo.furniture.utils;

import dev.lucaargolo.furniture.registry.ModBlockRegistry;

public record WeatheringEntry(
        ModBlockRegistry.BlockEntry<?> unaffected,
        ModBlockRegistry.BlockEntry<?> exposed,
        ModBlockRegistry.BlockEntry<?> weathered,
        ModBlockRegistry.BlockEntry<?> oxidized,
        ModBlockRegistry.BlockEntry<?> waxedUnaffected,
        ModBlockRegistry.BlockEntry<?> waxedExposed,
        ModBlockRegistry.BlockEntry<?> waxedWeathered,
        ModBlockRegistry.BlockEntry<?> waxedOxidized
) { }
