package dev.lucaargolo.furniture.client.utils;

import org.jetbrains.annotations.Nullable;

public interface GroupedModel {

    @Nullable
    ModelGroup furniture$getGroup();

    void furniture$setGroup(ModelGroup group);

}
