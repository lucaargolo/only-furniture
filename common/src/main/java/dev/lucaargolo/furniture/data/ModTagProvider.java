package dev.lucaargolo.furniture.data;

import dev.lucaargolo.furniture.ModRegistry;
import dev.lucaargolo.furniture.data.builder.ModTagBuilder;
import net.minecraft.tags.TagKey;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

public class ModTagProvider {

    public static <T> void generate(ModRegistry<T> registry, Function<TagKey<T>, ModTagBuilder<T>> function) {
        registry.forEach((entry) -> {
            Arrays.stream(entry.getTags()).map(t -> t.cast(registry.getRegistryKey())).filter(Optional::isPresent).map(Optional::get).forEach(tag -> {
                function.apply(tag).add(entry.get()).setReplace(false);
            });
        });
    }

}
