package bweb;

import java.util.*;
import java.util.function.Function;

public class Path {
    private List<BWAPI.TilePosition> tiles = new ArrayList<>();
    private double dist = 0.0;
    private boolean reachable = false, diagonal = true, cached = true;
    private BWAPI.TilePosition source, target;
    private BWAPI.UnitType type;

    public Path(BWAPI.TilePosition _source, BWAPI.TilePosition _target, BWAPI.UnitType _type, boolean _diagonal, boolean _cached) {
        source = _source;
        target = _target;
        type = _type;
        diagonal = _diagonal;
        cached = _cached;
    }
    public Path(BWAPI.Position _source, BWAPI.Position _target, BWAPI.UnitType _type, boolean _diagonal, boolean _cached) {
        this(new BWAPI.TilePosition(_source.x, _source.y), new BWAPI.TilePosition(_target.x, _target.y), _type, _diagonal, _cached);
    }
    public Path() {
        source = TilePositions.Invalid;
        target = TilePositions.Invalid;
        type = UnitTypes.None;
    }
    public List<BWAPI.TilePosition> getTiles() { return tiles; }
    public BWAPI.TilePosition getSource() { return source; }
    public BWAPI.TilePosition getTarget() { return target; }
    public double getDistance() { return dist; }
    public boolean isReachable() { return reachable; }

    public void generateJPS(Function<BWAPI.TilePosition, Boolean> walkable) {
        // Not implemented: JPS pathfinding
        reachable = false;
    }
    public void generateBFS(Function<BWAPI.TilePosition, Boolean> walkable) {
        tiles.clear();
        dist = 0.0;
        reachable = false;
        if (source == null || target == null || !walkable.apply(source) || !walkable.apply(target)) return;
        Queue<BWAPI.TilePosition> queue = new LinkedList<>();
        Map<BWAPI.TilePosition, BWAPI.TilePosition> cameFrom = new HashMap<>();
        Set<BWAPI.TilePosition> visited = new HashSet<>();
        queue.add(source);
        visited.add(source);
        int[] dx = {1, -1, 0, 0};
        int[] dy = {0, 0, 1, -1};
        boolean found = false;
        while (!queue.isEmpty()) {
            BWAPI.TilePosition curr = queue.poll();
            if (curr.x == target.x && curr.y == target.y) {
                found = true;
                break;
            }
            for (int d = 0; d < 4; d++) {
                int nx = curr.x + dx[d];
                int ny = curr.y + dy[d];
                BWAPI.TilePosition next = new BWAPI.TilePosition(nx, ny);
                if (!visited.contains(next) && walkable.apply(next)) {
                    queue.add(next);
                    visited.add(next);
                    cameFrom.put(next, curr);
                }
            }
        }
        if (found) {
            List<BWAPI.TilePosition> path = new ArrayList<>();
            BWAPI.TilePosition curr = target;
            while (!curr.equals(source)) {
                path.add(curr);
                curr = cameFrom.get(curr);
            }
            path.add(source);
            Collections.reverse(path);
            tiles.addAll(path);
            dist = path.size();
            reachable = true;
        }
    }
    public void generateAS(Function<BWAPI.TilePosition, Double> heuristic) {
        tiles.clear();
        dist = 0.0;
        reachable = false;
        if (source == null || target == null) return;
        PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingDouble(n -> n.f));
        Map<BWAPI.TilePosition, Node> allNodes = new HashMap<>();
        Node startNode = new Node(source, null, 0.0, heuristic.apply(source));
        open.add(startNode);
        allNodes.put(source, startNode);
        Set<BWAPI.TilePosition> closed = new HashSet<>();
        int[] dx = {1, -1, 0, 0};
        int[] dy = {0, 0, 1, -1};
        boolean found = false;
        Node endNode = null;
        while (!open.isEmpty()) {
            Node current = open.poll();
            if (current.pos.x == target.x && current.pos.y == target.y) {
                found = true;
                endNode = current;
                break;
            }
            closed.add(current.pos);
            for (int d = 0; d < 4; d++) {
                int nx = current.pos.x + dx[d];
                int ny = current.pos.y + dy[d];
                BWAPI.TilePosition nextPos = new BWAPI.TilePosition(nx, ny);
                if (closed.contains(nextPos)) continue;
                if (!BWEB.isWalkable(nextPos, type)) continue;
                double g = current.g + 1.0;
                double h = heuristic.apply(nextPos);
                Node nextNode = allNodes.get(nextPos);
                if (nextNode == null || g < nextNode.g) {
                    nextNode = new Node(nextPos, current, g, h);
                    open.add(nextNode);
                    allNodes.put(nextPos, nextNode);
                }
            }
        }
        if (found && endNode != null) {
            List<BWAPI.TilePosition> path = new ArrayList<>();
            Node curr = endNode;
            while (curr != null) {
                path.add(curr.pos);
                curr = curr.parent;
            }
            Collections.reverse(path);
            tiles.addAll(path);
            dist = path.size();
            reachable = true;
        }
    }
    private static class Node {
        BWAPI.TilePosition pos;
        Node parent;
        double g, h, f;
        Node(BWAPI.TilePosition pos, Node parent, double g, double h) {
            this.pos = pos;
            this.parent = parent;
            this.g = g;
            this.h = h;
            this.f = g + h;
        }
    }
    public boolean terrainWalkable(BWAPI.TilePosition tile) {
        return BWEB.isWalkable(tile, type);
    }
    public boolean unitWalkable(BWAPI.TilePosition tile) {
        return BWEB.isWalkable(tile, type) && BWEB.isUsed(tile, 1, 1) == UnitTypes.None;
    }
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Path)) return false;
        Path r = (Path) o;
        return source.equals(r.source) && target.equals(r.target);
    }
    @Override
    public int hashCode() {
        return Objects.hash(source, target);
    }
}

