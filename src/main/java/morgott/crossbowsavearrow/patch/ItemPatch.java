package morgott.crossbowsavearrow.patch;

import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemWeapon;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.none.ChangeActiveSlotInteraction;
import com.traktool.mixintale.api.Accessor;
import com.traktool.mixintale.api.Patch;
import com.traktool.mixintale.api.Postfix;
import com.traktool.mixintale.api.This;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Crossbows (any weapon with an Ammo stat modifier and a reload on Ability3) switch slots with the plain
 * default swap instead of their SwapFrom chain.
 * <p>
 * The vanilla crossbow SwapFrom (Weapon_Crossbow_Swap_From) branches on the Ammo stat to give the loaded
 * arrows back, and its ModifyInventory step waits for the server. Loaded ammo now stays in the item, so that
 * refund would duplicate arrows, and the branch made the client (which predicts Ammo = 0 right after equip,
 * EntityStatsToClear) and the server (which restores the stored ammo) take different paths: the server then
 * corrects the client's predicted slot change, so fast scrolling snapped back to the crossbow.
 * Item interactions are sent to the client with the item, so both sides run the same plain swap.
 */
@Patch(Item.class)
public final class ItemPatch {

    private ItemPatch() {
    }

    @Accessor("interactions")
    private static void setInteractions(@This Item self, Map<InteractionType, String> interactions) {
        throw new AssertionError();
    }

    @Postfix("processConfig")
    public static void onProcessConfig(@This Item self) {
        ItemWeapon weapon = self.getWeapon();
        Map<InteractionType, String> interactions = self.getInteractions();
        if (weapon == null || weapon.getStatModifiers() == null || !interactions.containsKey(InteractionType.Ability3)
                || !weapon.getStatModifiers().containsKey(EntityStatType.getAssetMap().getIndex("Ammo"))) {
            return;
        }
        Map<InteractionType, String> replaced = new EnumMap<>(interactions);
        replaced.put(InteractionType.SwapFrom, ChangeActiveSlotInteraction.DEFAULT_ROOT.getId());
        setInteractions(self, Collections.unmodifiableMap(replaced));
    }
}
