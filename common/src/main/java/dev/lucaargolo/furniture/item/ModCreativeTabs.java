package dev.lucaargolo.furniture.item;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.block.ModBlocks;
import dev.lucaargolo.furniture.utils.ModRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;

import java.util.function.Supplier;

public class ModCreativeTabs {

    public static final ModRegistry<CreativeModeTab> CREATIVE_TABS = FurnitureMod.INSTANCE.registry(Registries.CREATIVE_MODE_TAB);

    public static Supplier<CreativeModeTab> CREATIVE_TAB = CREATIVE_TABS.register("creative_tab", () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
            .title(Component.translatable("itemGroup."+FurnitureMod.MOD_ID+".creative_tab"))
            .icon(ModBlocks.LAMP_POST.get().asItem()::getDefaultInstance)
            .displayItems(((parameters, output) -> {
                ModItems.ITEMS.forEach((entry) -> output.accept(entry.get()));
            }))
            .build()
    );

}
