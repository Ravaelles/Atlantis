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
 * spiral used to re-derive these per search; the catalogue needs only
 * the anchor - validation ranks the tiles.
 * Pure lookup, no search: an unresolvable modifier (unknown names,
 * missing map knowledge) answers null and the caller keeps its legacy
 * path. Never throws: a bad anchor must decline, never crash siting.
 *
 * <p>Since A4e the vocabulary lives here, not in the deleted
 * {@code PositionModifier}: same strings, same anchors, no search.</p>
 */
public final class ModifierAnchor {

    /** Base at the nearest free expansion. */
    public static final String BASE_AT_NEAREST_FREE = "NEAREST_FREE";
    /** Building in the main base region. */
    public static final String MAIN = "MAIN";
    public static final String MAIN_MINERALS = "MAIN_MINERALS";
    /** Building at the main choke. */
    public static final String MAIN_CHOKE = "MAIN_CHOKE";
    public static final String NATURAL_CHOKE = "NATURAL_CHOKE";
    /** Building at the natural expansion. */
    public static final String NATURAL = "NATURAL";
    public static final String MAP_CENTER = "MAP_CENTER";
    public static final String ENEMY_NATURAL = "ENEMY_NATURAL";
    public static final String ENEMY_MAIN = "ENEMY_MAIN";

    private ModifierAnchor() {
    }

    /**
     * The anchor a modifier names, or null when it names nothing usable.
     * The search around it is the catalogue's job, not this method's.
     */
    public static HasPosition resolve(String modifier) {
        if (modifier == null) return null;

        try {
            switch (modifier) {
                case NATURAL:
                    return BaseLocations.natural();
                case MAIN:
                    return Select.main();
                case MAIN_CHOKE: {
                    AChoke choke = Chokes.mainChoke();
                    return choke == null ? null : choke.center();
                }
                case NATURAL_CHOKE: {
                    AChoke choke = Chokes.natural();
                    return choke == null ? null : choke.center();
                }
                case ENEMY_NATURAL:
                    return BaseLocations.enemyNatural();
                case ENEMY_MAIN:
                    return atlantis.information.enemy.EnemyUnits.enemyBase();
                case MAP_CENTER:
                    return mapCenter();
                case MAIN_MINERALS:
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
