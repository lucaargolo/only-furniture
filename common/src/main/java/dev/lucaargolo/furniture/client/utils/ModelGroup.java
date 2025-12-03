package dev.lucaargolo.furniture.client.utils;

import com.mojang.datafixers.util.Either;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public record ModelGroup(String name, Vec3 origin, List<Either<Integer, ModelGroup>> children) {

}
