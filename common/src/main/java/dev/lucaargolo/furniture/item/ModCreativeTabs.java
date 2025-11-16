package dev.lucaargolo.furniture.item;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.block.ModBlocks;
import dev.lucaargolo.furniture.block.base.WoodBlock;
import dev.lucaargolo.furniture.registry.ModItemRegistry;
import dev.lucaargolo.furniture.registry.ModRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.state.properties.WoodType;
import org.apache.commons.lang3.stream.Streams;

import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

public class ModCreativeTabs {

    private static final List<WoodType> WOOD_TYPES = WoodType.values().toList();

    private static final Comparator<ModItemRegistry.ItemEntry<?>> COMPARATOR = Comparator.comparing((ModItemRegistry.ItemEntry<?> entry) -> {
        return (entry.get() instanceof BlockItem bi && bi.getBlock() instanceof WoodBlock) ? 0 : 1;
    }).thenComparing(entry -> {
        if (entry.get() instanceof BlockItem bi && bi.getBlock() instanceof WoodBlock wb && !(bi.getBlock() instanceof WoodBlock.LeafBlock)) {
            int idx = WOOD_TYPES.indexOf(wb.getWood());
            return idx >= 0 ? idx : Integer.MAX_VALUE;
        } else {
            return Integer.MAX_VALUE;
        }
    }).thenComparingInt(ModItemRegistry.ItemEntry::getLocalId);

    public static final ModRegistry<CreativeModeTab> REGISTRY = FurnitureMod.INSTANCE.registry(Registries.CREATIVE_MODE_TAB);

    public static Supplier<CreativeModeTab> CREATIVE_TAB = REGISTRY.register("creative_tab", () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
            .title(Component.translatable("itemGroup."+FurnitureMod.MOD_ID+".creative_tab"))
            .icon(ModBlocks.OUTDOOR_BENCH_MAP.get(WoodType.OAK).get().asItem()::getDefaultInstance)
            .displayItems(((parameters, output) -> {
                Streams.of(ModItems.REGISTRY).sorted(COMPARATOR).map(ModItemRegistry.ItemEntry::get).forEach(output::accept);
            }))
            .build()
    );

}
