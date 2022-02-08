package jbweb;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import bwapi.*;
import bwem.*;

public class Walls {
    private static HashMap<ChokePoint, Wall> walls = new HashMap<>();
    private static boolean logInfo = true;

    static int failedPlacement = 0;
    static int failedAngle = 0;
    static int failedPath = 0;
    static int failedTight = 0;
    static int failedSpawn = 0;
    static int failedPower = 0;

    /// Given a vector of UnitTypes, an Area and a Chokepoint, finds an optimal wall placement, returns a valid pointer if a Wall was created.
    /// Note: Highly recommend that only Terran walls attempt to be walled tight, as most Protoss and Zerg wall-ins have gaps to allow your units through.
    /// BWEB makes tight walls very differently from non-tight walls and will only create a tight wall if it is completely tight.
    /// <param name="buildings"> A Vector of UnitTypes that you want the Wall to consist of.
    /// <param name="area"> The Area that you want the Wall to be contained within.
    /// <param name="choke"> The Chokepoint that you want the Wall to block.
    /// <param name="tight"> (Optional) Decides whether this Wall intends to be walled around a specific UnitType.
    /// <param name="defenses"> (Optional) A Vector of UnitTypes that you want the Wall to have defenses consisting of.
    /// <param name="openWall"> (Optional) Set as true if you want an opening in the wall for unit movement.
    /// <param name="requireTight"> (Optional) Set as true if you want pixel perfect placement.
    public static Wall createWall(List<UnitType> buildings, Area area, ChokePoint choke, UnitType tightType, List<UnitType> defenses, boolean openWall, boolean requireTight) {
        FileWriter writeFile = null;
        try {
            writeFile = new FileWriter("bwapi-data/write/BWEB_Wall.txt");
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
        String timeNow = formatter.format(date);

        // Print the clock position of this Wall
        double clock = Math.round((JBWEB.getAngle(new Pair<>(JBWEB.mapBWEM.getMap().getCenter(), new Position(area.getTop()))) + 90) / 30);
        if (new Position(area.getTop()).x < JBWEB.mapBWEM.getMap().getCenter().x) {
            clock += 6;
        }

        // Open the log file if desired and write information
        if (logInfo) {
            try {
                writeFile.write(timeNow);
                writeFile.write(JBWEB.game.mapFileName());
                writeFile.write("At: " + clock + " o'clock.");
                writeFile.write("\n");
                writeFile.write("Buildings:");
            } catch (IOException e) {
                e.printStackTrace();
            }

            for (UnitType building : buildings){
                try {
                    writeFile.write(building.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // Verify inputs are correct
        if (area == null) {
            try {
                writeFile.write("JBWEB: Can't create a wall without a valid Area");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        if (choke == null) {

            try {
                writeFile.write("JBWEB: Can't create a wall without a valid Chokepoint");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        if (buildings.isEmpty()) {
            try {
                writeFile.write("JBWEB: Can't create a wall with an empty vector of UnitTypes.");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        // Verify not attempting to create a Wall in the same Area/ChokePoint combination
        for (ChokePoint chokePoint : walls.keySet()) {
            Wall wall = walls.get(chokePoint);
            if (wall.getArea() == area && wall.getChokePoint() == choke) {
                try {
                    writeFile.write("JBWEB: Can't create a Wall where one already exists.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return wall;
            }
        }

        // Create a Wall
        Wall wall = new Wall(area, choke, buildings, defenses, tightType, requireTight, openWall);

        // Verify the Wall creation was successful
        boolean wallFound = (wall.getSmallTiles().size() + wall.getMediumTiles().size() + wall.getLargeTiles().size()) == wall.getRawBuildings().size();

        // Log information
        if (logInfo) {
            try {
                writeFile.write("Failure Reasons:");
                writeFile.write("Power: " + failedPower);
                writeFile.write("Angle: " + failedAngle);
                writeFile.write("Placement: " + failedPlacement);
                writeFile.write("Tight: " + failedTight);
                writeFile.write("Path: " + failedPath);
                writeFile.write("Spawn: " + failedSpawn);
                writeFile.write("\n");

                date = new Date(System.currentTimeMillis() - date.getTime());
                writeFile.write("Generation Time: " + date.getTime() + "ms and " + (wallFound ? "successful." : "failed."));
                writeFile.write("--------------------");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            writeFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // If we found a suitable Wall, push into container and return pointer to it
        if (wallFound) {
            walls.replace(choke, wall);
            return walls.get(choke);
        }

        return null;
    }

    /// Creates a Forge Fast Expand at the natural.
    /// Places 1 Forge, 1 Gateway, 1 Pylon and 10 Cannons.
    public static Wall createFFE() {
        List<UnitType> buildings = new ArrayList<>();
        buildings.add(UnitType.Protoss_Forge);
        buildings.add(UnitType.Protoss_Gateway);
        buildings.add(UnitType.Protoss_Pylon);
        List<UnitType> defenses = new ArrayList<>();
        defenses.add(UnitType.Protoss_Photon_Cannon);
        defenses.add(UnitType.Protoss_Photon_Cannon);
        defenses.add(UnitType.Protoss_Photon_Cannon);
        defenses.add(UnitType.Protoss_Photon_Cannon);
        defenses.add(UnitType.Protoss_Photon_Cannon);
        defenses.add(UnitType.Protoss_Photon_Cannon);
        defenses.add(UnitType.Protoss_Photon_Cannon);
        defenses.add(UnitType.Protoss_Photon_Cannon);
        defenses.add(UnitType.Protoss_Photon_Cannon);
        defenses.add(UnitType.Protoss_Photon_Cannon);
        return createWall(buildings, JBWEB.getNaturalArea(), JBWEB.getNaturalChoke(), UnitType.None, defenses, true, false);
    }

    /// Creates a "Sim City" of Zerg buildings at the natural.
    /// Places 10 Sunkens, 1 Evolution Chamber and 1 Hatchery.
    public static Wall createZSimCity() {
        List<UnitType> buildings = new ArrayList<>();
        buildings.add(UnitType.Zerg_Hatchery);
        buildings.add(UnitType.Zerg_Evolution_Chamber);
        List<UnitType> defenses = new ArrayList<>();
        defenses.add(UnitType.Zerg_Sunken_Colony);
        defenses.add(UnitType.Zerg_Sunken_Colony);
        defenses.add(UnitType.Zerg_Sunken_Colony);
        defenses.add(UnitType.Zerg_Sunken_Colony);
        defenses.add(UnitType.Zerg_Sunken_Colony);
        defenses.add(UnitType.Zerg_Sunken_Colony);
        defenses.add(UnitType.Zerg_Sunken_Colony);
        defenses.add(UnitType.Zerg_Sunken_Colony);
        defenses.add(UnitType.Zerg_Sunken_Colony);
        defenses.add(UnitType.Zerg_Sunken_Colony);
        return createWall(buildings, JBWEB.getNaturalArea(), JBWEB.getNaturalChoke(), UnitType.None, defenses, true, false);
    }

    /// Creates a full wall of Terran buildings at the main choke.
    /// Places 2 Depots and 1 Barracks.
    public static Wall createTWall() {
        List<UnitType> buildings = new ArrayList<>();
        buildings.add(UnitType.Terran_Supply_Depot);
        buildings.add(UnitType.Terran_Supply_Depot);
        buildings.add(UnitType.Terran_Barracks);
        List<UnitType> defenses = new ArrayList<>();
        UnitType type = JBWEB.game.enemy() != null && JBWEB.game.enemy().getRace() == Race.Protoss ? UnitType.Protoss_Zealot : UnitType.Zerg_Zergling;

        return createWall(buildings, JBWEB.getMainArea(), JBWEB.getMainChoke(), type, defenses, false, true);
    }

    /// Returns the closest Wall to the given TilePosition.
    public static Wall getClosestWall(TilePosition here) {
        double distBest = Double.MAX_VALUE;
        Wall bestWall = null;
        for (ChokePoint chokePoint : walls.keySet()) {
            Wall wall = walls.get(chokePoint);
            double dist = here.getDistance(new TilePosition(wall.getChokePoint().getCenter()));

        if (dist < distBest) {
            distBest = dist;
            bestWall = wall;
        }
    }
        return bestWall;
    }

    /// Returns a pointer to a Wall if it has been created in the given Area and ChokePoint.
    /// Note: If you only pass an Area or a ChokePoint (not both), it will imply and pick a Wall that exists within that Area or blocks that ChokePoint.
    /// <param name="area"> The Area that the Wall resides in.
    /// <param name="choke"> The Chokepoint that the Wall blocks.
    public static Wall getWall(ChokePoint choke) {
        if (choke == null) {
            return null;
        }

        for (ChokePoint chokePoint : walls.keySet()) {
            Wall wall = walls.get(chokePoint);
            if (wall.getChokePoint() == choke) {
                return wall;
            }
        }
        return null;
    }

    /// Returns a map containing every Wall keyed by Chokepoint.
    public static HashMap<ChokePoint, Wall> getWalls() {
        return walls;
    }

    /// Calls the draw function for each Wall that exists.
    public static void draw() {
        for (ChokePoint chokePoint : walls.keySet()) {
            Wall wall = walls.get(chokePoint);
            wall.draw();
        }
    }
}
