package atlantis.map.analysis;

import atlantis.map.position.APosition;

import java.util.ArrayList;
import java.util.List;

/**
 * One-time role assignment for every base location on the map.
 *
 * <p>
 * Pure: roles are computed from the base list, our start position and a
 * ground-connectivity predicate - no game reads, no statics, no cache. The
 * same rules the old port applied ({@code Stations.findStations}: natural =
 * nearest gas + 5-mineral base to a main), stated as data flow instead of
 * init-time side effects. Wiring (who builds the node list per game) is the
 * next slice; this one pins the arithmetic.
 * </p>
 */
public final class BaseRoles {

    private BaseRoles() {
    }

    /**
     * Assigns exactly one role per node, relative to {@code ourStart}:
     * the start location we began on is MAIN; the nearest gas-bearing
     * 5-mineral base to it is NATURAL; a base with no gas is MINERAL_ONLY;
     * a base with no ground link to MAIN is ISLAND; the rest are EXPANSION.
     * Island wins over mineral-only (an island without gas is still reached
     * by air, not by walking).
     */
    public static List<AssignedBase> assign(List<BaseNode> nodes, APosition ourStart, GroundLink link) {
        List<AssignedBase> assigned = new ArrayList<>(nodes.size());
        if (nodes.isEmpty()) return assigned;

        BaseNode main = nearestStart(nodes, ourStart);
        BaseNode natural = naturalFor(nodes, main, link);

        for (BaseNode node : nodes) {
            assigned.add(new AssignedBase(node, roleOf(node, main, natural, link)));
        }
        return assigned;
    }

    private static BaseNode nearestStart(List<BaseNode> nodes, APosition ourStart) {
        BaseNode best = null;
        double bestDist = Double.MAX_VALUE;
        for (BaseNode node : nodes) {
            if (!node.startLocation) continue;
            double dist = ourStart == null ? 0 : node.position.distTo(ourStart);
            if (dist < bestDist) {
                bestDist = dist;
                best = node;
            }
        }
        if (best != null) return best;

        return nodes.get(0);
    }

    private static BaseNode naturalFor(List<BaseNode> nodes, BaseNode main, GroundLink link) {
        BaseNode best = null;
        double bestDist = Double.MAX_VALUE;
        for (BaseNode node : nodes) {
            if (node == main || node.startLocation) continue;
            if (node.gas == 0 || node.minerals < 5) continue;
            if (!link.connected(main, node)) continue;
            double dist = link.groundDistance(main, node);
            if (dist < bestDist) {
                bestDist = dist;
                best = node;
            }
        }
        return best;
    }

    private static Role roleOf(BaseNode node, BaseNode main, BaseNode natural, GroundLink link) {
        if (node == main) return Role.MAIN;
        if (!link.connected(main, node)) return Role.ISLAND;
        if (node == natural) return Role.NATURAL;
        if (node.gas == 0) return Role.MINERAL_ONLY;

        return Role.EXPANSION;
    }

    /**
     * What a base location is for us, assigned once per game.
     */
    public enum Role {
        MAIN,
        NATURAL,
        EXPANSION,
        ISLAND,
        MINERAL_ONLY
    }

    /**
     * Ground connectivity between two nodes, owned by the caller (reachability, not geometry).
     */
    public interface GroundLink {
        boolean connected(BaseNode a, BaseNode b);

        /**
         * Walking distance in tiles; decides which expansion is the natural.
         */
        double groundDistance(BaseNode a, BaseNode b);
    }

    /**
     * One base location as the assignment sees it: geometry + economy only.
     */
    public static final class BaseNode {
        private final APosition position;
        private final boolean startLocation;
        private final int minerals;
        private final int gas;

        public BaseNode(APosition position, boolean startLocation, int minerals, int gas) {
            this.position = position;
            this.startLocation = startLocation;
            this.minerals = minerals;
            this.gas = gas;
        }

        public APosition position() {
            return position;
        }
    }

    /**
     * A node with its assigned role.
     */
    public static final class AssignedBase {
        private final BaseNode node;
        private final Role role;

        AssignedBase(BaseNode node, Role role) {
            this.node = node;
            this.role = role;
        }

        public BaseNode node() {
            return node;
        }

        public Role role() {
            return role;
        }
    }
}
