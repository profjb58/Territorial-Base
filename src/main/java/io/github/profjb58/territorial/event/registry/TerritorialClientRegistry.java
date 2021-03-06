package io.github.profjb58.territorial.event.registry;

import io.github.profjb58.territorial.client.gui.KeyringScreen;
import io.github.profjb58.territorial.inventory.ItemInventory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.object.builder.v1.client.model.FabricModelPredicateProviderRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class TerritorialClientRegistry {

    public static void registerAll() {
        ScreenRegistry.register(TerritorialRegistry.KEYRING_SCREEN_HANDLER_TYPE, KeyringScreen::new);
        registerItemPredicates();

        BlockRenderLayerMap.INSTANCE.putBlock(TerritorialRegistry.LASER_TRANSMITTER, RenderLayer.getTranslucent());
    }

    private static void registerItemPredicates() {
        // Keyring item predicate
        // TODO - Maybe optimize this later
        FabricModelPredicateProviderRegistry.register(TerritorialRegistry.KEYRING, new Identifier("stage"), (itemStack, clientWorld, livingEntity) -> {
            ItemInventory keyringInventory = new ItemInventory(itemStack, 9);
            keyringInventory.loadFromAttachedItemTag();
            int numKeys = keyringInventory.getAmountOfFilledSlots();
            return numKeys / 9F;
        });
    }
}
