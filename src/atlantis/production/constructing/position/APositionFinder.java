package atlantis.production.constructing.position;

import atlantis.config.env.Env;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructing.Construction;
import atlantis.production.constructing.position.modifier.PositionModifier;
import atlantis.production.constructing.position.protoss.ProtossPositionFinder;
import atlantis.production.constructing.position.terran.TerranPositionFinder;
import atlantis.production.constructing.position.zerg.ZergPositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.util.We;
import atlantis.util.cache.Cache;
import atlantis.util.cache.CacheKey;
import atlantis.util.log.ErrorLog;

public class APositionFinder {
    public static Cache<APosition> cache = new Cache<>();

    // =========================================================

    public static void clearCache() {
        cache.clear();
    }

    /**
     * Returns build position for next building of given type.
     */
    public static APosition findPositionForNew(AUnit builder, AUnitType building, Construction construction) {
        if (construction == null || construction.productionOrder() == null) {
            ErrorLog.printMaxOncePerMinute(
                "construction = " + construction + "\n" +
                    "construction.productionOrder() = " + (construction != null ? construction.productionOrder() : "-")
            );
            return null;
        }

        if (Env.isTesting()) return APosition.create(A.rand(1, 99), A.rand(13, 99));

        HasPosition near = construction.nearTo();
        double maxDistance = construction.maxDistance() >= 0 ? construction.maxDistance() : 35;

//        System.err.println("building = " + building + ", near = " + near);

        String modifier = construction.productionOrder().getModifier();
        if (near == null && modifier != null) {
            near = PositionModifier.toPosition(modifier, building, builder, construction);
        }

        return findPositionForNew(builder, building, construction, near, maxDistance);
    }

    /**
     * Returns build position for next building of given type. If <b>nearTo</b> is not null, it forces to find
     * position
     * <b>maxDistance</b> build tiles from given position.
     */
    public static APosition findPositionForNew(
        AUnit builder, AUnitType building,
        Construction construction,
        HasPosition nearTo, double maxDistance
    ) {
        String cacheKey = CacheKey.create("findPositionForNew", building, nearTo, construction, A.digit(maxDistance));
//        System.err.println("cacheKey = " + cacheKey);

//        if (true) return FindPosition.findForBuilding(builder, building, construction, nearTo, maxDistance);

        return cache.get(
            cacheKey,
            37,
            () -> FindPosition.findForBuilding(builder, building, construction, nearTo, maxDistance)
        );
    }

    // =========================================================

    /**
     * Returns standard build position for building near given position.
     */
    public static APosition findStandardPosition(AUnit builder, AUnitType building, HasPosition nearTo, double maxDistance) {
//        return cache.get(
//            "findStandardPosition:" + building + "," + nearTo + "," + builder + "," + A.digit(maxDistance),
//            41,
//            () -> {
//            }
//        );

        if (maxDistance < 0) maxDistance = 28;

        // ===========================================================
        // = Handle standard building position according to the race =
        // = as every race uses completely different approach        =
        // ===========================================================

        // Terran
        if (We.terran()) {
            return TerranPositionFinder.findStandardPositionFor(builder, building, nearTo, maxDistance);
        }

        // Protoss
        else if (We.protoss()) {
            return ProtossPositionFinder.findStandardPositionFor(builder, building, nearTo, maxDistance);
        }

        // Zerg
        else if (We.zerg()) {
            return ZergPositionFinder.findStandardPositionFor(builder, building, nearTo, maxDistance);
        }

        else {
            System.err.println("Invalid race: " + AGame.getPlayerUs().getRace());
            System.exit(-1);
            return null;
        }
    }

}
