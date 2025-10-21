package dev.lucaargolo.furniture.client;

public class NeoForgeFurnitureModClient extends FurnitureModClient {

    public static NeoForgeFurnitureModClient INSTANCE;

    public NeoForgeFurnitureModClient() {
        this.init();
    }

    @Override
    public void init() {
        INSTANCE = this;
        super.init();
    }

}
