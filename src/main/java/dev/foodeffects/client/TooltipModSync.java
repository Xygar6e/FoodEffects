package dev.foodeffects.client;

import dev.foodeffects.FoodEffects;
import dev.foodeffects.FoodEffectsConfig;
import net.fabricmc.loader.api.FabricLoader;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Soft integration with Food Effect Tooltips (by DaFuqs), done with reflection
 * so there is no compile-time or hard runtime dependency on it.
 * Sets its "use lists as whitelists" option and fills the item list with the
 * foods from our config.
 */
public final class TooltipModSync {
    private TooltipModSync() {}

    @SuppressWarnings("unchecked")
    public static void sync() {
        FoodEffectsConfig ours = FoodEffectsConfig.get();
        if (!ours.syncTooltipMod) return;
        if (!FabricLoader.getInstance().isModLoaded("foodeffecttooltips")) return;

        try {
            Class<?> client = Class.forName("de.dafuqs.foodeffecttooltips.FoodeffecttooltipsClient");
            Object cfg = client.getField("CONFIG").get(null);
            if (cfg == null) {
                FoodEffects.LOGGER.warn("Food Effect Tooltips config not ready, skipping sync");
                return;
            }
            Class<?> cfgClass = cfg.getClass();

            Set<String> items = new LinkedHashSet<>();
            for (FoodEffectsConfig.Entry e : ours.entries) {
                if (e.enabled) items.add(e.item);
            }
            if (items.isEmpty()) items.add("no_mod:testitem"); // its own placeholder

            Field whitelist = cfgClass.getField("UseAsWhitelistInstead");
            Field itemList = cfgClass.getField("BlacklistedItemIdentifiers");
            Field modList = cfgClass.getField("BlacklistedModsIDs");

            whitelist.setBoolean(cfg, true);
            itemList.set(cfg, new ArrayList<>(items));
            // Mod-wide entries would also be whitelisted, so clear them.
            modList.set(cfg, new ArrayList<String>());

            Class<?> holderClass = Class.forName("me.shedaniel.autoconfig.ConfigHolder");
            Object holder = Class.forName("me.shedaniel.autoconfig.AutoConfig")
                    .getMethod("getConfigHolder", Class.class).invoke(null, cfgClass);
            holderClass.getMethod("save").invoke(holder);

            FoodEffects.LOGGER.info("Synced {} foods to Food Effect Tooltips whitelist", items.size());
        } catch (Throwable t) {
            FoodEffects.LOGGER.warn("Could not sync with Food Effect Tooltips", t);
        }
    }
}
