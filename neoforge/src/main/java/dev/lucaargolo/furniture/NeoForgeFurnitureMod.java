package dev.lucaargolo.furniture;


import dev.lucaargolo.furniture.client.FurnitureModClient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@Mod(FurnitureMod.MOD_ID)
public class NeoForgeFurnitureMod extends FurnitureMod {

    public static NeoForgeFurnitureMod INSTANCE;
    private final IEventBus modBus;

    public NeoForgeFurnitureMod(IEventBus modBus) {
        INSTANCE = this;
        this.modBus = modBus;
        this.modBus.register(this);
        this.init();
    }

    @SubscribeEvent
    public void onClientInit(FMLClientSetupEvent event) {
        loadPlatformClass(FurnitureModClient.class);
    }

    @Override
    public String getPlatform() {
        return "NeoForge";
    }

    public IEventBus getModBus() {
        return modBus;
    }
}
