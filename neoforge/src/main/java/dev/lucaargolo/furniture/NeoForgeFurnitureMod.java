package dev.lucaargolo.furniture;


import dev.lucaargolo.furniture.client.FurnitureModClient;
import dev.lucaargolo.furniture.registry.ModMenuTypeRegistry;
import io.netty.buffer.Unpooled;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.network.connection.ConnectionType;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

@Mod(FurnitureMod.MOD_ID)
public class NeoForgeFurnitureMod extends FurnitureMod {

    private final IEventBus modBus;

    public NeoForgeFurnitureMod(IEventBus modBus) {
        this.modBus = modBus;
        this.init();
        if(FMLEnvironment.dist.isClient()) {
            loadPlatformClass(FurnitureModClient.class);
        }
    }

    @Override
    public String getPlatform() {
        return "NeoForge";
    }

    @Override
    public boolean isFakePlayer(Player player) {
        return player instanceof FakePlayer;
    }

    @Override
    public Block getPottedBlock(Block block) {
        FlowerPotBlock potBlock = (FlowerPotBlock) Blocks.FLOWER_POT;
        Supplier<? extends Block> pottedBlockSupplier = potBlock.getFullPotsView().getOrDefault(BuiltInRegistries.BLOCK.getKey(block), () -> Blocks.FLOWER_POT);
        return pottedBlockSupplier.get();
    }

    @Override
    public <M extends AbstractContainerMenu, D> void openMenu(ModMenuTypeRegistry.AdvancedMenuTypeEntry<M, D> entry, Player player, Component title, D data) {
        player.openMenu(new MenuProvider() {
            @Override
            public @NotNull Component getDisplayName() {
                return title;
            }

            @Override
            public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
                RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), player.level().registryAccess(), ConnectionType.NEOFORGE);
                entry.getStreamCodec().encode(buf, data);
                return entry.get().create(containerId, playerInventory, buf);
            }
        }, buf -> entry.getStreamCodec().encode(buf, data));
    }

    public static IEventBus getModBus() {
        return ((NeoForgeFurnitureMod) FurnitureMod.getInstance()).modBus;
    }

}
