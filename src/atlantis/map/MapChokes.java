package atlantis.map;

import atlantis.AGame;
import atlantis.enemy.AEnemyUnits;
import atlantis.position.APosition;
import atlantis.util.Cache;
import bwem.ChokePoint;
import jbweb.JBWEB;

import java.util.*;

public class MapChokes {

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
                "mainBaseChoke",
                -1,
                () -> AChoke.create(JBWEB.getMainChoke())
//                    AUnit mainBase = Select.mainBase();
//                    if (mainBase == null) {
//                        return null;
//                    }
//
//                    // Define region where our main base is
//                    ARegion mainRegion = getRegion(mainBase.position());
//                    // System.out.println("mainRegion = " + mainRegion);
//                    if (mainRegion == null) {
//                        return null;
//                    }
//
//                    // Define localization of the second base to expand
//                    APosition natural = natural();
//                    // System.out.println("secondBase = " + secondBase);
//                    if (natural == null) {
//                        return null;
//                    }
//
//                    // Define region of the second base
//                    ARegion naturalRegion = natural.getRegion();
//                    // System.out.println("secondRegion = " + secondRegion);
//                    if (naturalRegion == null) {
//                        return null;
//                    }
//
//                    // Try to match choke points between the two regions
//                    for (AChoke mainRegionChoke : mainRegion.chokes()) {
//                        // System.out.println("mainRegionChoke = " + mainRegionChoke + " / "
//                        // + (mainRegionChoke.getFirstRegion()) + " / " + (mainRegionChoke.getSecondRegion()));
//                        if (naturalRegion.equals(mainRegionChoke.getFirstRegion())
//                                || naturalRegion.equals(mainRegionChoke.getSecondRegion())) {
//                            return mainRegionChoke;
//                            // System.out.println("MAIN CHOKE FOUND! " + cached_mainBaseChokepoint);
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
    public static AChoke chokeForNatural() {
        return (AChoke) cache.get(
                "chokeForNatural",
                100,
                () -> AChoke.create(JBWEB.getNaturalChoke())
        );
//        return chokeForNatural(AMap.natural());
    }

    public static AChoke chokeForNatural(APosition relativeTo) {
        if (relativeTo == null) {
            return null;
        }

        return (AChoke) cache.get(
                "chokeForNatural:" + relativeTo.toStringPixels(),
                400,
                () -> {
                    ARegion naturalRegion = Regions.getRegion(BaseLocations.natural(relativeTo.position()));
                    if (naturalRegion == null) {
                        System.err.println("Can't find region for natural base");
                        AGame.setUmsMode(true);
                        return null;
                    }

                    AChoke chokeForMainBase = mainChoke();
                    if (chokeForMainBase == null) {
                        return null;
                    }

                    for (AChoke choke : naturalRegion.chokes()) {
                        if (choke.getCenter().distTo(chokeForMainBase) > 1) {
                            return choke;
                        }
                    }

                    return null;
                }
        );
    }

    public static AChoke nearestChoke(APosition position) {
        return (AChoke) cache.get(
                "nearestChoke:" + position.toStringPixels(),
                -1,
            () -> {
                double nearestDist = 99999;
                AChoke nearest = null;

                for (AChoke chokePoint : chokes()) {
                    double dist = position.groundDistanceTo(chokePoint.getCenter()) - (chokePoint.getWidth() / 64.0);
                    if (dist < nearestDist) {
                        nearestDist = dist;
                        nearest = chokePoint;
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
                    for (ChokePoint choke : AMap.getMap().chokes()) {
                        chokes.add(AChoke.create(choke));
                    }
                    return chokes;
                }
        );
    }

    public static AChoke enemyMainChoke() {
        APosition enemyMain = AEnemyUnits.enemyBase();
        if (enemyMain == null) {
            return null;
        }

        return (AChoke) cache.get(
                "enemyMainChoke",
                150,
                () -> nearestChoke(enemyMain)
        );
    }

    public static AChoke enemyNaturalChoke() {
        APosition enemyNatural = BaseLocations.enemyNatural();
        if (enemyNatural == null) {
            return null;
        }

        return (AChoke) cache.get(
                "enemyNaturalChoke",
                100,
                () -> chokeForNatural(enemyNatural)
        );
    }

}
