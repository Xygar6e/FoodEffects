# Food Effects (Fabric, Minecraft 26.3)

Eating food gives status effects. Rewritten for Fabric from the feature list of
Food Effects by InfernalStudios (Apache-2.0, https://github.com/infernalstudios/Food-Effects).

## How it works
At startup the mod writes your configured effects into each food's consumable
component. The game applies them natively, and tooltip mods can read them.
Config changes apply after restarting the game.

## Config
- In game: Mod Menu -> Food Effects -> Config (needs YACL + Mod Menu).
- File: config/foodeffects.json. Add a food by adding another entry; the same
  food may appear several times. "clear" lists effects removed on eating.
- Install the mod on BOTH client and server (clients need it for tooltips),
  with matching configs.

## Food Effect Tooltips
If installed, this mod switches it to whitelist mode on game start and
whitelists exactly the foods in foodeffects.json, so tooltips show only effects
from this mod (rotten flesh etc. stay hidden). Turn off with
"syncTooltipMod": false.

## Build
Easiest: generate a 26.3 project at https://fabricmc.net/develop/template/, then
copy src/ from here over it and merge dependencies from build.gradle.
Or run `gradle wrapper --gradle-version 9.7.1` here, then `./gradlew build`. Needs Java 25.

## Not ported yet
Faster eating / lower saturation for cookies and berries.
