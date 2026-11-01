package dev.foodeffects.client;

import dev.foodeffects.FoodEffectsConfig;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class FoodEffectsScreen {
    private FoodEffectsScreen() {}

    public static Screen create(Screen parent) {
        FoodEffectsConfig cfg = FoodEffectsConfig.get();
        List<FoodEffectsConfig.Entry> defaults = FoodEffectsConfig.defaultEntries();

        ConfigCategory.Builder category = ConfigCategory.createBuilder()
                .name(Component.literal("Foods"))
                .tooltip(Component.literal("Edit which effect each food gives. Changes apply after restarting the game. Add new foods in config/foodeffects.json."));

        for (FoodEffectsConfig.Entry e : cfg.entries) {
            FoodEffectsConfig.Entry def = defaults.stream()
                    .filter(d -> d.item.equals(e.item))
                    .findFirst().orElse(e.copy());

            category.group(OptionGroup.createBuilder()
                    .name(Component.literal(e.item))
                    .collapsed(true)
                    .option(Option.<Boolean>createBuilder()
                            .name(Component.literal("Enabled"))
                            .binding(def.enabled, () -> e.enabled, v -> e.enabled = v)
                            .controller(TickBoxControllerBuilder::create)
                            .build())
                    .option(Option.<String>createBuilder()
                            .name(Component.literal("Effect ID"))
                            .description(OptionDescription.of(Component.literal(
                                    "e.g. minecraft:speed, minecraft:regeneration. Leave blank for none.")))
                            .binding(def.effect, () -> e.effect, v -> e.effect = v)
                            .controller(StringControllerBuilder::create)
                            .build())
                    .option(Option.<Integer>createBuilder()
                            .name(Component.literal("Duration (seconds)"))
                            .binding(def.seconds, () -> e.seconds, v -> e.seconds = v)
                            .controller(o -> IntegerSliderControllerBuilder.create(o).range(0, 1200).step(1))
                            .build())
                    .option(Option.<Integer>createBuilder()
                            .name(Component.literal("Amplifier (0 = level I)"))
                            .binding(def.amplifier, () -> e.amplifier, v -> e.amplifier = v)
                            .controller(o -> IntegerSliderControllerBuilder.create(o).range(0, 9).step(1))
                            .build())
                    .build());
        }

        return YetAnotherConfigLib.createBuilder()
                .title(Component.literal("Food Effects"))
                .category(category.build())
                .category(ConfigCategory.createBuilder()
                        .name(Component.literal("General"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.literal("Show effects in tooltips"))
                                .description(OptionDescription.of(Component.literal(
                                        "Adds a 'When Eaten' list to configured foods. Needs no other mod.")))
                                .binding(true, () -> cfg.builtInTooltips, v -> cfg.builtInTooltips = v)
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.literal("Sync Food Effect Tooltips"))
                                .description(OptionDescription.of(Component.literal(
                                        "Only show tooltip effects for foods listed here, hiding vanilla ones like rotten flesh.")))
                                .binding(true, () -> cfg.syncTooltipMod, v -> cfg.syncTooltipMod = v)
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .build())
                .save(() -> {
                    FoodEffectsConfig.save();
                    TooltipModSync.sync();
                })
                .build()
                .generateScreen(parent);
    }
}
