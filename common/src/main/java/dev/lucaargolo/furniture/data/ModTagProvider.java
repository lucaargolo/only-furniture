package dev.lucaargolo.furniture.data;

import dev.lucaargolo.furniture.ModRegistry;
import net.minecraft.tags.TagKey;

import java.util.Arrays;
import java.util.Optional;

public interface ModTagProvider<T> {

    ModTagBuilder<T> getOrCreateModTagBuilder(TagKey<T> tag);

    default void addTags(ModRegistry<T> registry) {
        registry.forEach((entry) -> {
            Arrays.stream(entry.getTags()).map(t -> t.cast(registry.getRegistryKey())).filter(Optional::isPresent).map(Optional::get).forEach(tag -> {
                getOrCreateModTagBuilder(tag).add(entry.get()).setReplace(false);
            });
        });
    }

}
