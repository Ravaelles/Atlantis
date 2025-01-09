package bweb;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Walls {
    private static Map<BWEM.ChokePoint, Wall> walls = new HashMap<>();

    public static Map<BWEM.ChokePoint, Wall> getWalls() {
        return walls;
    }

    public static Wall getWall(BWEM.ChokePoint choke) {
        return walls.get(choke);
    }

    public static Wall getClosestWall(BWAPI.TilePosition here) {
        double distBest = Double.MAX_VALUE;
        Wall bestWall = null;
        for (Wall wall : walls.values()) {
            double dist = here.getDistance(new BWAPI.TilePosition(wall.getChokePoint().Center().x, wall.getChokePoint().Center().y));
            if (dist < distBest) {
                distBest = dist;
                bestWall = wall;
            }
        }
        return bestWall;
    }

    public static Wall createWall(List<BWAPI.UnitType> buildings, BWEM.Area area, BWEM.ChokePoint choke, BWAPI.UnitType tightType, List<BWAPI.UnitType> defenses, boolean openWall, boolean requireTight) {
        // Placeholder: implement real wall creation logic if available
        Wall wall = new Wall(area, choke, buildings, defenses, tightType, requireTight, openWall);
        if (!walls.containsKey(choke)) {
            walls.put(choke, wall);
        }
        return wall;
    }

    public static void draw() {
        for (Wall wall : walls.values()) wall.draw();
    }
}
