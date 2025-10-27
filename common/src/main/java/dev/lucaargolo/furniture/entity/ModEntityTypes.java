package dev.lucaargolo.furniture.entity;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.utils.ModRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import java.util.function.Supplier;


public class ModEntityTypes {

    public static final ModRegistry<EntityType<?>> ENTITY_TYPES = FurnitureMod.INSTANCE.registry(Registries.ENTITY_TYPE);

    public static final Supplier<EntityType<SeatEntity>> SEAT = ENTITY_TYPES.register("seat", () -> EntityType.Builder.<SeatEntity>of(SeatEntity::new, MobCategory.MISC)
        .sized(0f, 0f)
        .build("seat")
    );

}
