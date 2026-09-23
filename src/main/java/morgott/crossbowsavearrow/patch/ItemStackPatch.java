package morgott.crossbowsavearrow.patch;

import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.traktool.mixintale.api.Accessor;
import com.traktool.mixintale.api.Arg;
import com.traktool.mixintale.api.Patch;
import com.traktool.mixintale.api.Replace;
import com.traktool.mixintale.api.This;
import org.bson.BsonDocument;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * isEquivalentType() ignores the stored ammo key.
 * <p>
 * The held crossbow's stored ammo is updated on every shot and reload. Without this, that write makes the
 * running interaction see a different held item and stop (Interaction.tickInternal: ItemChanged), and makes
 * the server wipe the Ammo stat as if another weapon was equipped (LegacyHotbarChangeStatSystem).
 */
@Patch(ItemStack.class)
public final class ItemStackPatch {

    private static final String LOADED_AMMO_KEY = "LoadedAmmo";

    private ItemStackPatch() {
    }

    @Accessor("metadata")
    private static BsonDocument metadata(@This ItemStack self) {
        throw new AssertionError();
    }

    @Replace("isEquivalentType")
    public static boolean isEquivalentType(@This ItemStack self, @Arg(0) ItemStack other) {
        if (other == null || !self.getItemId().equals(other.getItemId())) {
            return false;
        }
        BsonDocument a = metadata(self);
        BsonDocument b = metadata(other);
        return Objects.equals(a, b) || Objects.equals(withoutAmmo(a), withoutAmmo(b));
    }

    @Nullable
    private static BsonDocument withoutAmmo(@Nullable BsonDocument metadata) {
        if (metadata == null || !metadata.containsKey(LOADED_AMMO_KEY)) {
            return metadata;
        }
        BsonDocument clean = metadata.clone();
        clean.remove(LOADED_AMMO_KEY);
        return clean.isEmpty() ? null : clean;
    }
}
