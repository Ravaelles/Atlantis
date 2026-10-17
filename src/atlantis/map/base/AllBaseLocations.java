package atlantis.map.base;

import atlantis.map.AMap;
import bwem.Area;
import bwem.Base;

import java.util.ArrayList;
import java.util.List;

/**
 * Every base location on the map.
 *
 * <p>
 * Own iteration over BWEM areas, replacing the {@code jbweb.Stations}
 * leg ({@code Stations.allBases}). Same membership, verified against the
 * port: a base counts when its minerals + geysers exceed 5
 * ({@code Stations.findStations} skips the rest), in area-iteration order.
 * Without initialized BWEM the answer is empty (the port answered its
 * never-filled list, same thing).
 * </p>
 */
public class AllBaseLocations {
    public static List<ABaseLocation> get() {
        List<ABaseLocation> locations = new ArrayList<>();
        if (AMap.bwem() == null || AMap.bwem().getMap() == null) return locations;

        for (Area area : AMap.bwem().getMap().getAreas()) {
            for (Base base : area.getBases()) {
                if (base.getMinerals().size() + base.getGeysers().size() <= 5) continue;
                locations.add(ABaseLocation.create(base));
            }
        }
        return locations;
    }
}
