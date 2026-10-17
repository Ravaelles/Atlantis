package atlantis.map.analysis;

import atlantis.map.position.APosition;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The map as areas linked by chokes: ground connectivity and backdoors.
 *
 * <p>
 * Pure: hand-built nodes + edges in, answers out. No BWEM reads, no game,
 * no statics - building it from BWEM areas/chokes is the thin engine
 * adapter of the next slice. Ground connectivity here is the area-graph
 * form of {@code BaseRoles.GroundLink}: an island base is an area with no
 * ground path to ours, and a drop candidate is an enemy area in the same
 * situation. Choke widths ride along because wall anchors (M4) need them
 * from the same graph.
 * </p>
 */
public final class AreaGraph {

    /** One BWEM area as the graph sees it. */
    public static final class AreaNode {
        private final int id;
        private final APosition center;
        private final boolean hasDepot;
        private final int minerals;

        public AreaNode(int id, APosition center, boolean hasDepot, int minerals) {
            this.id = id;
            this.center = center;
            this.hasDepot = hasDepot;
            this.minerals = minerals;
        }

        public int id() {
            return id;
        }

        public APosition center() {
            return center;
        }
    }

    /** One choke between two areas, with its width in pixels. */
    public static final class ChokeEdge {
        private final int left;
        private final int right;
        private final int widthPx;

        public ChokeEdge(int left, int right, int widthPx) {
            this.left = left;
            this.right = right;
            this.widthPx = widthPx;
        }
    }

    /** An enemy area reachable by air but not by ground: a drop target. */
    public static final class DropCandidate {
        private final int areaId;
        private final APosition center;
        private final double flightTiles;

        DropCandidate(int areaId, APosition center, double flightTiles) {
            this.areaId = areaId;
            this.center = center;
            this.flightTiles = flightTiles;
        }

        public int areaId() {
            return areaId;
        }

        public APosition center() {
            return center;
        }

        public double flightTiles() {
            return flightTiles;
        }
    }

    private final Map<Integer, AreaNode> nodes;
    private final Map<Integer, Set<Integer>> links;

    private AreaGraph(Map<Integer, AreaNode> nodes, Map<Integer, Set<Integer>> links) {
        this.nodes = nodes;
        this.links = links;
    }

    public static AreaGraph of(List<AreaNode> nodes, List<ChokeEdge> edges) {
        Map<Integer, AreaNode> byId = new HashMap<>();
        Map<Integer, Set<Integer>> links = new HashMap<>();
        for (AreaNode node : nodes) {
            byId.put(node.id(), node);
            links.put(node.id(), new HashSet<Integer>());
        }
        for (ChokeEdge edge : edges) {
            if (!byId.containsKey(edge.left) || !byId.containsKey(edge.right)) continue;
            links.get(edge.left).add(edge.right);
            links.get(edge.right).add(edge.left);
        }
        return new AreaGraph(byId, links);
    }

    /** Ground path between two areas (BFS over choke links). Unknown ids are unconnected. */
    public boolean groundConnected(int fromId, int toId) {
        if (fromId == toId) return nodes.containsKey(fromId);
        if (!nodes.containsKey(fromId) || !nodes.containsKey(toId)) return false;

        Set<Integer> seen = new HashSet<>();
        Deque<Integer> open = new ArrayDeque<>();
        seen.add(fromId);
        open.add(fromId);
        while (!open.isEmpty()) {
            int current = open.poll();
            Set<Integer> next = links.get(current);
            if (next == null) continue;
            for (int id : next) {
                if (id == toId) return true;
                if (seen.add(id)) open.add(id);
            }
        }
        return false;
    }

    /**
     * Enemy areas with no ground path from ours, ranked by flight distance
     * from our center: islands first by construction, then backdoors.
     * Areas with no depot and no minerals are not worth a drop.
     */
    public List<DropCandidate> dropCandidates(int ourAreaId, Set<Integer> enemyAreaIds) {
        List<DropCandidate> candidates = new ArrayList<>();
        AreaNode ours = nodes.get(ourAreaId);
        if (ours == null || ours.center() == null) return candidates;

        for (int enemyId : enemyAreaIds) {
            AreaNode enemy = nodes.get(enemyId);
            if (enemy == null || enemy.center() == null) continue;
            if (!enemy.hasDepot && enemy.minerals == 0) continue;
            if (groundConnected(ourAreaId, enemyId)) continue;
            candidates.add(new DropCandidate(
                enemyId, enemy.center(),
                ours.center().distTo(enemy.center())));
        }

        Collections.sort(candidates, new Comparator<DropCandidate>() {
            @Override
            public int compare(DropCandidate a, DropCandidate b) {
                return Double.compare(a.flightTiles(), b.flightTiles());
            }
        });
        return candidates;
    }
}
