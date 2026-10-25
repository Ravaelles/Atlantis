package atlantis.map.choke;

import atlantis.game.AGame;
import atlantis.information.enemy.EnemyUnits;
import atlantis.map.base.ABaseLocation;
import atlantis.map.base.BaseLocations;
import atlantis.map.base.define.DefineNaturalBase;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.map.position.Positions;
import atlantis.map.region.ARegion;
import atlantis.map.region.Regions;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.util.cache.Cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Chokes {
    private static Cache<Object> cache = new Cache<>();
//    private static HashMap<APosition, AChoke> cached_basesToChokes = new HashMap<>();
//    protected final Set<AChoke> disabledChokes = new HashSet<>();
//    protected static AChoke cached_basesToChokepoints = null;

    // =========================================================

    /**
     * NEXT #193: a main/natural designation (ours or the enemy's) under
     * this width, or it is not a choke at all. The region-null fallback
     * used to pick the Euclidean-nearest choke with no check, so a giant
     * area far from every base could designate as "main choke" (TauCross
     * bottom start). Wide areas stay in chokes() for micro use - only
     * the designations refuse them.
     */
    public static final int MAX_DESIGNATED_CHOKE_WIDTH_TILES = 10;

    /** True when this choke may designate as a main/natural choke. */
    public static boolean isPlausibleDesignatedChoke(AChoke choke) {
        return choke != null && choke.width() < MAX_DESIGNATED_CHOKE_WIDTH_TILES;
    }

    /** Nearest designation-plausible choke, or null when none qualifies. */
    public static AChoke nearestNarrowChoke(HasPosition from, Collection<AChoke> chokes) {
        if (from == null || chokes == null) return null;

        AChoke nearest = null;
        double best = Double.MAX_VALUE;
        for (AChoke choke : chokes) {
            if (!isPlausibleDesignatedChoke(choke)) continue;

            double dist = choke.distTo(from);
            if (dist < best) {
                best = dist;
                nearest = choke;
            }
        }

        return nearest;
    }

    // =========================================================

    /**
     * Returns list of all choke points i.e. places where suddenly it gets extra tight and fighting there
     * usually prefers ranged units. They are perfect places for terran bunkers.
     */
    public static List<AChoke> chokes() {
        return (List<AChoke>) cache.get(
            "chokes",
            -1,
            () -> AllChokes.get()
        );
    }

    // =========================================================

    /**
     * Every starting location in BroodWar AI tournament has exactly one critical choke point to defend. This
     * method returns this choke point. It's perfect position to defend (because it's *choke* point).
     */
    public static AChoke mainChoke() {
        return (AChoke) cache.get(
            "mainChoke",
            -1,
//            () -> mainChokeFromJbweb()
            () -> DefineMainChoke.define()
        );
    }

    public static HasPosition naturalOrAnyBuilding() {
        AChoke natural = natural();
        if (natural != null) return natural;

        return Select.mainOrAnyBuilding();
    }

    /**
     * Returns chokepoint to defend for the natural (second) base.
     */
    public static AChoke natural() {
        return (AChoke) cache.getIfValid(
            "natural",
            -1,
            () -> DefineNaturalChoke.define()
        );
    }

    public static AChoke natural(HasPosition relativeTo, String flag) {
        if (relativeTo == null) {
            return null;
        }

        return (AChoke) cache.get(
            "natural:" + relativeTo.toStringPixels(),
            -1,
            () -> {
                ARegion naturalRegion = Regions.getRegion(DefineNaturalBase.naturalIfMainIsAt(relativeTo.position()));
                if (naturalRegion == null) {
                    System.err.println("Can't find region for natural base");
                    AGame.setUmsMode();

                    return null;
                }

                AChoke mainChoke = mainChoke();
                if (mainChoke == null) {
                    return null;
                }

                for (AChoke choke : naturalRegion.chokes()) {
                    if (fullfillsConditionsForNatural(choke, flag)) return choke;
                }

                return null;
            }
        );
    }

    public static boolean fullfillsConditionsForNatural(AChoke choke, String flag) {
        if (choke == null) return false;
        if (!isPlausibleDesignatedChoke(choke)) return false;

        return choke.position().distToMapBorders() > 9
            && choke.center().distToOr999(flagToChoke(flag)) >= 10;
    }

    private static AChoke flagToChoke(String flag) {
        if (flag.equals("NATURAL")) return Chokes.mainChoke();
        if (flag.equals("ENEMY_NATURAL")) return Chokes.enemyMainChoke();

        return null;
    }

    public static AChoke nearestChoke(HasPosition position) {
        return nearestChoke(position, "");
    }

    public static AChoke nearestChoke(HasPosition position, final String flag) {
        if (position == null) return null;
//        if (Env.isTesting()) return null;

        return (AChoke) cache.get(
//            "nearestChoke:" + position.toStringPixels(),
            "nearestChoke:" + flag + ";ex" + (position.x() / 48) + ",ey:" + (position.y() / 48),
            -1,
            () -> {

                double nearestDist = 99999;
                AChoke nearest = null;

                for (AChoke choke : chokes()) {
                    if (!"ALL".equals(flag)) {
                        if (choke.equals(mainChoke())) continue;
                        if (!fullfillsConditionsForNatural(choke, flag)) continue;
                    }

//                    double dist = position.position().groundDistanceTo(choke.center()) - (choke.width() / 64.0);
                    double dist = position.position().groundDistanceTo(choke.center());
                    if (dist < nearestDist) {
                        nearestDist = dist;
                        nearest = choke;
                    }
                }

                return nearest;
            }
        );
    }

    public static AChoke enemyMainChoke() {
        AUnit enemyMain = EnemyUnits.enemyBase();

        return (AChoke) cache.getIfValid(
            "enemyMainChoke",
            -1,
//            () -> nearestChoke(enemyMain, "MAIN")
            () -> {
                HasPosition nearTo = enemyMain;

                if (nearTo == null) nearTo = enemyMainGuessWhenEnemyBaseIsUnknown();

                return nearestChoke(nearTo, "MAIN");
            }
        );
    }

    private static AChoke enemyMainGuessWhenEnemyBaseIsUnknown() {
        AChoke mainChoke = mainChoke();
        if (mainChoke == null) return null;

        List<ABaseLocation> possibleLocationsOfEnemyBase = BaseLocations.startingLocations(true);

        if (possibleLocationsOfEnemyBase.size() == 1) {
            return nearestChoke(possibleLocationsOfEnemyBase.get(0), "ENEMY_MAIN");
        }

        return null;
    }

    public static AChoke enemyNaturalChoke() {
        APosition enemyNatural = BaseLocations.enemyNatural();
        if (enemyNatural == null) {
            return null;
        }

        return (AChoke) cache.get(
            "enemyNaturalChoke",
            -1,
            () -> nearestChoke(enemyNatural, "ENEMY_NATURAL")
        );
    }

    public static APosition mainChokeCenter() {
        AChoke mainChoke = mainChoke();

        if (mainChoke == null) {
            return null;
        }

        return mainChoke.center();
    }

    public static void fakeChokes(AChoke fakeChoke) {
        cache.set(
            "chokes",
            -1,
            () -> {
                List<AChoke> chokes = new ArrayList<>();
                chokes.add(fakeChoke);

                return chokes;
            }
        );
    }

    public static Positions enemyMainAndNaturalChokes() {
        AChoke enemyMainChoke = enemyMainChoke();
        AChoke enemyNaturalChoke = enemyNaturalChoke();
        Positions positions = new Positions();

        if (enemyMainChoke != null) {
            positions.addPosition(enemyMainChoke.center());
        }
        if (enemyNaturalChoke != null) {
            positions.addPosition(enemyNaturalChoke.center());
        }

        return positions;
    }
}
