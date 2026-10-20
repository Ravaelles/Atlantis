package atlantis.battle.strategy;

import atlantis.map.choke.AChoke;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.select.Count;

/**
 * Choke doctrine (NEXT #170): never push a narrow choke into a nearby
 * enemy without crushing superiority; on defense wait as long as
 * possible. The line holds ~10 tiles off the choke center - unseen, in
 * range to collapse - so the enemy commits into the funnel and dies
 * retreating. Macro only (choke play needs bases); duels kite instead.
 */
public final class ChokeStandoff {

    /** Chokes farther than this from the unit are not on its way. */
    public static final double DETECT_TILES = 15;

    /** Enemies this close to the choke contest it. */
    public static final double CONTEST_TILES = 12;

    /** Stand this far off the choke center, on our side: unseen, able to collapse. */
    public static final double STANDOFF_TILES = 10;

    /** Crushing superiority: at this ratio or better the line may push through. */
    public static final double CRUSH_RATIO = 2.0;

    private ChokeStandoff() {
    }

    public static boolean applies(AUnit unit, HasPosition anchor) {
        if (unit == null) return false;
        if (Count.bases() == 0) return false;
        AChoke choke = BattleSetup.chokeNear(unit);
        if (choke == null || choke.center() == null) return false;
        int enemies = BattleSetup.enemiesNear(choke.center(), CONTEST_TILES);
        if (enemies == 0) return false;
        return !crushing(choke);
    }

    /** Pure core: with these headcounts around the choke, may the line push? */
    public static boolean mayAdvance(int oursNear, int enemiesNear) {
        if (enemiesNear <= 0) return true;
        return oursNear >= CRUSH_RATIO * enemiesNear;
    }

    public static boolean crushing(AChoke choke) {
        if (choke == null || choke.center() == null) return true;
        int enemies = BattleSetup.enemiesNear(choke.center(), CONTEST_TILES);
        int ours = BattleSetup.oursNear(choke.center(), CONTEST_TILES);
        return mayAdvance(ours, enemies);
    }

    /** Pure geometry: the hold point, standoff tiles off the center toward our side. */
    public static APosition holdPoint(APosition chokeCenter, HasPosition ourSide) {
        if (chokeCenter == null || ourSide == null) return null;
        return chokeCenter.translateTilesTowards(ourSide, STANDOFF_TILES);
    }

    /** Guarded detection: this unit's hold point, or null when no choke is held. */
    public static APosition holdPointFor(AUnit unit) {
        try {
            AChoke choke = BattleSetup.chokeNear(unit);
            if (choke == null || choke.center() == null) return null;
            return holdPoint(choke.center(), unit);
        } catch (Exception ignored) {
            return null;
        }
    }
}
