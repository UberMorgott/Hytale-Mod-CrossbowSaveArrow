package morgott.crossbowsavearrow.patch;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemWeapon;
import com.hypixel.hytale.server.core.entity.StatModifiersManager;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.traktool.mixintale.api.Accessor;
import com.traktool.mixintale.api.Arg;
import com.traktool.mixintale.api.Patch;
import com.traktool.mixintale.api.Postfix;
import com.traktool.mixintale.api.Prefix;
import com.traktool.mixintale.api.This;
import it.unimi.dsi.fastutil.ints.IntSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Per-crossbow loaded ammo for players.
 * <p>
 * The ammo lives in the crossbow item's metadata. While a crossbow is held, every change of the Ammo stat
 * (shots, reloads) is written through to the held item, so the item always carries its current ammo wherever
 * it goes (other slots, chests, drops, other players). When a crossbow enters the hand, the stat is set to the
 * value stored in that item (0 if none) instead of the vanilla EntityStatsToClear wipe, so no path can restore
 * or duplicate ammo. The respawn stat reset is undone by RespawnResetStatsPatch.
 * <p>
 * The stored value is sent with Predictable.NONE: the client cannot predict it (it only predicts the vanilla
 * EntityStatsToClear wipe on equip), so it must apply it as authoritative.
 * <p>
 * MixinTale relocates this class into StatModifiersManager and forbids instance fields there, so the
 * per-manager state is kept in a weak map (JDK types only: the game's class loader cannot see mod classes).
 */
@Patch(StatModifiersManager.class)
public final class StatModifiersManagerPatch {

    private static final String LOADED_AMMO_KEY = "LoadedAmmo";

    // State indexes: Boolean player, Boolean recalculating, Float ammo before recalculation, Byte slot, String weaponId, Float stored
    private static final int PLAYER = 0;
    // Modifiers were recalculated in this call, so max ammo matches the item in hand
    private static final int RECALCULATING = 1;
    // Ammo before this call recalculated modifiers for a newly equipped item
    private static final int AMMO_BEFORE = 2;
    // Held crossbow that owns the stat (-1 / null = none in hand)
    private static final int SLOT = 3;
    private static final int WEAPON_ID = 4;
    // Value last stored in the held crossbow
    private static final int STORED = 5;

    private static final Map<StatModifiersManager, Object[]> STATES = Collections.synchronizedMap(new WeakHashMap<>());

    private StatModifiersManagerPatch() {
    }

    @Accessor("recalculate")
    private static boolean recalculate(@This StatModifiersManager self) {
        throw new AssertionError();
    }

    @Accessor("statsToClear")
    private static IntSet statsToClear(@This StatModifiersManager self) {
        throw new AssertionError();
    }

    @Prefix("recalculateEntityStatModifiers")
    public static void onBeforeRecalculate(
            @This StatModifiersManager self,
            @Arg(0) Ref<EntityStore> ref,
            @Arg(1) EntityStatMap statMap,
            @Arg(2) ComponentAccessor<EntityStore> componentAccessor
    ) {
        boolean player = componentAccessor.getComponent(ref, InventoryComponent.Hotbar.getComponentType()) != null
                && componentAccessor.getComponent(ref, Player.getComponentType()) != null;
        Object[] state = STATES.get(self);
        if (!player) {
            if (state != null) {
                state[PLAYER] = false;
            }
            return;
        }
        if (state == null) {
            state = new Object[]{false, false, 0f, (byte) -1, null, 0f};
            STATES.put(self, state);
        }
        int ammoIndex = DefaultEntityStatTypes.getAmmo();
        EntityStatValue ammoStat = statMap.get(ammoIndex);
        InventoryComponent.Hotbar hotbar = componentAccessor.getComponent(ref, InventoryComponent.Hotbar.getComponentType());
        if (heldCrossbow(ref, componentAccessor, hotbar, ammoIndex) != null) {
            // A held crossbow's ammo is set from the item by this patch instead of being wiped on equip
            statsToClear(self).remove(ammoIndex);
        }
        state[PLAYER] = true;
        state[RECALCULATING] = recalculate(self);
        state[AMMO_BEFORE] = ammoStat != null ? ammoStat.get() : 0f;
    }

    @Postfix("recalculateEntityStatModifiers")
    public static void onAfterRecalculate(
            @This StatModifiersManager self,
            @Arg(0) Ref<EntityStore> ref,
            @Arg(1) EntityStatMap statMap,
            @Arg(2) ComponentAccessor<EntityStore> componentAccessor
    ) {
        Object[] state = STATES.get(self);
        if (state == null || !(Boolean) state[PLAYER]) {
            return;
        }

        InventoryComponent.Hotbar hotbar = componentAccessor.getComponent(ref, InventoryComponent.Hotbar.getComponentType());
        int ammoIndex = DefaultEntityStatTypes.getAmmo();
        EntityStatValue ammoStat = statMap.get(ammoIndex);
        if (hotbar == null || ammoStat == null) {
            return;
        }

        ItemContainer container = hotbar.getInventory();
        byte slot = hotbar.getActiveSlot();
        ItemStack item = heldCrossbow(ref, componentAccessor, hotbar, ammoIndex);
        String weaponId = item != null ? item.getItemId() : null;
        float stored = item != null ? storedAmmo(item) : 0;
        byte lastSlot = (Byte) state[SLOT];
        String lastWeaponId = (String) state[WEAPON_ID];
        float lastStored = (Float) state[STORED];

        if (item != null && slot == lastSlot && weaponId.equals(lastWeaponId) && stored == lastStored) {
            // Write-through: the held crossbow always stores its current ammo
            float ammo = ammoStat.get();
            if (ammo != stored) {
                write(container, slot, item, ammo);
                state[STORED] = ammo;
            }
            return;
        }

        if (lastWeaponId != null) {
            // The previous crossbow left the hand; save a shot or reload that happened since the last write
            float ammoBefore = (Float) state[AMMO_BEFORE];
            ItemStack previous = container.getItemStack(lastSlot);
            if (lastSlot != slot && previous != null && lastWeaponId.equals(previous.getItemId())
                    && storedAmmo(previous) == lastStored && ammoBefore != lastStored) {
                write(container, lastSlot, previous, ammoBefore);
            }
            state[SLOT] = (byte) -1;
            state[WEAPON_ID] = null;
        }

        // Max ammo comes from the new item's modifiers; wait until they have been recalculated
        if (item == null || !(Boolean) state[RECALCULATING] && !weaponId.equals(lastWeaponId)) {
            return;
        }
        state[SLOT] = slot;
        state[WEAPON_ID] = weaponId;
        state[STORED] = stored;
        // The client wiped Ammo on equip (EntityStatsToClear is in the item packet): minimize first so the Set
        // below is a real change and gets sent even when the server value already matches
        statMap.minimizeStatValue(EntityStatMap.Predictable.NONE, ammoIndex);
        statMap.setStatValue(EntityStatMap.Predictable.NONE, ammoIndex, Math.min(stored, ammoStat.getMax()));
    }

    private static float storedAmmo(@Nonnull ItemStack item) {
        Float saved = item.getFromMetadataOrNull(LOADED_AMMO_KEY, Codec.FLOAT);
        return saved != null ? saved : 0;
    }

    private static void write(@Nonnull ItemContainer container, short slot, @Nonnull ItemStack item, float ammo) {
        container.setItemStackForSlot(slot, item.withMetadata(LOADED_AMMO_KEY, Codec.FLOAT, ammo > 0 ? ammo : null));
    }

    // Crossbow = weapon with an Ammo stat modifier and a reload (Ability3), so modded crossbows match too
    @Nullable
    private static ItemStack heldCrossbow(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull ComponentAccessor<EntityStore> componentAccessor,
            @Nonnull InventoryComponent.Hotbar hotbar,
            int ammoIndex
    ) {
        InventoryComponent.Tool tool = componentAccessor.getComponent(ref, InventoryComponent.Tool.getComponentType());
        if (tool != null && tool.isUsingToolsItem()) {
            return null;
        }

        ItemStack item = hotbar.getActiveItem();
        if (ItemStack.isEmpty(item)) {
            return null;
        }
        Item type = item.getItem();
        ItemWeapon weapon = type.getWeapon();
        if (weapon == null || weapon.getStatModifiers() == null || !weapon.getStatModifiers().containsKey(ammoIndex)
                || !type.getInteractions().containsKey(InteractionType.Ability3)) {
            return null;
        }
        return item;
    }
}
