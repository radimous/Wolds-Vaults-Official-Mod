package xyz.iwolfking.woldsvaults.mixins;

import iskallia.vault.init.ModConfigs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.config.*;


@Mixin(ModConfigs.class)
public class MixinModConfigs {

    @Inject(method = "register", at = @At("TAIL"), remap = false)
    private static void onReloadConfigs(CallbackInfo ci) {
        xyz.iwolfking.woldsvaults.init.ModConfigs.GEM_BOX = (GemBoxConfig) (new GemBoxConfig()).readConfig();
        xyz.iwolfking.woldsvaults.init.ModConfigs.SUPPLY_BOX = (SupplyBoxConfig) (new SupplyBoxConfig()).readConfig();
        xyz.iwolfking.woldsvaults.init.ModConfigs.AUGMENT_BOX = (AugmentBoxConfig) (new AugmentBoxConfig()).readConfig();
        xyz.iwolfking.woldsvaults.init.ModConfigs.INSCRIPTION_BOX = (InscriptionBoxConfig) (new InscriptionBoxConfig()).readConfig();
        xyz.iwolfking.woldsvaults.init.ModConfigs.OMEGA_BOX = (OmegaBoxConfig) (new OmegaBoxConfig()).readConfig();
        xyz.iwolfking.woldsvaults.init.ModConfigs.UNHINGED_SCAVENGER = (UnhingedScavengerConfig) (new UnhingedScavengerConfig().readConfig());
        xyz.iwolfking.woldsvaults.init.ModConfigs.HAUNTED_BRAZIERS = (HauntedBraziersConfig) (new HauntedBraziersConfig().readConfig());
        xyz.iwolfking.woldsvaults.init.ModConfigs.ENCHANTED_ELIXIR = (EnchantedElixirConfig) (new EnchantedElixirConfig().readConfig());
        xyz.iwolfking.woldsvaults.init.ModConfigs.VAULT_AMULET = (VaultAmuletConfig) (new VaultAmuletConfig().readConfig());
    }
}
