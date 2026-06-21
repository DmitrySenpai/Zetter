package me.dantaeusb.zetter.item.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.dantaeusb.zetter.core.Helper;
import me.dantaeusb.zetter.core.ZetterCraftingRecipes;
import me.dantaeusb.zetter.item.FrameItem;
import me.dantaeusb.zetter.item.PaintingItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * Only for frames, toggle
 */
public class FramingRecipe extends CustomRecipe {
    private final Ingredient inputFrame;
    private final Ingredient inputPainting;

    public FramingRecipe(Ingredient inputFrame, Ingredient inputPainting) {
        super(CraftingBookCategory.MISC);

        this.inputFrame = inputFrame;
        this.inputPainting = inputPainting;
    }

    @Override
    public String toString () {
        return "FramingRecipe [inputFrame=" + this.inputFrame + ", inputPainting=" + this.inputPainting + "]";
    }

    /**
     * Used to check if a recipe matches current crafting inventory
     */
    public boolean matches(CraftingInput craftingInventory, Level world) {
        ItemStack frameStack = ItemStack.EMPTY;
        ItemStack paintingStack = ItemStack.EMPTY;

        for(int i = 0; i < craftingInventory.size(); ++i) {
            if (craftingInventory.getItem(i).isEmpty()) {
                continue;
            }

            if (this.isFrameIngredient(craftingInventory.getItem(i))) {
                if (!frameStack.isEmpty()) {
                    return false;
                }

                frameStack = craftingInventory.getItem(i);
            } else if (this.isPaintingIngredient(craftingInventory.getItem(i))) {
                if (!paintingStack.isEmpty()) {
                    return false;
                }

                paintingStack = craftingInventory.getItem(i);
            } else {
                // We have something else in the grid
                return false;
            }
        }

        if (frameStack.isEmpty() || paintingStack.isEmpty()) {
            return false;
        }

        if (!FrameItem.isEmpty(frameStack) || PaintingItem.isEmpty(paintingStack)) {
            return false;
        }

        return true;
    }

    /**
     * Returns an Item that is the result of this recipe
     */
    public @NotNull ItemStack assemble(CraftingInput craftingInventory, HolderLookup.Provider provider) {
        ItemStack frameStack = ItemStack.EMPTY;
        ItemStack paintingStack = ItemStack.EMPTY;

        for(int i = 0; i < craftingInventory.size(); ++i) {
            if (this.isFrameIngredient(craftingInventory.getItem(i))) {
                if (!frameStack.isEmpty()) {
                    return ItemStack.EMPTY;
                }

                frameStack = craftingInventory.getItem(i);
            } else if (this.isPaintingIngredient(craftingInventory.getItem(i))) {
                if (!paintingStack.isEmpty()) {
                    return ItemStack.EMPTY;
                }

                paintingStack = craftingInventory.getItem(i);
            }
        }

        if (frameStack.isEmpty() || paintingStack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        if (!FrameItem.isEmpty(frameStack) || PaintingItem.isEmpty(paintingStack)) {
            return ItemStack.EMPTY;
        }

        ItemStack outStack = frameStack.copy();
        outStack.setCount(1);

        CompoundTag compoundTag = Helper.getCustomData(paintingStack);
        if (compoundTag == null) {
            return ItemStack.EMPTY;
        }

        Helper.setCustomData(outStack, compoundTag);

        return outStack;
    }

    private boolean isFrameIngredient(ItemStack stack) {
        return this.inputFrame.test(stack) || stack.getItem() instanceof FrameItem;
    }

    private boolean isPaintingIngredient(ItemStack stack) {
        return this.inputPainting.test(stack) || (stack.getItem() instanceof PaintingItem && !(stack.getItem() instanceof FrameItem));
    }

    /**
     * @return
     */
    public RecipeSerializer<?> getSerializer() {
        return ZetterCraftingRecipes.FRAMING.get();
    }

    /**
     * Used to determine if this recipe can fit in a grid of the given width/height
     */
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    public static class Serializer implements RecipeSerializer<FramingRecipe> {
        public static final MapCodec<FramingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC_NONEMPTY.fieldOf("frame").forGetter(recipe -> recipe.inputFrame),
            Ingredient.CODEC_NONEMPTY.fieldOf("painting").forGetter(recipe -> recipe.inputPainting)
        ).apply(instance, FramingRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, FramingRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.inputFrame,
            Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.inputPainting,
            FramingRecipe::new
        );

        @Override
        public MapCodec<FramingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, FramingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
