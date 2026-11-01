package dev.foodeffects.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;

public class FoodEffectsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Food Effect Tooltips creates its config during its own client init,
        // so wait until every client initializer has run.
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> TooltipModSync.sync());
    }
}
