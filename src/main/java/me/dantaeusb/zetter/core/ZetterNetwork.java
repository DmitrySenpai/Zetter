package me.dantaeusb.zetter.core;

import me.dantaeusb.zetter.Zetter;
import me.dantaeusb.zetter.network.packet.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;

@EventBusSubscriber(modid = Zetter.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ZetterNetwork {
    // @todo: [LOW] Rename this on release, it's zetter:zetter_channel 0.1
    public static final ResourceLocation simpleChannelRL = ResourceLocation.fromNamespaceAndPath(Zetter.MOD_ID, "zetter_channel");
    public static final int MESSAGE_PROTOCOL_VERSION = 3;

    public static final byte PAINTING_FRAME = 21;
    public static final byte CANVAS_REQUEST = 22;
    public static final byte CANVAS_SYNC = 24;

    public static final byte PAINTING_UNLOAD_CANVAS = 23;

    public static final byte PALETTE_UPDATE = 25;
    public static final byte PAINTING_RENAME = 26;

    public static final byte CANVAS_REQUEST_SYNC_VIEW = 28;
    public static final byte CANVAS_SYNC_VIEW = 27;

    public static final byte CANVAS_REMOVE = 29;
    public static final byte EASEL_SYNC = 30;

    public static final byte HISTORY_UPDATE = 31;
    public static final byte HISTORY_SYNC = 33;
    public static final byte HISTORY_RESET = 34;

    public static final byte ARTIST_TABLE_MODE = 32;

    public static final byte EASEL_CANVAS_INIT = 35;

    public static final byte CANVAS_REQUEST_EXPORT = 40;
    public static final byte CANVAS_EXPORT = 41;
    public static final byte CANVAS_EXPORT_ERROR = 42;

    private static final Map<Integer, PacketRegistration<?>> REGISTRATIONS_BY_ID = new HashMap<>();
    private static final Map<Class<?>, PacketRegistration<?>> REGISTRATIONS_BY_CLASS = new HashMap<>();

    @SubscribeEvent
    @SuppressWarnings("unused")
    public static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        REGISTRATIONS_BY_ID.clear();
        REGISTRATIONS_BY_CLASS.clear();

        registerMessage(PAINTING_FRAME, CCanvasActionPacket.class, CCanvasActionPacket::writePacketData, CCanvasActionPacket::readPacketData, CCanvasActionPacket::handle, PacketFlow.SERVERBOUND);
        registerMessage(CANVAS_REQUEST, CCanvasRequestPacket.class, CCanvasRequestPacket::writePacketData, CCanvasRequestPacket::readPacketData, CCanvasRequestPacket::handle, PacketFlow.SERVERBOUND);
        registerMessage(PAINTING_UNLOAD_CANVAS, CCanvasUnloadRequestPacket.class, CCanvasUnloadRequestPacket::writePacketData, CCanvasUnloadRequestPacket::readPacketData, CCanvasUnloadRequestPacket::handle, PacketFlow.SERVERBOUND);
        registerMessage(CANVAS_SYNC, SCanvasSyncPacket.class, SCanvasSyncPacket::writePacketData, SCanvasSyncPacket::readPacketData, SCanvasSyncPacket::handle, PacketFlow.CLIENTBOUND);
        registerMessage(PALETTE_UPDATE, CPaletteUpdatePacket.class, CPaletteUpdatePacket::writePacketData, CPaletteUpdatePacket::readPacketData, CPaletteUpdatePacket::handle, PacketFlow.SERVERBOUND);
        registerMessage(PAINTING_RENAME, CSignPaintingPacket.class, CSignPaintingPacket::writePacketData, CSignPaintingPacket::readPacketData, CSignPaintingPacket::handle, PacketFlow.SERVERBOUND);
        registerMessage(CANVAS_SYNC_VIEW, SCanvasSyncViewPacket.class, SCanvasSyncViewPacket::writePacketData, SCanvasSyncViewPacket::readPacketData, SCanvasSyncViewPacket::handle, PacketFlow.CLIENTBOUND);
        registerMessage(CANVAS_REQUEST_SYNC_VIEW, CCanvasRequestViewPacket.class, CCanvasRequestViewPacket::writePacketData, CCanvasRequestViewPacket::readPacketData, CCanvasRequestViewPacket::handle, PacketFlow.SERVERBOUND);
        registerMessage(CANVAS_REMOVE, SCanvasRemovalPacket.class, SCanvasRemovalPacket::writePacketData, SCanvasRemovalPacket::readPacketData, SCanvasRemovalPacket::handle, PacketFlow.CLIENTBOUND);
        registerMessage(EASEL_SYNC, SEaselStateSyncPacket.class, SEaselStateSyncPacket::writePacketData, SEaselStateSyncPacket::readPacketData, SEaselStateSyncPacket::handle, PacketFlow.CLIENTBOUND);
        registerMessage(HISTORY_UPDATE, CCanvasHistoryActionPacket.class, CCanvasHistoryActionPacket::writePacketData, CCanvasHistoryActionPacket::readPacketData, CCanvasHistoryActionPacket::handle, PacketFlow.SERVERBOUND);
        registerMessage(ARTIST_TABLE_MODE, CArtistTableModeChangePacket.class, CArtistTableModeChangePacket::writePacketData, CArtistTableModeChangePacket::readPacketData, CArtistTableModeChangePacket::handle, PacketFlow.SERVERBOUND);
        registerMessage(HISTORY_SYNC, SCanvasHistoryActionPacket.class, SCanvasHistoryActionPacket::writePacketData, SCanvasHistoryActionPacket::readPacketData, SCanvasHistoryActionPacket::handle, PacketFlow.CLIENTBOUND);
        registerMessage(HISTORY_RESET, SEaselResetPacket.class, SEaselResetPacket::writePacketData, SEaselResetPacket::readPacketData, SEaselResetPacket::handle, PacketFlow.CLIENTBOUND);
        registerMessage(EASEL_CANVAS_INIT, SEaselCanvasInitializationPacket.class, SEaselCanvasInitializationPacket::writePacketData, SEaselCanvasInitializationPacket::readPacketData, SEaselCanvasInitializationPacket::handle, PacketFlow.CLIENTBOUND);
        registerMessage(CANVAS_REQUEST_EXPORT, CCanvasRequestExportPacket.class, CCanvasRequestExportPacket::writePacketData, CCanvasRequestExportPacket::readPacketData, CCanvasRequestExportPacket::handle, PacketFlow.SERVERBOUND);
        registerMessage(CANVAS_EXPORT, SCanvasSyncExportPacket.class, SCanvasSyncExportPacket::writePacketData, SCanvasSyncExportPacket::readPacketData, SCanvasSyncExportPacket::handle, PacketFlow.CLIENTBOUND);
        registerMessage(CANVAS_EXPORT_ERROR, SCanvasSyncExportErrorPacket.class, SCanvasSyncExportErrorPacket::writePacketData, SCanvasSyncExportErrorPacket::readPacketData, SCanvasSyncExportErrorPacket::handle, PacketFlow.CLIENTBOUND);

        event.registrar(Zetter.MOD_ID)
            .versioned(String.valueOf(MESSAGE_PROTOCOL_VERSION))
            .playBidirectional(ZetterPayload.TYPE, ZetterPayload.STREAM_CODEC, ZetterNetwork::handlePayload);
    }

    public static void sendToServer(Object packet) {
        PacketDistributor.sendToServer(new ZetterPayload(getPacketId(packet), packet));
    }

    public static void sendToPlayer(ServerPlayer player, Object packet) {
        PacketDistributor.sendToPlayer(player, new ZetterPayload(getPacketId(packet), packet));
    }

    private static <T> void registerMessage(
        int id,
        Class<T> packetClass,
        BiConsumer<T, FriendlyByteBuf> encoder,
        Function<FriendlyByteBuf, T> decoder,
        BiConsumer<T, PayloadContext> consumer,
        PacketFlow direction
    ) {
        PacketRegistration<T> registration = new PacketRegistration<>(id, packetClass, encoder, decoder, consumer, direction);
        REGISTRATIONS_BY_ID.put(id, registration);
        REGISTRATIONS_BY_CLASS.put(packetClass, registration);
    }

    @SuppressWarnings("unchecked")
    private static void handlePayload(ZetterPayload payload, IPayloadContext context) {
        PacketRegistration<Object> registration = (PacketRegistration<Object>) REGISTRATIONS_BY_ID.get(payload.packetId());

        if (registration == null) {
            Zetter.LOG.warn("Unknown Zetter packet id received: {}", payload.packetId());
            return;
        }

        if (registration.direction() != context.flow()) {
            Zetter.LOG.warn("Zetter packet {} received on wrong side: {}", payload.packetId(), context.flow());
            return;
        }

        registration.consumer().accept(payload.packet(), new PayloadContext(context));
    }

    private static int getPacketId(Object packet) {
        PacketRegistration<?> registration = REGISTRATIONS_BY_CLASS.get(packet.getClass());

        if (registration == null) {
            throw new IllegalArgumentException("Unregistered Zetter packet: " + packet.getClass().getName());
        }

        return registration.id();
    }

    @SuppressWarnings("unchecked")
    private static void writePacket(Object packet, FriendlyByteBuf buffer) {
        PacketRegistration<Object> registration = (PacketRegistration<Object>) REGISTRATIONS_BY_CLASS.get(packet.getClass());

        if (registration == null) {
            throw new IllegalArgumentException("Unregistered Zetter packet: " + packet.getClass().getName());
        }

        registration.encoder().accept(packet, buffer);
    }

    private static Object readPacket(int packetId, FriendlyByteBuf buffer) {
        PacketRegistration<?> registration = REGISTRATIONS_BY_ID.get(packetId);

        if (registration == null) {
            throw new IllegalArgumentException("Unknown Zetter packet id: " + packetId);
        }

        return registration.decoder().apply(buffer);
    }

    private record PacketRegistration<T>(
        int id,
        Class<T> packetClass,
        BiConsumer<T, FriendlyByteBuf> encoder,
        Function<FriendlyByteBuf, T> decoder,
        BiConsumer<T, PayloadContext> consumer,
        PacketFlow direction
    ) {
    }

    public record ZetterPayload(int packetId, Object packet) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<ZetterPayload> TYPE = new CustomPacketPayload.Type<>(simpleChannelRL);
        public static final StreamCodec<RegistryFriendlyByteBuf, ZetterPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> payload.write(buffer),
            ZetterPayload::read
        );

        private static ZetterPayload read(RegistryFriendlyByteBuf buffer) {
            int packetId = buffer.readVarInt();
            return new ZetterPayload(packetId, readPacket(packetId, buffer));
        }

        private void write(RegistryFriendlyByteBuf buffer) {
            buffer.writeVarInt(this.packetId);
            writePacket(this.packet, buffer);
        }

        @Override
        public CustomPacketPayload.Type<ZetterPayload> type() {
            return TYPE;
        }
    }

    public static class PayloadContext {
        private final IPayloadContext context;

        private PayloadContext(IPayloadContext context) {
            this.context = context;
        }

        public boolean isClientSide() {
            return this.context.flow() == PacketFlow.CLIENTBOUND;
        }

        public void setPacketHandled(boolean handled) {
        }

        public ServerPlayer getSender() {
            Player player = this.context.player();
            return player instanceof ServerPlayer serverPlayer ? serverPlayer : null;
        }

        public CompletableFuture<Void> enqueueWork(Runnable runnable) {
            return this.context.enqueueWork(runnable);
        }
    }
}
