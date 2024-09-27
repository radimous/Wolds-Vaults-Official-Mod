package xyz.iwolfking.woldsvaults.mixins.pnc;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import iskallia.vault.init.ModItems;
import me.desht.pneumaticcraft.common.recipes.machine.PressureChamberRecipeImpl;
import me.desht.pneumaticcraft.common.recipes.machine.PressureDisenchantingRecipe;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Mixin(value = PressureDisenchantingRecipe.class, remap = false)
public abstract class MixinDisenchantingPressureRecipe extends PressureChamberRecipeImpl {
    @Shadow protected abstract boolean blacklisted(ItemStack stack);

    public MixinDisenchantingPressureRecipe(ResourceLocation id, List<Ingredient> inputs, float pressureRequired, ItemStack... outputs) {
        super(id, inputs, pressureRequired, outputs);
    }

    /**
     * @author iwolfking
     * @reason Add Omega Pog to recipe
     */
    @Overwrite
    public Collection<Integer> findIngredients(@Nonnull IItemHandler chamberHandler) {
        int bookSlot = -1;
        int itemSlot = -1;
        int pogSlot = -1;

        for (int i = 0; i < chamberHandler.getSlots(); ++i) {
            ItemStack stack = chamberHandler.getStackInSlot(i);
            if(stack.getItem() == ModItems.ECHO_POG) {
                pogSlot = i;
            }
            if (stack.getItem() == Items.BOOK) {
                bookSlot = i;
            } else {
                int minEnchantments = stack.getItem() == Items.ENCHANTED_BOOK ? 2 : 1;
                if (!this.blacklisted(stack) && EnchantmentHelper.getEnchantments(stack).size() >= minEnchantments) {
                    itemSlot = i;
                }
            }

            if (bookSlot >= 0 && itemSlot >= 0 && pogSlot >= 0) {
                return ImmutableList.of(bookSlot, itemSlot, pogSlot);
            }
        }

        return Collections.emptyList();
    }

    /**
     * @author iwolfking
     * @reason Add Omega Pog to recipe
     */
    @Overwrite
    public NonNullList<ItemStack> craftRecipe(@Nonnull IItemHandler chamberHandler, List<Integer> ingredientSlots, boolean simulate) {
        ItemStack book = chamberHandler.extractItem((Integer) ingredientSlots.get(0), 1, simulate);
        ItemStack enchantedStack = chamberHandler.extractItem((Integer) ingredientSlots.get(1), 1, simulate);
        ItemStack pog = chamberHandler.extractItem((Integer) ingredientSlots.get(2), 1, simulate);
        if (!book.isEmpty() && !enchantedStack.isEmpty() && !pog.isEmpty()) {
            Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(enchantedStack);
            List<Enchantment> l = new ArrayList(enchantments.keySet());
            Enchantment strippedEnchantment = (Enchantment) l.get(ThreadLocalRandom.current().nextInt(l.size()));
            int level = (Integer) enchantments.get(strippedEnchantment);
            enchantments.remove(strippedEnchantment);
            if (enchantedStack.getItem() == Items.ENCHANTED_BOOK) {
                enchantedStack = new ItemStack(Items.ENCHANTED_BOOK);
            }

            EnchantmentHelper.setEnchantments(enchantments, enchantedStack);
            ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);
            EnchantmentHelper.setEnchantments(ImmutableMap.of(strippedEnchantment, level), enchantedBook);
            return NonNullList.of(ItemStack.EMPTY, new ItemStack[]{enchantedBook, enchantedStack});
        } else {
            return NonNullList.create();
        }
    }


    /**
     * @author iwolfking
     * @reason Add Omega Pog to recipe
     */
    @Overwrite
    public List<Ingredient> getInputsForDisplay() {
        ItemStack pick = new ItemStack(Items.DIAMOND_PICKAXE);
        pick.enchant(Enchantments.BLOCK_FORTUNE, 1);
        ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);
        enchantedBook.enchant(Enchantments.BLOCK_FORTUNE, 1);
        enchantedBook.enchant(Enchantments.BLOCK_EFFICIENCY, 1);
        return ImmutableList.of(Ingredient.of(new ItemStack[]{pick, enchantedBook}), Ingredient.of(new ItemLike[]{Items.BOOK}), Ingredient.of(ModItems.ECHO_POG));
    }

    /**
     * @author iwolfking
     * @reason Add Omega Pog to recipe
     */
    @Overwrite
    public List<List<ItemStack>> getResultsForDisplay() {
        ItemStack pick = new ItemStack(Items.DIAMOND_PICKAXE);
        ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);
        enchantedBook.enchant(Enchantments.BLOCK_EFFICIENCY, 1);
        ItemStack resultBook = new ItemStack(Items.ENCHANTED_BOOK);
        resultBook.enchant(Enchantments.BLOCK_FORTUNE, 1);
        return ImmutableList.of(ImmutableList.of(pick, enchantedBook), ImmutableList.of(resultBook));
    }

    /**
     * @author iwolfking
     * @reason Add Omega Pog to recipe
     */
    @Overwrite
    public boolean isValidInputItem(ItemStack stack) {
        return stack.getItem() == ModItems.ECHO_POG || stack.getItem() == Items.BOOK || stack.getItem() != Items.ENCHANTED_BOOK && EnchantmentHelper.getEnchantments(stack).size() > 0;
    }


}
