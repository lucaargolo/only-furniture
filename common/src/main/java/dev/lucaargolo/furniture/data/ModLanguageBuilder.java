package dev.lucaargolo.furniture.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatType;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public interface ModLanguageBuilder {

    void add(String key, String value);

    default void add(TagKey<?> tagKey, String value) {
        this.add(tagTranslationKey(tagKey), value);
    }

    default void add(ResourceLocation identifier, String value) {
        this.add(identifier.toLanguageKey(), value);
    }

    default void add(Item item, String value) {
        this.add(item.getDescriptionId(), value);
    }

    default void add(Block block, String value) {
        this.add(block.getDescriptionId(), value);
    }

    default void add(EntityType<?> entityType, String value) {
        this.add(entityType.getDescriptionId(), value);
    }

    default void add(Holder<Attribute> entityAttribute, String value) {
        this.add(entityAttribute.value().getDescriptionId(), value);
    }

    default void add(MobEffect statusEffect, String value) {
        this.add(statusEffect.getDescriptionId(), value);
    }

    default void add(StatType<?> statType, String value) {
        this.add("stat_type." + Objects.requireNonNull(BuiltInRegistries.STAT_TYPE.getKey(statType)).toString().replace(':', '.'), value);
    }

    default void addCreativeTab(ResourceKey<CreativeModeTab> registryKey, String value) {
        CreativeModeTab group = BuiltInRegistries.CREATIVE_MODE_TAB.getOrThrow(registryKey);
        ComponentContents content = group.getDisplayName().getContents();
        if (content instanceof TranslatableContents translatableTextContent) {
            this.add(translatableTextContent.getKey(), value);
        } else {
            throw new UnsupportedOperationException("Cannot add language entry for ItemGroup (%s) as the display name is not translatable.".formatted(group.getDisplayName().getString()));
        }
    }

    default void addEnchantment(ResourceKey<Enchantment> enchantment, String value) {
        this.add(Util.makeDescriptionId("enchantment", enchantment.location()), value);
    }

    default void addExistingFile(Path existingLanguageFile) throws IOException {
        try (Reader reader = Files.newBufferedReader(existingLanguageFile)) {
            JsonObject translations = JsonParser.parseReader(reader).getAsJsonObject();

            for(String key : translations.keySet()) {
                this.add(key, translations.get(key).getAsString());
            }
        }

    }

    private static String tagTranslationKey(TagKey<?> tagKey) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("tag.");
        ResourceLocation registryIdentifier = tagKey.registry().location();
        ResourceLocation tagIdentifier = tagKey.location();
        if (!registryIdentifier.getNamespace().equals("minecraft")) {
            stringBuilder.append(registryIdentifier.getNamespace()).append(".");
        }

        stringBuilder.append(registryIdentifier.getPath().replace("/", ".")).append(".").append(tagIdentifier.getNamespace()).append(".").append(tagIdentifier.getPath().replace("/", ".").replace(":", "."));
        return stringBuilder.toString();
    }

}
