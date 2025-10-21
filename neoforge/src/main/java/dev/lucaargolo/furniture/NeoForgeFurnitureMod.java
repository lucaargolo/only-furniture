package dev.lucaargolo.furniture;


import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(FurnitureMod.MOD_ID)
public class NeoForgeFurnitureMod extends FurnitureMod {

    public static NeoForgeFurnitureMod INSTANCE;

    public NeoForgeFurnitureMod(IEventBus eventBus) {
        INSTANCE = this;
        LOG.info("Hello from NeoForge!");
        this.init();
    }

    @Override
    public String getPlatform() {
        return "NeoForge";
    }

}
