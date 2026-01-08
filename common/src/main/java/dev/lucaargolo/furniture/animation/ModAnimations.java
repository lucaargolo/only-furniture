package dev.lucaargolo.furniture.animation;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.block.impl.FridgeBlock;
import dev.lucaargolo.furniture.registry.ModRegistry;
import dev.lucaargolo.furniture.registry.minecraft.MinecraftEntry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class ModAnimations {

    public static final ResourceLocation REGISTRY_ID = FurnitureMod.id("animation");
    public static final ResourceKey<Registry<AnimationDefinition>> REGISTRY_KEY = ResourceKey.createRegistryKey(REGISTRY_ID);
    public static final ModRegistry<AnimationDefinition> REGISTRY = FurnitureMod.registry(REGISTRY_KEY);

    public static final MinecraftEntry<AnimationDefinition> FRIDGE_OPEN_TOP_DOOR = REGISTRY.register("fridge_open_top_door", () -> {
        return new AnimationDefinition("top.door", 20, -135f, 0f, Easing.EASE_IN_OUT_SINE, Target.ROTATE_Y, state -> state.setValue(FridgeBlock.TOP_OPEN, true), state -> state.setValue(FridgeBlock.TOP_OPEN, true));
    });

    public static final MinecraftEntry<AnimationDefinition> FRIDGE_CLOSE_TOP_DOOR = REGISTRY.register("fridge_close_top_door", () -> {
        return new AnimationDefinition("top.door", 20, 0f, -135f, Easing.EASE_IN_OUT_SINE, Target.ROTATE_Y, state -> state.setValue(FridgeBlock.TOP_OPEN, true), state -> state.setValue(FridgeBlock.TOP_OPEN, false));
    });

    public static final MinecraftEntry<AnimationDefinition> FRIDGE_OPEN_BOTTOM_DOOR = REGISTRY.register("fridge_open_bottom_door", () -> {
        return new AnimationDefinition("bottom.door", 20, -135f, 0f, Easing.EASE_IN_OUT_SINE, Target.ROTATE_Y, state -> state.setValue(FridgeBlock.BOTTOM_OPEN, true), state -> state.setValue(FridgeBlock.BOTTOM_OPEN, true));
    });

    public static final MinecraftEntry<AnimationDefinition> FRIDGE_CLOSE_BOTTOM_DOOR = REGISTRY.register("fridge_close_bottom_door", () -> {
        return new AnimationDefinition("bottom.door", 20, 0f, -135f, Easing.EASE_IN_OUT_SINE, Target.ROTATE_Y, state -> state.setValue(FridgeBlock.BOTTOM_OPEN, true), state -> state.setValue(FridgeBlock.BOTTOM_OPEN, false));
    });

}
