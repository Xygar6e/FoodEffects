package dev.foodeffects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * JSON config at config/foodeffects.json. To add a new food, add another
 * entry to the "entries" list (same food can appear more than once to give
 * several effects). The in-game screen edits the entries that exist.
 */
public final class FoodEffectsConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("foodeffects.json");
    private static FoodEffectsConfig instance = new FoodEffectsConfig();

    /**
     * If Food Effect Tooltips is installed, switch it to whitelist mode and
     * whitelist exactly the foods in this config, so only effects given by this
     * mod are shown in tooltips (vanilla ones like rotten flesh are hidden).
     */
    public boolean syncTooltipMod = true;

    public List<Entry> entries = defaultEntries();

    public static final class Entry {
        public String item = "minecraft:cookie";
        /** Effect id, e.g. "minecraft:speed". Leave empty to only clear effects. */
        public String effect = "minecraft:speed";
        public int seconds = 10;
        /** 0 = level I, 1 = level II, ... */
        public int amplifier = 0;
        public boolean enabled = true;
        /** Effect ids removed when the food is eaten (JSON only). */
        public List<String> clear = new ArrayList<>();

        public Entry() {}

        public Entry(String item, String effect, int seconds, int amplifier, String... clear) {
            this.item = item;
            this.effect = effect;
            this.seconds = seconds;
            this.amplifier = amplifier;
            this.clear = new ArrayList<>(Arrays.asList(clear));
        }

        public Entry copy() {
            Entry e = new Entry(item, effect, seconds, amplifier);
            e.enabled = enabled;
            e.clear = new ArrayList<>(clear);
            return e;
        }
    }

    public static List<Entry> defaultEntries() {
        List<Entry> l = new ArrayList<>();
        l.add(new Entry("minecraft:mushroom_stew", "minecraft:regeneration", 5, 1));
        l.add(new Entry("minecraft:rabbit_stew", "minecraft:jump_boost", 10, 1));
        l.add(new Entry("minecraft:beetroot_soup", "minecraft:health_boost", 30, 0));
        l.add(new Entry("minecraft:cookie", "minecraft:speed", 10, 0));
        l.add(new Entry("minecraft:pumpkin_pie", "minecraft:haste", 15, 0));
        l.add(new Entry("minecraft:pufferfish", "minecraft:water_breathing", 10, 0));
        l.add(new Entry("minecraft:honey_bottle", "minecraft:instant_health", 1, 0));
        l.add(new Entry("minecraft:baked_potato", "minecraft:strength", 10, 0));
        l.add(new Entry("minecraft:dried_kelp", "", 0, 0,
                "minecraft:poison", "minecraft:nausea", "minecraft:blindness"));
        return l;
    }

    public static FoodEffectsConfig get() {
        return instance;
    }

    public static void load() {
        if (Files.exists(PATH)) {
            try (Reader r = Files.newBufferedReader(PATH)) {
                FoodEffectsConfig loaded = GSON.fromJson(r, FoodEffectsConfig.class);
                if (loaded != null && loaded.entries != null) {
                    instance = loaded;
                    return;
                }
            } catch (Exception e) {
                FoodEffects.LOGGER.error("Could not read {}, using defaults", PATH, e);
            }
        }
        instance = new FoodEffectsConfig();
        save();
    }

    public static void save() {
        try (Writer w = Files.newBufferedWriter(PATH)) {
            GSON.toJson(instance, w);
        } catch (IOException e) {
            FoodEffects.LOGGER.error("Could not write {}", PATH, e);
        }
    }
}
