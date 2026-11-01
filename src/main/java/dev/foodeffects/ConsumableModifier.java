package dev.foodeffects;

import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.item.consume_effects.RemoveStatusEffectsConsumeEffect;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Writes the configured effects into each food's CONSUMABLE component at load.
 * Because the effects live in the item's own data, the game applies them
 * natively and tooltip mods (Food Effect Tooltips, AppleSkin, ...) can read them.
 * Changes to the config take effect after a restart.
 */
public final class ConsumableModifier {
    private ConsumableModifier() {}

    public static void register() {
        DefaultItemComponentEvents.MODIFY.register(context -> {
            Map<String, List<FoodEffectsConfig.Entry>> byItem = new LinkedHashMap<>();
            for (FoodEffectsConfig.Entry e : FoodEffectsConfig.get().entries) {
                if (e.enabled) byItem.computeIfAbsent(e.item, k -> new ArrayList<>()).add(e);
            }

            for (Map.Entry<String, List<FoodEffectsConfig.Entry>> en : byItem.entrySet()) {
                Identifier id = Identifier.tryParse(en.getKey());
                if (id == null) {
                    FoodEffects.LOGGER.warn("Bad item id in config: {}", en.getKey());
                    continue;
                }
                Optional<Holder.Reference<Item>> holder = BuiltInRegistries.ITEM.get(id);
                if (holder.isEmpty()) {
                    FoodEffects.LOGGER.warn("Unknown item in config: {}", en.getKey());
                    continue;
                }
                Item item = holder.get().value();
                Consumable base = item.components().get(DataComponents.CONSUMABLE);
                if (base == null) {
                    FoodEffects.LOGGER.warn("{} is not consumable, skipping", en.getKey());
                    continue;
                }

                List<ConsumeEffect> extra = buildEffects(en.getValue());
                if (extra.isEmpty()) continue;

                List<ConsumeEffect> merged = new ArrayList<>(base.onConsumeEffects());
                merged.addAll(extra);
                Consumable updated = new Consumable(
                        base.consumeSeconds(), base.animation(), base.sound(),
                        base.hasConsumeParticles(), merged);

                context.modify(item, builder -> builder.set(DataComponents.CONSUMABLE, updated));
            }
        });
    }

    private static List<ConsumeEffect> buildEffects(List<FoodEffectsConfig.Entry> entries) {
        List<ConsumeEffect> out = new ArrayList<>();
        List<MobEffectInstance> apply = new ArrayList<>();
        List<Holder<MobEffect>> remove = new ArrayList<>();

        for (FoodEffectsConfig.Entry e : entries) {
            for (String clearId : e.clear) {
                lookup(clearId).ifPresent(h -> remove.add(h));
            }
            if (e.effect != null && !e.effect.isBlank() && e.seconds > 0) {
                lookup(e.effect).ifPresentOrElse(
                        h -> apply.add(new MobEffectInstance(h, e.seconds * 20, Math.max(0, e.amplifier))),
                        () -> FoodEffects.LOGGER.warn("Unknown effect in config: {}", e.effect));
            }
        }

        if (!apply.isEmpty()) out.add(new ApplyStatusEffectsConsumeEffect(apply, 1.0F));
        if (!remove.isEmpty()) out.add(new RemoveStatusEffectsConsumeEffect(HolderSet.direct(remove)));
        return out;
    }

    private static Optional<Holder<MobEffect>> lookup(String id) {
        Identifier parsed = Identifier.tryParse(id);
        if (parsed == null) return Optional.empty();
        return BuiltInRegistries.MOB_EFFECT.get(parsed).map(r -> (Holder<MobEffect>) r);
    }
}
