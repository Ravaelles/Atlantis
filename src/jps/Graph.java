package jps;


import java.util.*;
import java.util.function.BiFunction;

/**
 * @author Kevin
 */
public class Graph<T extends Node> {
    public enum Diagonal {
        ALWAYS,
        NO_OBSTACLES,
        ONE_OBSTACLE,
        NEVER
    }

    private List<T> nodes;
    private int width;

    private BiFunction<Node, Node, Double> distance = euclidean;
    private BiFunction<Node, Node, Double> heuristic = euclidean;

    public Graph(List<List<T>> map, DistanceAlgo distance, DistanceAlgo heuristic) {
        width = map.get(0).size();
        nodes = new ArrayList<>(map.size() * map.get(0).size());

        map.forEach(nodes::addAll);

        this.distance = distance.algo;
        this.heuristic = heuristic.algo;
    }

    public Graph(T[][] map, DistanceAlgo distance, DistanceAlgo heuristic) {
        width = map[0].length;
        nodes = new ArrayList<>(map.length * map[0].length);

        for (T[] row : map) {
            Collections.addAll(nodes, row);
        }

        this.distance = distance.algo;
        this.heuristic = heuristic.algo;
    }

    /**
     * By default, will use EUCLIDEAN for distance and heuristic calculations. You can set your own algorithm
     * if you would like to change this.
     */
    public Graph(List<List<T>> map) {
        width = map.get(0).size();
        nodes = new ArrayList<>(map.size() * map.get(0).size());

        map.forEach(nodes::addAll);
    }

    /**
     * By default, will use EUCLIDEAN for distance and heuristic calculations. You can set your own algorithm
     * if you would like to change this.
     */
    public Graph(T[][] map) {
        width = map[0].length;
        nodes = new ArrayList<>(map.length * map[0].length);

        for (T[] row : map) {
            Collections.addAll(nodes, row);
        }
    }

    /**
     * @return List of all nodes in the graph.
     */
    public Collection<T> getNodes() { return nodes; }

    public T getNode(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= nodes.size() / width) {
            return null;
        }
        return nodes.get(x + y*width);
    }

    /**
     * Given two adjacent nodes, returns the distance between them.
     * @return The distance between the two nodes given.
     */
    public double getDistance(Node a, Node b) { return distance.apply(a, b); }

    /**
     * Given two nodes, returns the estimated distance between them. Optimizing this is the best way to improve
     * performance of your search time.
     * @return Estimated distance between the two given nodes.
     */
    public double getHeuristicDistance(Node a, Node b) { return heuristic.apply(a, b); }

    /**
     * By default, we return all reachable diagonal neighbors that have no obstacles blocking us.
     * i.e.
     * O O G
     * O C X
     * O O O
     *
     * In this above example, we could not go diagonally from our (C)urrent position to our (G)oal due to the obstacle (X).
     *
     * Please use {@link #getNeighborsOf(Node, Diagonal)} method if you would like to specify different diagonal functionality.
     *
     * @return All reachable neighboring nodes of the given node.
     */
    public Collection<T> getNeighborsOf(T node) {
        return getNeighborsOf(node, Diagonal.NO_OBSTACLES);
    }

    /**
     * @return All reachable neighboring nodes of the given node.
     */
    public Set<T> getNeighborsOf(T node, Diagonal diagonal) {
        int x = node.x;
        int y = node.y;
        Set<T> neighbors = new HashSet<>();

        boolean n = false, s = false, e = false, w = false, ne = false, nw = false, se = false, sw = false;

        // ?
        if (isWalkable(x, y - 1)) {
            neighbors.add(getNode(x, y - 1));
            n = true;
        }
        // ?
        if (isWalkable(x + 1, y)) {
            neighbors.add(getNode(x + 1, y));
            e = true;
        }
        // ?
        if (isWalkable(x, y + 1)) {
            neighbors.add(getNode(x, y+1));
            s = true;
        }
        // ?
        if (isWalkable(x - 1, y)) {
            neighbors.add(getNode(x-1, y));
            w = true;
        }

        switch (diagonal) {
            case NEVER:
                return neighbors;
            case NO_OBSTACLES:
                ne = n && e;
                nw = n && w;
                se = s && e;
                sw = s && w;
                break;
            case ONE_OBSTACLE:
                ne = n || e;
                nw = n || w;
                se = s || e;
                sw = s || w;
                break;
            case ALWAYS:
                ne = nw = se = sw = true;
        }

        // ?
        if (nw && isWalkable(x - 1, y - 1)) {
            neighbors.add(getNode(x - 1, y - 1));
        }
        // ?
        if (ne && isWalkable(x + 1, y - 1)) {
            neighbors.add(getNode(x + 1, y - 1));
        }
        // ?
        if (se && isWalkable(x + 1, y + 1)) {
            neighbors.add(getNode(x + 1, y + 1));
        }
        // ?
        if (sw && isWalkable(x - 1, y + 1)) {
            neighbors.add(getNode(x - 1, y + 1));
        }

        return neighbors;
    }

    public boolean isWalkable(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < nodes.size() / width && getNode(x, y).walkable;
    }

    /**
     * If you would like to define your own Distance Algorithm not included.
     */
    public void setDistanceAlgo(BiFunction<Node, Node, Double> distance) {
        this.distance = distance;
    }

    /**
     * If you would like to define your own Heuristic Algorithm not included.
     */
    public void setHeuristicAlgo(BiFunction<Node, Node, Double> heuristic) {
        this.heuristic = heuristic;
    }

    public enum DistanceAlgo {
        MANHATTAN(manhattan),
        EUCLIDEAN(euclidean),
        OCTILE(octile),
        CHEBYSHEV(chebyshev);

        BiFunction<Node, Node, Double> algo;

        DistanceAlgo(BiFunction<Node, Node, Double> algo) {
            this.algo = algo;
        }
    }
    private static BiFunction<Node, Node, Double> manhattan = (a, b) -> (double) Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    private static BiFunction<Node, Node, Double> euclidean = (a, b) -> Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
    private static BiFunction<Node, Node, Double> octile = (a, b) -> {
        double F = Math.sqrt(2) - 1;
        double dx = Math.abs(a.x - b.x);
        double dy = Math.abs(a.y - b.y);
        return (dx < dy) ? F * dx + dy : F * dy + dx;
    };
    private static BiFunction<Node, Node, Double> chebyshev = (a, b) -> (double) Math.max(Math.abs(a.x - b.x), Math.abs(a.y - b.y));
}
