package me.dantaeusb.zetter.core;

import me.dantaeusb.zetter.Zetter;
import me.dantaeusb.zetter.capability.canvastracker.CanvasServerTracker;
import me.dantaeusb.zetter.client.gui.overlay.CanvasOverlay;
import me.dantaeusb.zetter.client.renderer.CanvasRenderer;
import me.dantaeusb.zetter.menu.EaselMenu;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

@EventBusSubscriber(modid = Zetter.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class ZetterGameEvents {
    @SubscribeEvent
    public static void onPlayerDisconnected(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        CanvasServerTracker canvasTracker = (CanvasServerTracker) Helper.getLevelCanvasTracker(player.level());

        canvasTracker.stopTrackingAllCanvases(player.getUUID());
    }

    @SubscribeEvent
    public static void tickCanvasTracker(ServerTickEvent.Post event) {
        CanvasServerTracker canvasTracker = (CanvasServerTracker) Helper.getLevelCanvasTracker(ServerLifecycleHooks.getCurrentServer().overworld());
        canvasTracker.tick();
    }

    /**
     * @todo: [MED] Do we really need that hook here? It might be called very frequently
     * @param event
     */
    @SubscribeEvent
    public static void onRenderTickStart(ClientTickEvent.Post event) {
        if (Minecraft.getInstance().level != null) {
            CanvasRenderer.getInstance().update(Util.getMillis());
        }

        for (CanvasOverlay<?> overlay : ZetterOverlays.OVERLAYS.values()) {
            overlay.tick();
        }
    }

    /**
     * We need this because we need to start flow
     * only when container is opened and initialized
     *
     * Server-only
     * @param event
     */
    @SubscribeEvent
    public static void onPlayerContainerOpened(PlayerContainerEvent.Open event) {
        if (event.getContainer() instanceof EaselMenu easelMenu) {
            easelMenu.getState().addPlayer(event.getEntity());
        }
    }

    /**
     * We need this because we need to start flow
     * only when container is opened and initialized
     *
     * Server-only
     * @param event
     */
    @SubscribeEvent
    public static void onPlayerContainerClosed(PlayerContainerEvent.Close event) {
        if (event.getContainer() instanceof EaselMenu easelMenu) {
            easelMenu.getState().removePlayer(event.getEntity());
        }
    }
}
