package dev.lucaargolo.furniture.client.utils;

import org.joml.Vector3f;

public interface FurnitureBakedQuad {

    String furniture$getGroupName();

    void furniture$setGroupName(String groupName);

    Vector3f furniture$getPivot();

    void furniture$setPivot(Vector3f pivot);

}
