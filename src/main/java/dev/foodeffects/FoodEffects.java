package dev.foodeffects;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FoodEffects implements ModInitializer {
    public static final String MOD_ID = "foodeffects";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        // Config must be loaded before the item-component event fires.
        FoodEffectsConfig.load();
        ConsumableModifier.register();
    }
}
