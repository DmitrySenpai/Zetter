package me.dantaeusb.zetter.core;

import me.dantaeusb.zetter.Zetter;
import me.dantaeusb.zetter.capability.canvastracker.CanvasClientTracker;
import me.dantaeusb.zetter.capability.canvastracker.CanvasServerTracker;
import me.dantaeusb.zetter.capability.canvastracker.CanvasTracker;
import me.dantaeusb.zetter.capability.canvastracker.CanvasTrackerStorage;
import me.dantaeusb.zetter.capability.paintingregistry.PaintingRegistry;
import me.dantaeusb.zetter.capability.paintingregistry.PaintingRegistryStorage;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@EventBusSubscriber(modid = Zetter.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ZetterCapabilities {
    private static final String TAG_NAME_CANVAS_TRACKER = "canvasTracker";
    private static final String TAG_NAME_PAINTING_REGISTRY = "PaintingRegistry";

    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Zetter.MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<CanvasTracker>> CANVAS_TRACKER = ATTACHMENT_TYPES.register(
            "canvas_tracker",
            () -> AttachmentType.builder(ZetterCapabilities::createCanvasTracker)
                    .serialize(new IAttachmentSerializer<CompoundTag, CanvasTracker>() {
                        @Override
                        public CanvasTracker read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider provider) {
                            CanvasTracker canvasTracker = createCanvasTracker(holder);
                            if (canvasTracker.getLevel() != null && !canvasTracker.getLevel().isClientSide()) {
                                Tag canvasTrackerTag = tag.get(TAG_NAME_CANVAS_TRACKER);
                                if (canvasTrackerTag != null) {
                                    CanvasTrackerStorage.load(canvasTracker, canvasTrackerTag);
                                }
                            }
                            return canvasTracker;
                        }

                        @Override
                        public CompoundTag write(CanvasTracker canvasTracker, HolderLookup.Provider provider) {
                            CompoundTag compoundTag = new CompoundTag();
                            if (canvasTracker.getLevel() != null && !canvasTracker.getLevel().isClientSide()) {
                                compoundTag.put(TAG_NAME_CANVAS_TRACKER, CanvasTrackerStorage.save(canvasTracker));
                            }
                            return compoundTag;
                        }
                    })
                    .build()
    );

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PaintingRegistry>> PAINTING_REGISTRY = ATTACHMENT_TYPES.register(
            "painting_registry",
            () -> AttachmentType.builder(ZetterCapabilities::createPaintingRegistry)
                    .serialize(new IAttachmentSerializer<CompoundTag, PaintingRegistry>() {
                        @Override
                        public PaintingRegistry read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider provider) {
                            PaintingRegistry paintingRegistry = createPaintingRegistry(holder);
                            if (paintingRegistry.getWorld() != null && !paintingRegistry.getWorld().isClientSide()) {
                                Tag paintingRegistryTag = tag.get(TAG_NAME_PAINTING_REGISTRY);
                                if (paintingRegistryTag != null) {
                                    PaintingRegistryStorage.load(paintingRegistry, paintingRegistryTag);
                                }
                            }
                            return paintingRegistry;
                        }

                        @Override
                        public CompoundTag write(PaintingRegistry paintingRegistry, HolderLookup.Provider provider) {
                            CompoundTag compoundTag = new CompoundTag();
                            if (paintingRegistry.getWorld() != null && !paintingRegistry.getWorld().isClientSide()) {
                                compoundTag.put(TAG_NAME_PAINTING_REGISTRY, PaintingRegistryStorage.save(paintingRegistry));
                            }
                            return compoundTag;
                        }
                    })
                    .build()
    );

    public static void init(IEventBus bus) {
        ATTACHMENT_TYPES.register(bus);
    }

    @SubscribeEvent
    public static void registerCapabilityHandler(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ZetterBlockEntities.ARTIST_TABLE_BLOCK_ENTITY.get(),
                (artistTable, direction) -> direction == null || direction == Direction.UP || direction == Direction.DOWN
                        ? artistTable.getArtistTableGridContainer()
                        : null
        );
        event.registerEntity(
                Capabilities.ItemHandler.ENTITY_AUTOMATION,
                ZetterEntities.EASEL_ENTITY.get(),
                (easel, direction) -> direction == null || direction == Direction.UP || direction == Direction.DOWN
                        ? easel.getEaselContainer()
                        : null
        );
    }

    private static CanvasTracker createCanvasTracker(IAttachmentHolder holder) {
        Level level = (Level) holder;
        CanvasTracker canvasTracker = level.isClientSide() ? new CanvasClientTracker() : new CanvasServerTracker();
        canvasTracker.setLevel(level);
        return canvasTracker;
    }

    private static PaintingRegistry createPaintingRegistry(IAttachmentHolder holder) {
        Level level = (Level) holder;
        if (level.isClientSide()) {
            throw new IllegalArgumentException("Painting Registry should exist only on server in overworld");
        }
        return new PaintingRegistry(level);
    }
}
