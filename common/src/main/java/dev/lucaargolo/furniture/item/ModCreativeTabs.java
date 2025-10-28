package dev.lucaargolo.furniture.item;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.ModRegistry;
import dev.lucaargolo.furniture.block.MetalBlock;
import dev.lucaargolo.furniture.block.ModBlocks;
import dev.lucaargolo.furniture.block.WoodBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.properties.WoodType;
import org.apache.commons.lang3.stream.Streams;

import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

public class ModCreativeTabs {

    private static final List<WoodType> WOOD_TYPES = WoodType.values().toList();
    private static final Comparator<ModRegistry.ModEntry<? extends Item>> COMPARATOR = Comparator
            // Step 1: Non-BlockItems come first
            .comparing((ModRegistry.ModEntry<? extends Item> e) -> !(e.get() instanceof BlockItem))
            // Step 2: Among BlockItems, order by type: others < WoodBlock < MetalBlock
            .thenComparing(entry -> {
                if (entry.get() instanceof BlockItem bi) {
                    if (bi.getBlock() instanceof WoodBlock) return 1;
                    if (bi.getBlock() instanceof MetalBlock) return 2;
                }
                return 0;
            })
            // Step 3: For WoodBlocks, sort by wood type order
            .thenComparing(entry -> {
                if (entry.get() instanceof BlockItem bi && bi.getBlock() instanceof WoodBlock wb) {
                    return WOOD_TYPES.indexOf(wb.getWood());
                }
                return -1;
            })
            // Step 4: For MetalBlocks, sort by metal type, then by age
            .thenComparing(entry -> {
                if (entry.get() instanceof BlockItem bi && bi.getBlock() instanceof MetalBlock mb) {
                    return mb.getMetal().ordinal();
                }
                return -1;
            })
            .thenComparing(entry -> {
                if (entry.get() instanceof BlockItem bi && bi.getBlock() instanceof MetalBlock mb) {
                    return mb.getAge().ordinal();
                }
                return -1;
            })
            // Step 5: Finally, alphabetical by path
            .thenComparing(ModRegistry.ModEntry::path, String::compareToIgnoreCase);


    public static final ModRegistry<CreativeModeTab> CREATIVE_TABS = FurnitureMod.INSTANCE.registry(Registries.CREATIVE_MODE_TAB);

    public static Supplier<CreativeModeTab> CREATIVE_TAB = CREATIVE_TABS.register("creative_tab", () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
            .title(Component.translatable("itemGroup."+FurnitureMod.MOD_ID+".creative_tab"))
            .icon(ModBlocks.LAMP_POST.get().asItem()::getDefaultInstance)
            .displayItems(((parameters, output) -> {
                Streams.of(ModItems.ITEMS).sorted(COMPARATOR).map(ModRegistry.ModEntry::get).forEach(output::accept);
            }))
            .build()
    );

}
