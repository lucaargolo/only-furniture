package dev.lucaargolo.furniture.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.registry.minecraft.MinecraftEntry;
import dev.lucaargolo.furniture.sound.Instrument;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.sounds.SoundEvent;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ModSoundsProvider implements DataProvider {

    private final PackOutput output;

    public ModSoundsProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput output) {
        Path outputPath = this.output.getOutputFolder(PackOutput.Target.RESOURCE_PACK).resolve(FurnitureMod.MOD_ID+"/sounds.json");
        JsonObject outputJson = new JsonObject();
        for(Instrument instrument : Instrument.INSTRUMENTS) {
            for(Map.Entry<Instrument.Note, MinecraftEntry<SoundEvent>> entry : instrument.sounds().entrySet()) {
                Instrument.Note key = entry.getKey();
                String path = entry.getValue().key().getPath();
                JsonObject soundJson = new JsonObject();
                JsonArray soundsArray = new JsonArray();
                //TODO: Actually check the files instead of just assuming two variations
                for(int i = 0; i < 2; i++) {
                    JsonObject sound = new JsonObject();
                    sound.addProperty("name", FurnitureMod.id("piano/" + key.name().toLowerCase(Locale.ROOT) + "_" + i).toString());
                    soundsArray.add(sound);
                }
                soundJson.add("sounds", soundsArray);
                outputJson.add(path, soundJson);
            }
        }
        return DataProvider.saveStable(output, outputJson, outputPath);
    }

    @Override
    public @NotNull String getName() {
        return "FurnitureSounds";
    }
}
