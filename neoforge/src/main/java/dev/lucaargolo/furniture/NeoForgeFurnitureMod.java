package dev.lucaargolo.furniture;


import dev.lucaargolo.furniture.client.FurnitureModClient;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.util.FakePlayer;

@Mod(FurnitureMod.MOD_ID)
public class NeoForgeFurnitureMod extends FurnitureMod {

    private final IEventBus modBus;

    public NeoForgeFurnitureMod(IEventBus modBus) {
        this.modBus = modBus;
        this.init();
        if(FMLEnvironment.dist.isClient()) {
            this.loadPlatformClass(FurnitureModClient.class);
        }
    }

    @Override
    public boolean isFakePlayer(Player player) {
        return player instanceof FakePlayer;
    }

    @Override
    public String getPlatform() {
        return "NeoForge";
    }

    public static IEventBus getModBus() {
        return ((NeoForgeFurnitureMod) FurnitureMod.INSTANCE).modBus;
    }

}
