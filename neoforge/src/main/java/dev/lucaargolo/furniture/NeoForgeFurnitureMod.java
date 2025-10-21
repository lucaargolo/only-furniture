package dev.lucaargolo.furniture;


import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(FurnitureMod.MOD_ID)
public class NeoForgeFurnitureMod extends FurnitureMod {

    public static NeoForgeFurnitureMod INSTANCE;
    private final IEventBus modBus;

    public NeoForgeFurnitureMod(IEventBus modBus) {
        INSTANCE = this;
        this.modBus = modBus;
        this.init();
    }

    @Override
    public String getPlatform() {
        return "NeoForge";
    }

    public IEventBus getModBus() {
        return modBus;
    }
}
