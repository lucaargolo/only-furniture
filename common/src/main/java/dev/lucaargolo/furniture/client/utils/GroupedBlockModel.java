package dev.lucaargolo.furniture.client.utils;

import org.jetbrains.annotations.Nullable;

public interface GroupedBlockModel {

    @Nullable
    ModelGroup furniture$getGroup();

    void furniture$setGroup(ModelGroup group);

}
