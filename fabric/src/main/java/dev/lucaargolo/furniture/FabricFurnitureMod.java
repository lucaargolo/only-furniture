package dev.lucaargolo.furniture;

import dev.lucaargolo.furniture.block.ModBlocks;
import dev.lucaargolo.furniture.mixin.FlowerPotBlockAccessor;
import dev.lucaargolo.furniture.registry.ModMenuTypeRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.registry.OxidizableBlocksRegistry;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.OptionalInt;

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
    public String getPlatform() {
        return "Fabric";
    }

    @Override
    public boolean isFakePlayer(Player player) {
        return player instanceof FakePlayer;
    }

    @Override
    public Block getPottedBlock(Block block) {
        return FlowerPotBlockAccessor.getPottedByContentMap().getOrDefault(block, Blocks.FLOWER_POT);
    }

    @Override
    @Nullable
    @SuppressWarnings("unchecked")
    public <M extends AbstractContainerMenu, D> M openMenu(ModMenuTypeRegistry.AdvancedMenuTypeEntry<M, D> entry, Player player, Component title, D data) {
        ExtendedScreenHandlerType<M, D> type = (ExtendedScreenHandlerType<M, D>) entry.get();
        OptionalInt optional = player.openMenu(new ExtendedScreenHandlerFactory<D>() {
            @Override
            public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
                return type.create(i, inventory, data);
            }

            @Override
            public @NotNull Component getDisplayName() {
                return title;
            }

            @Override
            public D getScreenOpeningData(ServerPlayer serverPlayer) {
                return data;
            }
        });
        return optional.isPresent() ? (M) player.containerMenu : null;
    }
}
