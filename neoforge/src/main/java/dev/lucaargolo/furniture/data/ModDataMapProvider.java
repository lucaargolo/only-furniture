package dev.lucaargolo.furniture.data;

import dev.lucaargolo.furniture.block.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DataMapProvider;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;
import net.neoforged.neoforge.registries.datamaps.builtin.Oxidizable;
import net.neoforged.neoforge.registries.datamaps.builtin.Waxable;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ModDataMapProvider extends DataMapProvider {

    protected ModDataMapProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    protected void gather(HolderLookup.@NotNull Provider provider) {
        ModBlocks.WEATHERING_ENTRIES.forEach(entry -> {
            this.builder(NeoForgeDataMaps.OXIDIZABLES)
                    .add(entry.unaffected().key(), new Oxidizable(entry.exposed().get()), false)
                    .add(entry.exposed().key(), new Oxidizable(entry.weathered().get()), false)
                    .add(entry.weathered().key(), new Oxidizable(entry.oxidized().get()), false)
                    .build();

            this.builder(NeoForgeDataMaps.WAXABLES)
                    .add(entry.unaffected().key(), new Waxable(entry.waxedUnaffected().get()), false)
                    .add(entry.exposed().key(), new Waxable(entry.waxedExposed().get()), false)
                    .add(entry.weathered().key(), new Waxable(entry.waxedWeathered().get()), false)
                    .add(entry.oxidized().key(), new Waxable(entry.waxedOxidized().get()), false)
                    .build();
        });
    }


}
