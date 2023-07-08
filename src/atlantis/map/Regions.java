package atlantis.map;

import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.util.cache.Cache;
import atlantis.util.log.ErrorLog;
import bwapi.Position;
import bwem.Area;

import java.util.ArrayList;
import java.util.List;

public class Regions {

    private static Cache<Object> cache = new Cache<>();

    // =========================================================

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
            if (!reachableRegion.center().isExplored()) {
                regionToVisit = reachableRegion;
//                return APosition.createFrom(regionToVisit.getCenter());
                return regionToVisit;
            }
        }

        return null;
    }

    /**
     * @fix
     * @broken due to BWTA Polygon gone from the bridge :- ( It was working in BWMirror.
     *
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
        } else if (param instanceof ARegion) {
            position = ((ARegion) param).center().p();
        } else if (param instanceof HasPosition) {
            position = ((HasPosition) param).position().p();
        } else {
            ErrorLog.printErrorOnce("getRegion failed for " + param);
            return null;
        }

        try {
            ARegion region = ARegion.create(AMap.getMap().getArea(position.toTilePosition()));
            return region;
        } catch (Exception e) {
            ErrorLog.printErrorOnce("Failed trying to get region for " + position);
//            A.printStackTrace();
        }

        return null;
    }
}
