package atlantis.map.base;

import atlantis.map.AMap;
import atlantis.map.ReachableTiles;
import atlantis.map.analysis.BaseRoles;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.select.Select;
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
 *
 * <p>
 * Roles ({@code BaseRoles}) are assigned where the list is built: one
 * place, mirroring the old init-time assignment. Anything the engine
 * cannot answer (stub worlds, no map) keeps the default EXPANSION role -
 * roles refine answers, they never break the list itself.
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

        assignRoles(locations);
        return locations;
    }

    private static void assignRoles(List<ABaseLocation> locations) {
        try {
            HasPosition ourStart = Select.mainOrAnyBuildingPosition();
            List<BaseRoles.BaseNode> nodes = new ArrayList<>(locations.size());
            for (ABaseLocation location : locations) {
                nodes.add(new BaseRoles.BaseNode(
                    location.position(),
                    location.isStartLocation(),
                    location.minerals(),
                    location.gas()));
            }
            List<BaseRoles.AssignedBase> assigned = BaseRoles.assign(
                nodes,
                ourStart == null ? null : ourStart.position(),
                new BaseRoles.GroundLink() {
                    @Override
                    public boolean connected(BaseRoles.BaseNode a, BaseRoles.BaseNode b) {
                        return ReachableTiles.between(a.position(), b.position());
                    }

                    @Override
                    public double groundDistance(BaseRoles.BaseNode a, BaseRoles.BaseNode b) {
                        return a.position().groundDistanceTo(b.position());
                    }
                });
            for (int i = 0; i < locations.size() && i < assigned.size(); i++) {
                locations.get(i).setRole(assigned.get(i).role());
            }
        } catch (Exception e) {
            // Stub worlds and mapless sources cannot answer connectivity -
            // the list above is still complete, only unrefined.
        }
    }
}
