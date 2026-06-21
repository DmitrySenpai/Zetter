package me.dantaeusb.zetter.item.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.dantaeusb.zetter.core.Helper;
import me.dantaeusb.zetter.core.ZetterCraftingRecipes;
import me.dantaeusb.zetter.core.ZetterItems;
import me.dantaeusb.zetter.item.FrameItem;
import me.dantaeusb.zetter.item.PaintingItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
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
public class UnframingRecipe extends CustomRecipe {
    private final Ingredient inputFrame;

    public UnframingRecipe(Ingredient inputFrame) {
        super(CraftingBookCategory.MISC);

        this.inputFrame = inputFrame;
    }

    @Override
    public String toString () {
        return "UnframingRecipe [inputFrame=" + this.inputFrame + "]";
    }

    /**
     * Used to check if a recipe matches current crafting inventory
     * @todo: [LOW] Maybe we can just extend ShapelessRecipe
     */
    public boolean matches(CraftingInput craftingInventory, Level world) {
        ItemStack frameStack = ItemStack.EMPTY;

        for(int i = 0; i < craftingInventory.size(); ++i) {
            if (craftingInventory.getItem(i).isEmpty()) {
                continue;
            }

            if (this.isFrameIngredient(craftingInventory.getItem(i))) {
                if (!frameStack.isEmpty()) {
                    // We already found frame
                    return false;
                }

                frameStack = craftingInventory.getItem(i);
            } else {
                // We have something else in the grid
                return false;
            }
        }

        return !frameStack.isEmpty() && PaintingItem.getPaintingCode(frameStack) != null;
    }

    public NonNullList<ItemStack> getRemainingItems(CraftingInput inv) {
        NonNullList<ItemStack> remainingItems = NonNullList.withSize(inv.size(), ItemStack.EMPTY);

        for(int i = 0; i < remainingItems.size(); ++i) {
            ItemStack stackInSlot = inv.getItem(i);

            // @todo: do we need containerItem?
            /*if (stackInSlot.hasContainerItem()) {
                remainingItems.set(i, stackInSlot.getContainerItem());
            } else*/
            if (stackInSlot.getItem() instanceof FrameItem) {
                Item keepItem = stackInSlot.getItem();
                ItemStack keepStack = new ItemStack(keepItem);
                keepStack.setCount(1);
                remainingItems.set(i, keepStack);
                break;
            }
        }

        return remainingItems;
    }

    /**
     * Returns an Item that is the result of this recipe
     */
    public @NotNull ItemStack assemble(CraftingInput craftingInventory, HolderLookup.Provider provider) {
        ItemStack frameStack = ItemStack.EMPTY;

        for(int i = 0; i < craftingInventory.size(); ++i) {
            if (this.isFrameIngredient(craftingInventory.getItem(i))) {
                if (!frameStack.isEmpty()) {
                    return ItemStack.EMPTY;
                }

                frameStack = craftingInventory.getItem(i);
            }
        }

        if (!frameStack.isEmpty() && Helper.hasCustomData(frameStack)) {
            ItemStack outStack = new ItemStack(ZetterItems.PAINTING.get());
            CompoundTag compoundnbt = Helper.getCustomData(frameStack);
            Helper.setCustomData(outStack, compoundnbt);
            return outStack;
        } else {
            return ItemStack.EMPTY;
        }
    }

    private boolean isFrameIngredient(ItemStack stack) {
        return this.inputFrame.test(stack) || stack.getItem() instanceof FrameItem;
    }

    /**
     * @return
     */
    public RecipeSerializer<?> getSerializer() {
        return ZetterCraftingRecipes.UNFRAMING.get();
    }

    /**
     * Used to determine if this recipe can fit in a grid of the given width/height
     */
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 1;
    }

    public static class Serializer implements RecipeSerializer<UnframingRecipe> {
        public static final MapCodec<UnframingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC_NONEMPTY.fieldOf("frame").forGetter(recipe -> recipe.inputFrame)
        ).apply(instance, UnframingRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, UnframingRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.inputFrame,
            UnframingRecipe::new
        );

        @Override
        public MapCodec<UnframingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, UnframingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
