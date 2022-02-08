package jps;

import java.util.*;

/**
 * @author Kevin
 */
public class JPSDiagNoObstacles<T extends Node> extends JPS<T> {
    public JPSDiagNoObstacles(Graph<T> graph) {
        super(graph);
    }

    /**
     * Find the neighbors for a given node. If the node has a parent then prune the neighbors based on JPS algorithm.
     * If no parent for the given node, return all neighbors.
     */
    @Override
    protected Set<T> findNeighbors(T node, Map<T, T> parentMap) {
        Set<T> neighbors = new HashSet<>();

        Node parent = parentMap.get(node);
        if (parent != null) {
            final int x = node.x;
            final int y = node.y;
            // get normalized direction of travel
            final int dx = (x - parent.x) / Math.max(Math.abs(x - parent.x), 1);
            final int dy = (y - parent.y) / Math.max(Math.abs(y - parent.y), 1);

            // search diagonally
            if ((dx & dy) != 0) {
                if (graph.isWalkable(x, y + dy)) {
                    neighbors.add(graph.getNode(x, y + dy));
                }
                if (graph.isWalkable(x + dx, y)) {
                    neighbors.add(graph.getNode(x + dx, y));
                }
                if (graph.isWalkable(x, y + dy) && graph.isWalkable(x + dx, y)) {
                    neighbors.add(graph.getNode(x + dx, y + dy));
                }
            } else { // search horizontally/vertically
                if (dx != 0) {
                    boolean nextWalkable = graph.isWalkable(x + dx, y);
                    boolean topWalkable = graph.isWalkable(x, y + 1);
                    boolean bottomWalkable = graph.isWalkable(x, y - 1);

                    if (nextWalkable) {
                        neighbors.add(graph.getNode(x + dx, y));
                        if (topWalkable)
                            neighbors.add(graph.getNode(x + dx, y + 1));
                        if (bottomWalkable)
                            neighbors.add(graph.getNode(x + dx, y - 1));
                    }
                    if (topWalkable)
                        neighbors.add(graph.getNode(x, y + 1));
                    if (bottomWalkable)
                        neighbors.add(graph.getNode(x, y - 1));
                } else if (dy != 0) {
                    boolean nextWalkable = graph.isWalkable(x, y + dy);
                    boolean rightWalkable = graph.isWalkable(x + 1 , y);
                    boolean leftWalkable = graph.isWalkable(x - 1, y);

                    if (nextWalkable) {
                        neighbors.add(graph.getNode(x, y + dy));
                        if (rightWalkable)
                            neighbors.add(graph.getNode(x + 1, y + dy));
                        if (leftWalkable)
                            neighbors.add(graph.getNode(x - 1, y + dy));
                    }
                    if (rightWalkable)
                        neighbors.add(graph.getNode(x + 1, y));
                    if (leftWalkable)
                        neighbors.add(graph.getNode(x - 1, y));
                }
            }
        } else {
            // return all neighbors
            neighbors.addAll(graph.getNeighborsOf(node, Graph.Diagonal.NO_OBSTACLES));
        }

        return neighbors;
    }

    @Override
    protected T jump(T neighbor, T current, Set<T> goals) {
        if (neighbor == null || !neighbor.walkable) return null;
        if (goals.contains(neighbor)) return neighbor;

        int dx = neighbor.x - current.x;
        int dy = neighbor.y - current.y;

        // check for forced neighbors (eliminate symmetrical paths)
        // check along diagonal
        if ((dx & dy) != 0) {
            // when moving diagonally, must check for vertical/horizontal jump points
            if (jump(graph.getNode(neighbor.x + dx, neighbor.y), neighbor, goals) != null ||
                    jump(graph.getNode(neighbor.x, neighbor.y + dy), neighbor, goals) != null) {
                return neighbor;
            }
        } else { // check along horizontal/vertical
            if (dx != 0) {
                if ((graph.isWalkable(neighbor.x, neighbor.y - 1) && !graph.isWalkable(neighbor.x - dx, neighbor.y - 1)) ||
                        (graph.isWalkable(neighbor.x, neighbor.y + 1) && !graph.isWalkable(neighbor.x - dx, neighbor.y + 1))) {
                    return neighbor;
                }
            } else if (dy != 0) {
                if ((graph.isWalkable(neighbor.x - 1, neighbor.y) && !graph.isWalkable(neighbor.x - 1, neighbor.y - dy)) ||
                        (graph.isWalkable(neighbor.x + 1, neighbor.y) && !graph.isWalkable(neighbor.x + 1, neighbor.y - dy))) {
                    return neighbor;
                }
            }
        }

        // moving diagonally must make sure both of the vertical/horizontal neighbors is open to allow the path
        if (graph.isWalkable(neighbor.x + dx, neighbor.y) && graph.isWalkable(neighbor.x, neighbor.y + dy)) {
            return jump(graph.getNode(neighbor.x + dx, neighbor.y + dy), neighbor, goals);
        } else {
            return null;
        }
    }
}
