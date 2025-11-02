package dev.lucaargolo.furniture.data;

import dev.lucaargolo.furniture.FurnitureMod;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class NeoForgeModLanguageProvider extends LanguageProvider {

    public NeoForgeModLanguageProvider(PackOutput output) {
        super(output, FurnitureMod.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        ModLanguageProvider.generate(this::add);
    }

}
