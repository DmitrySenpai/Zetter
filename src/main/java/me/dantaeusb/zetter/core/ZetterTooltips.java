package me.dantaeusb.zetter.core;

import me.dantaeusb.zetter.Zetter;
import me.dantaeusb.zetter.client.gui.tooltip.CanvasTooltipRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = Zetter.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ZetterTooltips {
  @SubscribeEvent
  public static void registerClientTooltipComponentFactories(RegisterClientTooltipComponentFactoriesEvent event) {
    event.register(CanvasTooltipRenderer.CanvasComponent.class, CanvasTooltipRenderer::new);
  }
}
