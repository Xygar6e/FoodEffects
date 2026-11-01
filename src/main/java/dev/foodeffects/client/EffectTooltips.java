package dev.foodeffects.client;

import dev.foodeffects.ConsumableModifier;
import dev.foodeffects.FoodEffectsConfig;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffect;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Adds "When Eaten:" lines to foods configured in this mod. */
public final class EffectTooltips {
    private EffectTooltips() {}

    public static void register() {
        ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> {
            if (!FoodEffectsConfig.get().builtInTooltips) return;

            String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
            List<FoodEffectsConfig.Entry> entries = ConsumableModifier.active().get(itemId);
            if (entries == null) return;

            List<Component> out = new ArrayList<>();
            List<String> cleared = new ArrayList<>();

            for (FoodEffectsConfig.Entry e : entries) {
                if (e.effect != null && !e.effect.isBlank() && e.seconds > 0) {
                    Optional<Holder<MobEffect>> h = ConsumableModifier.lookup(e.effect);
                    if (h.isPresent()) {
                        MobEffect effect = h.get().value();
                        MutableComponent line = effect.getDisplayName().copy();
                        if (e.amplifier > 0) line.append(" " + roman(e.amplifier + 1));
                        if (!e.effect.contains("instant_")) line.append(" (" + duration(e.seconds) + ")");
                        out.add(line.withStyle(effect.getCategory().getTooltipFormatting()));
                    }
                }
                for (String c : e.clear) {
                    ConsumableModifier.lookup(c).ifPresent(hh -> cleared.add(hh.value().getDisplayName().getString()));
                }
            }

            if (!cleared.isEmpty()) {
                out.add(Component.literal("Clears: " + String.join(", ", cleared)).withStyle(ChatFormatting.BLUE));
            }
            if (out.isEmpty()) return;

            lines.add(CommonComponents.EMPTY);
            lines.add(Component.literal("When Eaten:").withStyle(ChatFormatting.DARK_PURPLE));
            lines.addAll(out);
        });
    }

    private static String duration(int seconds) {
        return (seconds / 60) + ":" + String.format("%02d", seconds % 60);
    }

    private static String roman(int n) {
        String[] r = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
        return n >= 0 && n < r.length ? r[n] : String.valueOf(n);
    }
}
