package morgott.crossbowsavearrow.patch;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemWeapon;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.RespawnSystems;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.traktool.mixintale.api.Arg;
import com.traktool.mixintale.api.Patch;
import com.traktool.mixintale.api.Postfix;

/**
 * Respawn resets every stat, including the held crossbow's Ammo. Only shots and reloads may change a
 * crossbow's stored ammo, so the Ammo stat is set back to the value stored in the held crossbow.
 */
@Patch(RespawnSystems.ResetStatsRespawnSystem.class)
public final class RespawnResetStatsPatch {

    private RespawnResetStatsPatch() {
    }

    @Postfix("onComponentRemoved")
    public static void onAfterReset(
            @Arg(0) Ref<EntityStore> ref,
            @Arg(1) DeathComponent component,
            @Arg(2) Store<EntityStore> store,
            @Arg(3) CommandBuffer<EntityStore> commandBuffer
    ) {
        EntityStatMap statMap = store.getComponent(ref, EntityStatMap.getComponentType());
        InventoryComponent.Hotbar hotbar = store.getComponent(ref, InventoryComponent.Hotbar.getComponentType());
        int ammoIndex = DefaultEntityStatTypes.getAmmo();
        EntityStatValue ammoStat = statMap != null ? statMap.get(ammoIndex) : null;
        ItemStack item = hotbar != null ? hotbar.getActiveItem() : null;
        if (ammoStat == null || ItemStack.isEmpty(item)) {
            return;
        }
        ItemWeapon weapon = item.getItem().getWeapon();
        Float stored = item.getFromMetadataOrNull("LoadedAmmo", Codec.FLOAT);
        if (stored == null || weapon == null || weapon.getStatModifiers() == null || !weapon.getStatModifiers().containsKey(ammoIndex)
                || !item.getItem().getInteractions().containsKey(InteractionType.Ability3)) {
            return;
        }
        statMap.setStatValue(EntityStatMap.Predictable.NONE, ammoIndex, Math.min(stored, ammoStat.getMax()));
    }
}
