package xyz.iwolfking.woldsvaults.mixins.vaulthunters.compat.lightmanscurrency;

import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import iskallia.vault.block.ShopPedestalBlock;
import iskallia.vault.block.entity.ShopPedestalBlockTile;
import iskallia.vault.container.oversized.OverSizedItemStack;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.objective.Objective;
import iskallia.vault.core.vault.objective.Objectives;
import iskallia.vault.core.vault.objective.ParadoxObjective;
import iskallia.vault.event.event.ShopPedestalPriceEvent;
import iskallia.vault.util.InventoryUtil;
import iskallia.vault.world.data.ServerVaults;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.items.CapabilityItemHandler;
import org.spongepowered.asm.mixin.*;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "lightmanscurrency")
        }
)
@Mixin(value = ShopPedestalBlock.class, remap = false)
public class MixinShopPedestalBlock extends Block implements EntityBlock, GameMasterBlock {


    @Shadow @Final public static BooleanProperty ACTIVE;

    public MixinShopPedestalBlock(Properties p_49795_) {
        super(p_49795_);
    }



    /**
     * @author iwolfking
     * @reason Handle shop pedestal to extend coin definitions
     */
    @Overwrite
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        BlockEntity var8 = worldIn.getBlockEntity(pos);
        ItemStack c;
        if (var8 instanceof ShopPedestalBlockTile tile) {
            if(ServerVaults.get(worldIn).isPresent()) {
                System.out.println("Player is in vault.");
                Optional<Vault> vaultOpt = ServerVaults.get(worldIn);

                if(vaultOpt.isPresent()) {
                    System.out.println("Vault is present.");
                    Vault vault = vaultOpt.get();

                    Objective.ObjList objectives = vault.get(Vault.OBJECTIVES).get(Objectives.LIST);
                    for(Objective obj : objectives) {
                        System.out.println(obj.getKey());
                        if(obj instanceof ParadoxObjective paradoxObjective) {
                            System.out.println("Has a Paradox objective.");
                            if(paradoxObjective.get(ParadoxObjective.TYPE).equals(ParadoxObjective.Type.BUILD)) {
                                System.out.println("Paradox build mode detected.");
                                return InteractionResult.FAIL;
                            }
                        }
                    }
                }
            }
            if (tile.isInitialized() && handIn == InteractionHand.MAIN_HAND) {
                c = tile.getOfferStack();
                if (!c.isEmpty()) {
                    ShopPedestalPriceEvent event = new ShopPedestalPriceEvent(player, c, tile.getCurrencyStack());
                    MinecraftForge.EVENT_BUS.post(event);
                    ItemStack currency = event.getCost();
                    return (InteractionResult) player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, (Direction) null).map((itemHandler) -> {
                        List<InventoryUtil.ItemAccess> allItems = List.of();
                        if (!player.isCreative()) {
                            allItems = InventoryUtil.findAllItems(player);
                            if(!woldsVaults$lightmansCurrencyExtract(player, currency)) {
                                if (worldIn.isClientSide) {
                                    player.displayClientMessage(new TranslatableComponent("message.the_vault.shop_pedestal.fail", new Object[]{currency.getHoverName()}), true);
                                }

                                return InteractionResult.sidedSuccess(worldIn.isClientSide);
                            }
                        }


                        if (!worldIn.isClientSide) {
                            if (!player.isCreative()) {
                                BlockState inactiveState = (BlockState) state.setValue(ACTIVE, false);
                                tile.setRemoved();
                                worldIn.setBlockAndUpdate(pos, inactiveState);
                            }
                            //ItemHandlerHelper.giveItemToPlayer(player, c.copy());
                            popResource(worldIn, pos, c.copy());
                            worldIn.playSound((Player) null, pos, SoundEvents.AMETHYST_BLOCK_STEP, SoundSource.BLOCKS, 1.0F, 1.0F);
                        } else {
                            if (!player.getAbilities().instabuild) {
                                tile.setRemoved();
                            }

                            player.displayClientMessage(new TranslatableComponent("message.the_vault.shop_pedestal.purchase", new Object[]{c.getCount(), c.getHoverName(), currency.getCount(), currency.getHoverName()}), true);
                        }

                        return InteractionResult.sidedSuccess(worldIn.isClientSide);
                    }).orElse(InteractionResult.PASS);
                }

                return InteractionResult.PASS;
            }
        }

        if (player.getAbilities().instabuild) {
            ItemStack o = player.getItemInHand(InteractionHand.MAIN_HAND);
            c = player.getItemInHand(InteractionHand.OFF_HAND);
            if (!c.isEmpty() && !o.isEmpty()) {
                worldIn.setBlockAndUpdate(pos, (BlockState) state.setValue(ACTIVE, true));
                BlockEntity var10 = worldIn.getBlockEntity(pos);
                if (var10 instanceof ShopPedestalBlockTile) {
                    ShopPedestalBlockTile tile = (ShopPedestalBlockTile) var10;
                    tile.setOffer(o.copy(), OverSizedItemStack.of(c.copy()));
                    tile.setChanged();
                    return InteractionResult.sidedSuccess(worldIn.isClientSide);
                }

                worldIn.setBlockAndUpdate(pos, (BlockState) state.setValue(ACTIVE, false));
            }
        }

        return InteractionResult.PASS;
    }




    @Unique
    private boolean woldsVaults$lightmansCurrencyExtract(Player player, ItemStack costStack) {
        CoinValue cValue = CoinValue.fromItemOrValue(costStack.getItem(), costStack.getCount(), 81L);
        return (MoneyUtil.ProcessPayment(player.getInventory(), player, cValue));
    }


    /**
     * @author iwolfking
     * @reason Could not tell you to be honest, but I'm not touching it
     */
    @Overwrite
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return (Boolean) pState.getValue(ACTIVE) ? new ShopPedestalBlockTile(pPos, pState) : null;
    }


}
