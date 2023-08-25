package jbweb;

import bwapi.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import bwem.Area;
import jps.*;

import static jbweb.Pathfinding.maxCacheSize;
import static jbweb.Pathfinding.unitPathCache;

public class Path {
    private List<TilePosition> tiles;
    private double dist;
    private boolean reachable;
    private TilePosition source, target;

    public Path() {
        tiles = new ArrayList<>();
        dist = 0.0;
        reachable = false;
        source = TilePosition.Invalid;
        target = TilePosition.Invalid;
    }

    /// Returns the vector of TilePositions associated with this Path.
    public List<TilePosition> getTiles() {
        return tiles;
    }

    /// Returns the source (start) TilePosition of the Path.
    public TilePosition getSource() {
        return source;
    }

    /// Returns the target (end) TilePosition of the Path.
    public TilePosition getTarget() {
        return target;
    }

    /// Returns the distance from the source to the target in pixels.
    public double getDistance() {
        return dist;
    }

    /// Returns a check if the path was able to reach the target.
    public boolean isReachable() {
        return reachable;
    }

    private List<List<Tile>> arrayToTileList(boolean[][] walkGrid) {
        List<List<Tile>> tiles = new ArrayList<>();
        for (int y = 0; y < walkGrid.length; y++) {
            List<Tile> tileRow = new ArrayList<>();
            for (int x = 0; x < walkGrid[y].length; x++) {
                Tile tile = new Tile(x, y);
                if (JBWEB.walkGrid[y][x]) {
                    tile.setWalkable(true);
                }
                else {
                    tile.setWalkable(false);
                }
                tileRow.add(tile);
            }
            tiles.add(tileRow);
        }
        return tiles;
    }

    // This function requires that parentGrid has been filled in for a path from source to target
    private void createPath(TilePosition s, TilePosition t, TilePosition[][] parentGrid) {
        tiles.add(target);
        reachable = true;
        TilePosition check = parentGrid[target.x][target.y];
        dist += new Position(target).getDistance(new Position(check));

        do {
            tiles.add(check);
            TilePosition prev = check;
            check = parentGrid[check.x][check.y];
            dist += new Position(prev).getDistance(new Position(check));
        } while (check != source);

        // HACK: Try to make it more accurate to positions instead of tiles
        Position correctionSource = new Position(tiles.get(tiles.size() - 2)); // Second to last tile
        Position correctionTarget = new Position(tiles.get(1)); // Second tile
        dist += s.getDistance(correctionSource.toTilePosition());
        dist += t.getDistance(correctionTarget.toTilePosition());
        dist -= 64.0;
    }

    /// Creates a path from the source to the target using JPS and collision provided by BWEB based on walkable tiles and used tiles.
    public void createUnitPath(Position s, Position t, Wall wall) {
        target = new TilePosition(t);
        source = new TilePosition(s);

        // If this path does not exist in cache, remove last reference and erase reference
        Pair<TilePosition, TilePosition> pathPoints = new Pair<>(source, target);
        if (unitPathCache.indexList.get(pathPoints) == null) {
            if (unitPathCache.pathCache.size() == maxCacheSize) {
                Path last = unitPathCache.pathCache.get(unitPathCache.pathCache.size() - 1);
                unitPathCache.pathCache.remove(unitPathCache.pathCache.size() - 1);
                unitPathCache.indexList.remove(new Pair<>(last.getSource(), last.getTarget()));
            }
        }

        // If it does exist, set this path as cached version, update reference and push cached path to the front
        else {
            Path oldPath = unitPathCache.indexList.get(pathPoints).get(unitPathCache.pathCacheIndex);
            dist = oldPath.getDistance();
            tiles = oldPath.getTiles();
            reachable = oldPath.isReachable();

            unitPathCache.pathCache.remove(unitPathCache.indexList.get(pathPoints).get(unitPathCache.pathCacheIndex));
            List<Path> tmpCache = new ArrayList<>();
            tmpCache.add(this);
            tmpCache.addAll(unitPathCache.pathCache);
            unitPathCache.pathCache = tmpCache;
            unitPathCache.pathCacheIndex = 0;
            return;
        }

        // If not reachable based on previous paths to this area
        if (target.isValid(JBWEB.game) && JBWEB.mapBWEM.getMap().getArea(target) != null && wall.wallWalkable(new TilePosition(source.x, source.y))) {
            System.out.println("unitPathCache = " + unitPathCache);
            System.out.println("unitPathCache.notReachableThisFrame = " + unitPathCache.notReachableThisFrame);
            Area area = JBWEB.mapBWEM.getMap().getArea(target);
            System.out.println("area = " + area);
            System.out.println("unitPathCache.notReachableThisFrame.get(area) = " + unitPathCache.notReachableThisFrame.get(area));
            System.out.println("JBWEB.game.getFrameCount() = " + JBWEB.game.getFrameCount());
            int checkReachable = unitPathCache.notReachableThisFrame.getOrDefault(area, JBWEB.game.getFrameCount());
            if (checkReachable >= JBWEB.game.getFrameCount() && JBWEB.game.getFrameCount() > 0) {
                reachable = false;
                dist = Double.MAX_VALUE;
                return;
            }
        }

        // If we found a path, store what was found
        List<List<Tile>> grid = arrayToTileList(JBWEB.walkGrid);
        JPS<Tile> jps = JPS.JPSFactory.getJPS(new Graph<>(grid), Graph.Diagonal.NO_OBSTACLES);
        Future<Queue<Tile>> futurePath = jps.findPath(new Tile(source.x, source.y), new Tile(target.x, target.y));
        Queue<Tile> path = new LinkedList<>();
        try {
            path = futurePath.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        if (path != null && !path.isEmpty()) {
            Position current = s;
            for (Tile jpsTile : path) {
                TilePosition tile = new TilePosition(jpsTile.getX(), jpsTile.getY());
                dist += new Position(tile).getDistance(current);
                current = new Position(tile);
                tiles.add(tile);
            }
            reachable = true;

            // Update cache
            List<Path> tmpCache = new ArrayList<>();
            tmpCache.add(this);
            tmpCache.addAll(unitPathCache.pathCache);
            unitPathCache.pathCache = tmpCache;
            unitPathCache.pathCacheIndex = 0;
        }

        // If not found, set destination area as unreachable for this frame
        else if (target.isValid(JBWEB.game) && JBWEB.mapBWEM.getMap().getArea(target) != null) {
            dist = Double.MAX_VALUE;
            unitPathCache.notReachableThisFrame.put(JBWEB.mapBWEM.getMap().getArea(target), JBWEB.game.getFrameCount());
            reachable = false;
        }
    }

    /// Creates a path from the source to the target using BFS.
    public void bfsPath(Position s, Position t, Wall wall) {
        TilePosition source = new TilePosition(s);
        TilePosition target = new TilePosition(t);
        List<TilePosition> direction = new ArrayList<>();
        direction.add(new TilePosition(0, 1));
        direction.add(new TilePosition(1, 0));
        direction.add(new TilePosition(-1, 0));
        direction.add(new TilePosition(0, -1));

        if (source.equals(target)
            || source.equals(new TilePosition(0, 0))
            || target.equals(new TilePosition(0, 0)))
            return;

        TilePosition[][] parentGrid = new TilePosition[256][256];
        Queue<TilePosition> nodeQueue = new LinkedList<>();
        nodeQueue.add(source);
        parentGrid[source.x][source.y] = source;

        // While not empty, pop off top the closest TilePosition to target
        while (!nodeQueue.isEmpty()) {
            TilePosition tile = nodeQueue.peek();
            nodeQueue.remove();

            for (TilePosition d : direction) {
                TilePosition next = new TilePosition(tile.x + d.x, tile.y + d.y);

                if (next.isValid(JBWEB.game)) {
                    System.out.println("A wall = " + wall);
                    System.out.println("B parentGrid = " + parentGrid);
                    System.out.println("C parentGrid = " + parentGrid[next.x]);
                    System.out.println("D parentGrid = " + parentGrid[next.x][next.y]);

                    // If next has a parent or is a collision, continue
                    if (
                        parentGrid[next.x] == null
                            || parentGrid[next.x][next.y] == null
                            || !parentGrid[next.x][next.y].equals(new TilePosition(0, 0))
                            || !wall.wallWalkable(next)
                    ) {
                        continue;
                    }

                    // Check diagonal collisions where necessary
                    if ((d.x == 1 || d.x == -1) && (d.y == 1 || d.y == -1) && (!wall.wallWalkable(new TilePosition(tile.x + d.x, tile.y))
                        || !wall.wallWalkable(new TilePosition(tile.x, tile.y + d.y)))) {
                        continue;
                    }

                    // Set parent here
                    parentGrid[next.x][next.y] = tile;

                    // If at target, return path
                    if (next.equals(target)) {
                        createPath(s.toTilePosition(), t.toTilePosition(), parentGrid);
                        return;
                    }

                    nodeQueue.add(next);
                }
            }
        }
        reachable = false;
        dist = Double.MAX_VALUE;
    }
}
