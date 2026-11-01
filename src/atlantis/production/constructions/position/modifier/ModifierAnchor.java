package atlantis.production.constructions.position.modifier;

import atlantis.map.base.BaseLocations;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.select.Select;

/**
 * Migration A4b (owner #146): the legacy order modifiers ("NATURAL",
 * "MAIN", choke names...) as catalogue neighbourhood anchors. The
 * spiral used to re-derive these per search ({@code PositionModifier});
 * the catalogue needs only the anchor - validation ranks the tiles.
 * Pure lookup, no search: an unresolvable modifier (bunkers, unknown
 * names, missing map knowledge) answers null and the caller keeps its
 * legacy path. Never throws: a bad anchor must decline, never crash
 * siting.
 */
public final class ModifierAnchor {

    private ModifierAnchor() {
    }

    /**
     * The anchor a modifier names, or null when it names nothing usable.
     * Mirrors the {@code PositionModifier} vocabulary anchor-for-anchor;
     * the search around it is the catalogue's job, not this method's.
     */
    public static HasPosition resolve(String modifier) {
        if (modifier == null) return null;

        try {
            switch (modifier) {
                case PositionModifier.NATURAL:
                    return BaseLocations.natural();
                case PositionModifier.MAIN:
                    return Select.main();
                case PositionModifier.MAIN_CHOKE: {
                    AChoke choke = Chokes.mainChoke();
                    return choke == null ? null : choke.center();
                }
                case PositionModifier.NATURAL_CHOKE: {
                    AChoke choke = Chokes.natural();
                    return choke == null ? null : choke.center();
                }
                case PositionModifier.ENEMY_NATURAL:
                    return BaseLocations.enemyNatural();
                case PositionModifier.ENEMY_MAIN:
                    return atlantis.information.enemy.EnemyUnits.enemyBase();
                case PositionModifier.MAP_CENTER:
                    return mapCenter();
                case PositionModifier.MAIN_MINERALS:
                    return mainMinerals();
                default:
                    return null;
            }
        } catch (Exception | Error ignored) {
            return null;
        }
    }

    private static HasPosition mapCenter() {
        try {
            int w = atlantis.map.AMap.getMapWidthInTiles();
            int h = atlantis.map.AMap.getMapHeightInTiles();
            if (w <= 0 || h <= 0) return null;

            return APosition.create(w / 2, h / 2);
        } catch (Exception | Error ignored) {
            return null;
        }
    }

    private static HasPosition mainMinerals() {
        try {
            atlantis.units.AUnit main = Select.main();
            if (main == null || main.position() == null) return null;

            return main.translateTilesTowards(main.position().region().center(), 2);
        } catch (Exception | Error ignored) {
            return null;
        }
    }
}
