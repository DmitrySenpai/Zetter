package me.dantaeusb.zetter.core;

import me.dantaeusb.zetter.Zetter;
import me.dantaeusb.zetter.client.gui.ArtistTableScreen;
import me.dantaeusb.zetter.client.gui.EaselScreen;
import me.dantaeusb.zetter.client.painting.ClientPaintingToolParameters;
import me.dantaeusb.zetter.client.renderer.CanvasRenderer;
import me.dantaeusb.zetter.item.FrameItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.neoforged.fml.event.lifecycle.InterModProcessEvent;
import net.neoforged.neoforge.registries.DeferredItem;

@EventBusSubscriber(modid = Zetter.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ZetterSetup
{
    @SubscribeEvent
    @SuppressWarnings("unused")
    public static void onClientSetupEvent(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // @todo: [CRIT] Broke icons with paintings!
            for (DeferredItem<FrameItem> frame : ZetterItems.FRAMES.values()) {
                ItemProperties.register(frame.get(), ResourceLocation.withDefaultNamespace("painting"), FrameItem::getHasPaintingPropertyOverride);
                ItemProperties.register(frame.get(), ResourceLocation.withDefaultNamespace("plate"), FrameItem::getHasPaintingPropertyOverride);
            }

            new CanvasRenderer(Minecraft.getInstance().getTextureManager());
            new ClientPaintingToolParameters();
        });
    }

    @SubscribeEvent
    @SuppressWarnings("unused")
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ZetterContainerMenus.EASEL.get(), EaselScreen::new);
        event.register(ZetterContainerMenus.ARTIST_TABLE.get(), ArtistTableScreen::new);
    }

    @SubscribeEvent
    @SuppressWarnings("unused")
    public static void onImcSetupEvent(InterModEnqueueEvent event) {
        InterModComms.sendTo("carryon", "blacklistEntity", () -> "zetter:custom_painting_entity");
        InterModComms.sendTo("carryon", "blacklistEntity", () -> "zetter:easel_entity");
        InterModComms.sendTo("carryon", "blacklistBlock", () -> "zetter:easel");
    }

    @SubscribeEvent
    @SuppressWarnings("unused")
    public static void registerListeners(InterModProcessEvent event) {

    }
}
