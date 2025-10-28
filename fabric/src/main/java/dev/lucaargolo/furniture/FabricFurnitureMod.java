package dev.lucaargolo.furniture;

import dev.lucaargolo.furniture.block.ModBlocks;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.registry.OxidizableBlocksRegistry;
import net.minecraft.world.entity.player.Player;

public class FabricFurnitureMod extends FurnitureMod implements ModInitializer {

    @Override
    public void onInitialize() {
        this.init();
        ModBlocks.WEATHERING_ENTRIES.forEach(entry -> {
            OxidizableBlocksRegistry.registerOxidizableBlockPair(entry.unaffected().get(), entry.exposed().get());
            OxidizableBlocksRegistry.registerOxidizableBlockPair(entry.exposed().get(), entry.weathered().get());
            OxidizableBlocksRegistry.registerOxidizableBlockPair(entry.weathered().get(), entry.oxidized().get());
            OxidizableBlocksRegistry.registerWaxableBlockPair(entry.unaffected().get(), entry.waxedUnaffected().get());
            OxidizableBlocksRegistry.registerWaxableBlockPair(entry.exposed().get(), entry.waxedExposed().get());
            OxidizableBlocksRegistry.registerWaxableBlockPair(entry.weathered().get(), entry.waxedWeathered().get());
            OxidizableBlocksRegistry.registerWaxableBlockPair(entry.oxidized().get(), entry.waxedOxidized().get());
        });
    }

    @Override
    public boolean isFakePlayer(Player player) {
        return player instanceof FakePlayer;
    }

    @Override
    public String getPlatform() {
        return "Fabric";
    }

}
