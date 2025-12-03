package dev.lucaargolo.furniture.client.utils;

import com.mojang.datafixers.util.Either;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface GroupedBlockModel {

    @Nullable
    Group furniture$getGroupHint();

    void furniture$setGroupHint(Group groupHint);

    record Group(String name, Vec3 origin, List<Either<Integer, Group>> children) {

    }

}
