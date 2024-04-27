package atlantis.map.choke;

import atlantis.config.ActiveMap;
import atlantis.config.env.Env;
import atlantis.game.AGame;
import atlantis.information.enemy.EnemyUnits;
import atlantis.map.AMap;
import atlantis.map.base.ABaseLocation;
import atlantis.map.base.BaseLocations;
import atlantis.map.base.define.DefineNaturalBase;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.map.region.ARegion;
import atlantis.map.region.Regions;
import atlantis.units.AUnit;
import atlantis.util.cache.Cache;
import bwem.ChokePoint;
import jbweb.JBWEB;

import java.util.ArrayList;
import java.util.List;

public class Chokes {
    private static Cache<Object> cache = new Cache<>();
//    private static HashMap<APosition, AChoke> cached_basesToChokes = new HashMap<>();
//    protected final Set<AChoke> disabledChokes = new HashSet<>();
//    protected static AChoke cached_basesToChokepoints = null;

    /**
     * Every starting location in BroodWar AI tournament has exactly one critical choke point to defend. This
     * method returns this choke point. It's perfect position to defend (because it's *choke* point).
     */
    public static AChoke mainChoke() {
        return (AChoke) cache.get(
            "mainChoke",
            -1,
            () -> AChoke.from(JBWEB.getMainChoke())
//            () -> {
//                    AUnit main = Select.main();
//                    if (main == null) {
//                        return null;
//                    }
//
//                    // Define region where our main base is
//                    ARegion mainRegion = MainRegion.mainRegion();
//                    if (mainRegion == null) {
//                        return null;
//                    }
//
//                    // Define localization of the second base to expand
//                    APosition natural = natural();
//                    if (natural == null) {
//                        return null;
//                    }
//
//                    // Define region of the second base
//                    ARegion naturalRegion = natural.getRegion();
//                    if (naturalRegion == null) {
//                        return null;
//                    }
//
//                    // Try to match choke points between the two regions
//                    for (AChoke mainRegionChoke : mainRegion.chokes()) {
//                        // + (mainRegionChoke.getFirstRegion()) + " / " + (mainRegionChoke.getSecondRegion()));
//                        if (naturalRegion.equals(mainRegionChoke.getFirstRegion())
//                                || naturalRegion.equals(mainRegionChoke.getSecondRegion())) {
//                            return mainRegionChoke;
//                        }
//                    }
//
////                    if (cached_mainBaseChoke == null) {
//                    return mainRegion.chokes().iterator().next();
////                    }
//                }
        );
    }

    /**
     * Returns chokepoint to defend for the natural (second) base.
     */
    public static AChoke natural() {
        return (AChoke) cache.getIfValid(
            "natural",
            -1,
            () -> {
                if (mainChoke() == null) return null;

                AChoke naturalFromJbweb = AChoke.from(JBWEB.getNaturalChoke());
                if (naturalFromJbweb != null) return naturalFromJbweb;

                if (!ActiveMap.isMap("7th")) {
                    AChoke choke = AChoke.from(JBWEB.getNaturalChoke());
                    if (fullfillsConditionsForNatural(choke, "NATURAL")) {
                        //                        System.err.println("choke.position() = " + choke.position());
                        //                        System.err.println("AMap.getMapHeightInTiles() = " + AMap.getMapHeightInTiles());
                        return choke;
                    }
                }

                return nearestChoke(DefineNaturalBase.natural(), "MAIN");
            }
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
//                AChoke naturalFromJbweb = AChoke.from(JBWEB.getNaturalChoke());
//                if (naturalFromJbweb != null) return naturalFromJbweb;

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

    /**
     * Returns list of all choke points i.e. places where suddenly it gets extra tight and fighting there
     * usually prefers ranged units. They are perfect places for terran bunkers.
     */
    public static List<AChoke> chokes() {
        return (List<AChoke>) cache.get(
            "chokes",
            -1,
            () -> {
                List<AChoke> chokes = new ArrayList<>();
                for (ChokePoint chokePoint : AMap.getMap().chokes()) {
                    AChoke choke = AChoke.from(chokePoint);
                    if (isOk(choke)) {
                        chokes.add(choke);
                    }
                }
                return chokes;
            }
        );
    }

    private static boolean isOk(AChoke choke) {
        return choke.width() >= 1;
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
            () -> natural(enemyNatural, "ENEMY_NATURAL")
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
}
