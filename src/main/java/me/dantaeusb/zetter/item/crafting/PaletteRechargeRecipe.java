package me.dantaeusb.zetter.item.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.dantaeusb.zetter.core.Helper;
import me.dantaeusb.zetter.core.ZetterCraftingRecipes;
import me.dantaeusb.zetter.core.ZetterItems;
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
public class PaletteRechargeRecipe extends CustomRecipe {
    private final Ingredient inputPalette;
    private final Ingredient inputRecharge;

    public PaletteRechargeRecipe(Ingredient inputPalette, Ingredient inputRecharge) {
        super(CraftingBookCategory.MISC);

        this.inputPalette = inputPalette;
        this.inputRecharge = inputRecharge;
    }

    @Override
    public String toString () {
        return "PaletteRechargeRecipe [inputPalette=" + this.inputPalette + ", inputRecharge=" + this.inputRecharge + "]";
    }

    /**
     * Used to check if a recipe matches current crafting inventory
     */
    public boolean matches(CraftingInput craftingInventory, Level world) {
        ItemStack paletteStack = ItemStack.EMPTY;
        ItemStack rechargeStack = ItemStack.EMPTY;

        for(int i = 0; i < craftingInventory.size(); ++i) {
            if (craftingInventory.getItem(i).isEmpty()) {
                continue;
            }

            if (this.inputPalette.test(craftingInventory.getItem(i))) {
                if (!paletteStack.isEmpty()) {
                    return false;
                }

                paletteStack = craftingInventory.getItem(i);
            } else if (this.inputRecharge.test(craftingInventory.getItem(i))) {
                if (!rechargeStack.isEmpty()) {
                    return false;
                }

                rechargeStack = craftingInventory.getItem(i);
            }
        }

        return (!paletteStack.isEmpty() && Helper.hasCustomData(paletteStack) && paletteStack.getDamageValue() > 0) && !rechargeStack.isEmpty();
    }

    /**
     * Returns an Item that is the result of this recipe
     */
    public @NotNull ItemStack assemble(CraftingInput craftingInventory, HolderLookup.Provider provider) {
        ItemStack paletteStack = ItemStack.EMPTY;
        ItemStack rechargeStack = ItemStack.EMPTY;

        for(int i = 0; i < craftingInventory.size(); ++i) {
            if (this.inputPalette.test(craftingInventory.getItem(i))) {
                if (!paletteStack.isEmpty()) {
                    return ItemStack.EMPTY;
                }

                paletteStack = craftingInventory.getItem(i);
            } else if (this.inputRecharge.test(craftingInventory.getItem(i))) {
                if (!rechargeStack.isEmpty()) {
                    return ItemStack.EMPTY;
                }

                rechargeStack = craftingInventory.getItem(i);
            }
        }

        if ((!paletteStack.isEmpty() && Helper.hasCustomData(paletteStack) && paletteStack.getDamageValue() > 0) && !rechargeStack.isEmpty()) {
            ItemStack outStack = paletteStack.copy();

            int newDamage = paletteStack.getDamageValue();

            if (rechargeStack.is(ZetterItems.PALETTE.get()) && rechargeStack.getDamageValue() > 0) {
                newDamage -= (rechargeStack.getMaxDamage() - rechargeStack.getDamageValue());
                newDamage = Math.max(newDamage, 0);
            } else {
                newDamage = 0;
            }

            CompoundTag compoundnbt = Helper.getCustomData(paletteStack);
            Helper.setCustomData(outStack, compoundnbt);
            outStack.setDamageValue(newDamage);

            return outStack;
        } else {
            return ItemStack.EMPTY;
        }
    }

    /**
     * @todo: Not sure if that's the right thing use CRAFTING_SPECIAL_BOOKCLONING here
     * @return
     */
    public RecipeSerializer<?> getSerializer() {
        return ZetterCraftingRecipes.PALETTE_RECHARGE.get();
    }

    /**
     * Used to determine if this recipe can fit in a grid of the given width/height
     */
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    public static class Serializer implements RecipeSerializer<PaletteRechargeRecipe> {
        public static final MapCodec<PaletteRechargeRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC_NONEMPTY.fieldOf("palette").forGetter(recipe -> recipe.inputPalette),
            Ingredient.CODEC_NONEMPTY.fieldOf("recharge").forGetter(recipe -> recipe.inputRecharge)
        ).apply(instance, PaletteRechargeRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, PaletteRechargeRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.inputPalette,
            Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.inputRecharge,
            PaletteRechargeRecipe::new
        );

        @Override
        public MapCodec<PaletteRechargeRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, PaletteRechargeRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
