package atlantis.map.region;

import atlantis.map.AMap;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.util.cache.Cache;
import atlantis.util.log.ErrorLog;
import bwapi.Position;
import bwem.Area;

import java.util.ArrayList;
import java.util.List;

public class Regions {
    private static final Source EngineRegionsSource = (tx, ty) ->
        ARegion.create(AMap.getMap().getArea(APosition.create(tx * 32, ty * 32).toTilePosition()));
    private static final Cache<Object> cache = new Cache<>();
    private static Source source = null;
    /**
     * Test-seam twin of {@link #useSource}: the sim installs its own
     * region list (rect-region fake map) because there is no BWEM
     * behind it. Production never calls this; tests clear it in
     * {@code finally} next to the source.
     */
    private static java.util.List<ARegion> regionsOverride = null;

    public static void useSource(Source newSource) {
        source = newSource;
    }

    public static void useRegions(java.util.List<ARegion> regions) {
        regionsOverride = regions;
    }

    public static void clearRegions() {
        regionsOverride = null;
    }

    public static void useEngine() {
        source = null;
    }

    private static Source source() {
        return source != null ? source : EngineRegionsSource;
    }

    /**
     * Returns nearest (preferably directly connected) region which has center of it still unexplored.
     */
    public static ARegion getNearestUnexploredRegion(APosition position) {
        ARegion region = getRegion(position);
        if (region == null) {
            return null;
        }

        ARegion regionToVisit = null;

        for (ARegion reachableRegion : region.getReachableRegions()) {
            if (reachableRegion == null || reachableRegion.center() == null) continue;
            if (!reachableRegion.center().isExplored()) {
                regionToVisit = reachableRegion;
//                return APosition.createFrom(regionToVisit.getCenter());
                return regionToVisit;
            }
        }

        return null;
    }

    // =========================================================

    /**
     * @fix
     * @broken due to BWTA Polygon gone from the bridge :- ( It was working in JBWAPI.
     * <p>
     * Can be used to avoid getting to close to the region edges, which may cause unit to get stuck.
     */
//    public static double getDistanceToAnyRegionPolygonPoint(APosition unitPosition) {
//        return 99;
//        Region region = unitPosition.getRegion();
//
//        if (region == null) {
//            System.err.println("isPositionFarFromAnyRegionPolygonPoint -> Region is null");
//            return 999;
//        }
//        if (region.getPolygon() == null) {
//            System.err.println("isPositionFarFromAnyRegionPolygonPoint -> region.getPolygon() is null");
//            return 999;
//        }
//
//        // === Define polygon points for given region ==============
//
//        Positions polygonPoints = new Positions();
//        if (regionsToPolygonPoints.containsKey(region.toString())) {
//            polygonPoints = regionsToPolygonPoints.get(region.toString());
//        } else {
//            polygonPoints = new Positions();
//            polygonPoints.addPositions(region.getPolygon().getPoints());
//            regionsToPolygonPoints.put(region.toString(), polygonPoints);
//        }
//
////        for (Positions positions : regionsToPolygonPoints.values()) {
////            for (Iterator it = positions.arrayList().iterator(); it.hasNext();) {
////                Position position = (Position) it.next();
////                APainter.paintCircle(position, 13, Color.Yellow);
////                APainter.paintCircle(position, 16, Color.Yellow);
////            }
////        }
//
//        APosition nearestPolygon = polygonPoints.nearestTo(unitPosition);
//
//        // =========================================================
//
//        if (nearestPolygon != null) {
//            double distanceTo = nearestPolygon.distanceTo(unitPosition);
//            return nearestPolygon.distanceTo(unitPosition);
//        } else {
//            return 99;
//        }
//    }

    // =========================================================
    public static List<ARegion> regions() {
        if (regionsOverride != null) return regionsOverride;

        return (List<ARegion>) cache.get(
            "regions",
            -1,
            () -> {
                ArrayList<ARegion> regions = new ArrayList<>();
                for (Area area : AMap.getMap().getAreas()) {
                    regions.add(ARegion.create(area));
                }
                return regions;
            }
        );
    }

    /**
     * Returns region object for given <b>position</b>. This object provides some very helpful informations
     * like you can access list of choke points that belong to it etc.
     *
     * @see ARegion
     */
    public static ARegion getRegion(Object param) {
        Position position = null;

        if (param instanceof Position) {
            position = (Position) param;
        }
        else if (param instanceof ARegion) {
            position = ((ARegion) param).center().p();
        }
        else if (param instanceof HasPosition) {
            position = ((HasPosition) param).position().p();
        }
        else {
            ErrorLog.printErrorOnce("getRegion failed for " + param);

            return null;
        }

        try {
            if (position.getX() >= 31000) return null;

            ARegion region = source().regionAt(position.getX() / 32, position.getY() / 32);

            return region;
        } catch (Exception e) {
//            if (!A.isUms() || Have.base()) {
//                ErrorLog.printMaxOncePerMinutePlusPrintStackTrace("Failed trying to get region");
//                ErrorLog.printErrorOnce("Failed trying to get region for " + position + " / " + e.getMessage());
//            }
//            ErrorLog.printMaxOncePerMinutePlusPrintStackTrace("Region get fail");
//            if (A.isUms()) e.printStackTrace();
//            AConsole.printStackTrace();
        }

        return null;
    }

    public static Cache<Object> getCache() {
        return cache;
    }

    /**
     * Which region a tile belongs to.
     *
     * <p>In a game the answer comes from BWEM: {@code AMap.getMap().getArea(...)}
     * and {@link ARegion#create}. The stub world has no areas at all - every
     * inherited method that touches one would throw - so the harness answers with
     * one region per base location ({@code tests.fakes.FakeRegion}). A port says
     * which of those two is in charge, so this class no longer imports the fake
     * and the jar no longer has to ship it.</p>
     */
    public interface Source {
        /**
         * @param tx tile x, @param ty tile y.
         */
        ARegion regionAt(int tx, int ty);
    }
}
