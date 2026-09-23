package morgott.crossbowsavearrow;

import com.hypixel.hytale.server.core.entity.StatModifiersManager;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.logging.Level;

public class CrossbowSaveArrow extends JavaPlugin {

    public CrossbowSaveArrow(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        // MixinTale relocates patch handlers into the target under a "mixintale$" prefix
        boolean patched = Arrays.stream(StatModifiersManager.class.getDeclaredMethods())
                .anyMatch(m -> m.getName().startsWith("mixintale$"));
        if (!patched) {
            getLogger().at(Level.SEVERE).log("[CrossbowSaveArrow] StatModifiersManager is not patched - is MixinTale in EarlyPlugins? Check [MixinTale] log lines.");
            return;
        }
        getLogger().at(Level.INFO).log("[CrossbowSaveArrow] Loaded! Each crossbow keeps its own loaded ammo.");
    }
}
