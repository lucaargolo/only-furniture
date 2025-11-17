package dev.lucaargolo.furniture.client.screen;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.menu.StorageMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.NotNull;

public class StorageMenuScreen extends AbstractContainerScreen<StorageMenu> {

    private static final ResourceLocation texture = FurnitureMod.id("textures/gui/storage.png");

    public StorageMenuScreen(StorageMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        int storageHeight = 24 + 18 * menu.getRows();
        int playerHeight = 101;
        this.imageHeight = storageHeight + playerHeight;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        blitBg(guiGraphics, texture, leftPos, topPos, imageWidth, 24+18*menu.getRows()); //STORAGE AREA
        blitBg(guiGraphics, texture, leftPos, topPos+24+18*menu.getRows(), imageWidth, imageHeight-(24+18*menu.getRows())); //REMAINING AREA (player inventory)

        for(Slot slot : this.menu.slots) {
            guiGraphics.blit(texture, leftPos+slot.x-1, topPos+slot.y-1, 18, 18, 18, 0, 18, 18, 256, 256);
        }
    }

    private void blitBg(GuiGraphics guiGraphics, ResourceLocation texture, int x, int y, int width, int height) {
        guiGraphics.blit(texture, x, y, 6, 6, 0, 0, 6, 6, 256, 256); //TOP LEFT
        guiGraphics.blit(texture, x+6, y, width -12, 6, 6, 0, 6, 6, 256, 256); //TOP
        guiGraphics.blit(texture, x+ width -6, y, 6, 6, 12, 0, 6, 6, 256, 256); //TOP RIGHT

        guiGraphics.blit(texture, x, y+6, 6, height -12, 0, 6, 6, 6, 256, 256); //LEFT
        guiGraphics.blit(texture, x+6, y+6, width -12, height -12, 6, 6, 6, 6, 256, 256); //CENTER
        guiGraphics.blit(texture, x+6+ width -12, y+6, 6, height -12, 12, 6, 6, 6, 256, 256); //RIGHT

        guiGraphics.blit(texture, x, y+ height -6, 6, 6, 0, 12, 6, 6, 256, 256); //BOTTOM LEFT
        guiGraphics.blit(texture, x+6, y+ height -6, width -12, 6, 6, 12, 6, 6, 256, 256); //BOTTOM
        guiGraphics.blit(texture, x+ width -6, y+ height -6, 6, 6, 12, 12, 6, 6, 256, 256); //BOTTOM RIGHT
    }

}
