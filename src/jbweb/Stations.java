package jbweb;

import bwapi.*;
import bwem.*;

import java.util.*;

public class Stations {
    private static List<Station> stations = new ArrayList<>();
    private static List<Station> mains = new ArrayList<>();
    private static List<Station> naturals = new ArrayList<>();

    private static List<TilePosition> stationDefenses(Base base, boolean placeRight, boolean placeBelow, boolean isMain, boolean isNatural) {
        List<TilePosition> defenses = new ArrayList<>();
        List<TilePosition> basePlacements = new ArrayList<>();
        List<TilePosition> geyserPlacements = new ArrayList<>();
        TilePosition here = base.getLocation();

        // Insert defenses
        if (placeBelow) {
            if (placeRight) {
                //BottomRight
                if (!isMain) {
                    basePlacements.add(new TilePosition(4, 0));
                    basePlacements.add(new TilePosition(1, 3));
                    basePlacements.add(new TilePosition(4, 3));
                    basePlacements.add(new TilePosition(-2, 2));
                    basePlacements.add(new TilePosition(-2, 0));
                    basePlacements.add(new TilePosition(-2, -2));
                    basePlacements.add(new TilePosition(0, -2));
                    basePlacements.add(new TilePosition(2, -2));
                } else {
                    basePlacements.add(new TilePosition(4, 0));
                    basePlacements.add(new TilePosition(1, 3));
                    basePlacements.add(new TilePosition(4, 3));
                }
                if (!isNatural) {
                    geyserPlacements.add(new TilePosition(0, -2));
                    geyserPlacements.add(new TilePosition(-2, 0));
                    geyserPlacements.add(new TilePosition(4, 0));
                    geyserPlacements.add(new TilePosition(0, 2));
                    geyserPlacements.add(new TilePosition(2, 2));
                }
            } else {
                //BottomLeft
                if (!isMain) {
                    basePlacements.add(new TilePosition(-2, 3));
                    basePlacements.add(new TilePosition(-2, 0));
                    basePlacements.add(new TilePosition(1, 3));
                    basePlacements.add(new TilePosition(0, -2));
                    basePlacements.add(new TilePosition(2, -2));
                    basePlacements.add(new TilePosition(4, -2));
                    basePlacements.add(new TilePosition(4, 0));
                    basePlacements.add(new TilePosition(4, 2));
                } else {
                    basePlacements.add(new TilePosition(-2, 3));
                    basePlacements.add(new TilePosition(-2, 0));
                    basePlacements.add(new TilePosition(1, 3));
                }
                if (!isNatural) {
                    geyserPlacements.add(new TilePosition(2, -2));
                    geyserPlacements.add(new TilePosition(-2, 0));
                    geyserPlacements.add(new TilePosition(4, 0));
                    geyserPlacements.add(new TilePosition(0, 2));
                    geyserPlacements.add(new TilePosition(2, 2));
                }
            }
        } else {
            if (placeRight) {
                //TopRight
                if (!isMain) {
                    basePlacements.add(new TilePosition(4, -2));
                    basePlacements.add(new TilePosition(1, -2));
                    basePlacements.add(new TilePosition(4, 1));
                    basePlacements.add(new TilePosition(-2, -1));
                    basePlacements.add(new TilePosition(-2, 1));
                    basePlacements.add(new TilePosition(-2, 3));
                    basePlacements.add(new TilePosition(0, 3));
                    basePlacements.add(new TilePosition(2, 3));
                } else {
                    basePlacements.add(new TilePosition(4, -2));
                    basePlacements.add(new TilePosition(1, -2));
                    basePlacements.add(new TilePosition(4, 1));
                }
                if (!isNatural) {
                    geyserPlacements.add(new TilePosition(0, -2));
                    geyserPlacements.add(new TilePosition(2, -2));
                    geyserPlacements.add(new TilePosition(-2, 0));
                    geyserPlacements.add(new TilePosition(4, 0));
                    geyserPlacements.add(new TilePosition(0, 2));
                }
            } else {
                //TopLeft
                if (!isMain) {
                    basePlacements.add(new TilePosition(-2, -2));
                    basePlacements.add(new TilePosition(1, -2));
                    basePlacements.add(new TilePosition(-2, 1));
                    basePlacements.add(new TilePosition(4, -1));
                    basePlacements.add(new TilePosition(4, 1));
                    basePlacements.add(new TilePosition(4, 3));
                    basePlacements.add(new TilePosition(0, 3));
                    basePlacements.add(new TilePosition(2, 3));
                } else {
                    basePlacements.add(new TilePosition(-2, -2));
                    basePlacements.add(new TilePosition(1, -2));
                    basePlacements.add(new TilePosition(-2, 1));
                }
                if (!isNatural) {
                    geyserPlacements.add(new TilePosition(0, -2));
                    geyserPlacements.add(new TilePosition(2, -2));
                    geyserPlacements.add(new TilePosition(-2, 0));
                    geyserPlacements.add(new TilePosition(4, 0));
                    geyserPlacements.add(new TilePosition(2, 2));
                }
            }
        }

        UnitType defenseType = UnitType.None;
        if (JBWEB.game.self().getRace() == Race.Protoss)
            defenseType = UnitType.Protoss_Photon_Cannon;
        if (JBWEB.game.self().getRace() == Race.Terran)
            defenseType = UnitType.Terran_Missile_Turret;
        if (JBWEB.game.self().getRace() == Race.Zerg)
            defenseType = UnitType.Zerg_Creep_Colony;

        // Add scanner addon for Terran
        if (JBWEB.game.self().getRace() == Race.Terran) {
            TilePosition scannerTile = new TilePosition(here.x + 4, here.y + 1);
            defenses.add(scannerTile);
            JBWEB.addReserve(scannerTile, 2, 2);
            JBWEB.addUsed(scannerTile, defenseType);
        }

        // Add a defense near each base placement if possible
        for (TilePosition placement : basePlacements) {
            TilePosition tile = new TilePosition(base.getLocation().x + placement.x, base.getLocation().y + placement.y);
            if (JBWEB.isPlaceable(defenseType, tile)) {
                defenses.add(tile);
                JBWEB.addReserve(tile, 2, 2);
                JBWEB.addUsed(tile, defenseType);
            }
        }

        // Add a defense near the geysers of this base if possible
        for (Geyser geyser : base.getGeysers()) {
            for (TilePosition placement : geyserPlacements) {
                TilePosition tile = new TilePosition(geyser.getTopLeft().x + placement.x, geyser.getTopLeft().y + placement.y);
                if (JBWEB.isPlaceable(defenseType, tile)) {
                    defenses.add(tile);
                    JBWEB.addReserve(tile, 2, 2);
                    JBWEB.addUsed(tile, defenseType);
                }
            }
        }

        // Remove used
        for (TilePosition tile : defenses){
            JBWEB.removeUsed (tile, 2, 2);
        }

        return defenses;
    }

    private static void addResourceOverlap(Position resourceCenter, Position startCenter, Position stationCenter) {
        TilePosition test = new TilePosition(startCenter);
        TilePosition stationTilePosition = new TilePosition(stationCenter);

        while (test != stationTilePosition) {
            double distBest = Double.MAX_VALUE;
            TilePosition current = test;
            for (int x = current.x - 1; x <= current.x + 1; x++) {
                for (int y = current.y - 1; y <= current.y + 1; y++) {
                    TilePosition t = new TilePosition(x, y);
                    Position p = new Position(t.toPosition().x + 16, t.toPosition().y + 16);

                    if (!t.isValid(JBWEB.game))
                        continue;

                    double dist = JBWEB.isReserved(t, 1, 1) ? p.getDistance(stationCenter) + 16 : p.getDistance(stationCenter);
                    if (dist <= distBest) {
                        test = t;
                        distBest = dist;
                    }
                }
            }

            if (test.isValid(JBWEB.game))
                JBWEB.addReserve(test, 1, 1);
        }
    }

    /// Initializes the building of every BWEB::Station on the map, call it only once per game.
    public static void findStations() {
        // Find all main bases
        List<Base> mainBases = new ArrayList<>();
        List<Base> natBases = new ArrayList<>();
        for (Area area : JBWEB.mapBWEM.getMap().getAreas()) {
            for (Base base : area.getBases()) {
                if (base.isStartingLocation())
                    mainBases.add(base);
            }
        }

        // Find all natural bases
        for (Base main : mainBases) {
            Base baseBest = null;
            double distBest = Double.MAX_VALUE;
            for (Area area : JBWEB.mapBWEM.getMap().getAreas()) {
                for (Base base : area.getBases()) {
                    // Must have gas, be accessible and at least 5 mineral patches
                    if (base.isStartingLocation()
                            || base.getGeysers().isEmpty()
                            || base.getArea().getAccessibleNeighbors().isEmpty()
                            || base.getMinerals().size() < 5)
                        continue;

                    double dist = JBWEB.getGroundDistance(base.getCenter(), main.getCenter());
                    if (dist < distBest) {
                        distBest = dist;
                        baseBest = base;
                    }
                }
            }

            // Store any natural we found
            if (baseBest != null) {
                natBases.add(baseBest);
            }
        }

        for (Area area : JBWEB.mapBWEM.getMap().getAreas()) {
            for (Base base : area.getBases()) {
                Position resourceCentroid = new Position(0, 0);
                Position defenseCentroid = new Position(0, 0);
                int cnt = 0;

                // Resource and defense centroids
                for (Mineral mineral : base.getMinerals()) {
                    resourceCentroid = new Position(resourceCentroid.x + mineral.getCenter().x, resourceCentroid.y + mineral.getCenter().y);
                    cnt++;
                }

                if (cnt > 0) {
                    defenseCentroid = new Position(resourceCentroid.x/cnt, resourceCentroid.y/cnt);
                }

                for (Geyser gas : base.getGeysers()) {
                    defenseCentroid = new Position((defenseCentroid.x + gas.getCenter().x)/2, (defenseCentroid.y + gas.getCenter().y)/2);
                    resourceCentroid = new Position(resourceCentroid.x + gas.getCenter().x, resourceCentroid.y + gas.getCenter().y);
                    cnt++;
                }

                if (cnt > 0)
                    resourceCentroid = new Position(resourceCentroid.x/cnt, resourceCentroid.y/cnt);

                // Add reserved tiles
                for (Mineral m : base.getMinerals()) {
                    JBWEB.addReserve(m.getTopLeft(), 2, 1);
                    addResourceOverlap(resourceCentroid, m.getCenter(), base.getCenter());
                }

                for (Geyser g : base.getGeysers()) {
                    JBWEB.addReserve(g.getTopLeft(), 4, 2);
                    addResourceOverlap(resourceCentroid, g.getCenter(), base.getCenter());
                }
                JBWEB.addReserve(base.getLocation(), 4, 3);


                // Station info
                boolean isMain = false;
                for (Base cb: mainBases) {
                    if (cb == base) {
                        isMain = true;
                        break;
                    }
                }

                boolean isNatural = false;
                for (Base cb: natBases) {
                    if (cb == base) {
                        isNatural = true;
                        break;
                    }
                }

                boolean placeRight = base.getCenter().x < defenseCentroid.x;
                boolean placeBelow = base.getCenter().y < defenseCentroid.y;
                List<TilePosition> defenses = stationDefenses(base, placeRight, placeBelow, isMain, isNatural);

                // Add to our station lists
                Station newStation = new Station(resourceCentroid, defenses, base, isMain, isNatural);
                stations.add(newStation);

                if (isMain)
                    mains.add(newStation);
                if (isNatural)
                    naturals.add(newStation);
            }
        }
    }

    /// Calls the draw function for each Station that exists.
    public static void draw() {
        for (Station station : stations){
            station.draw();
        }
    }

    /// Returns the closest BWEB::Station to the given TilePosition.
    public static Station getClosestStation(TilePosition here) {
        double distBest = Double.MAX_VALUE;
        Station bestStation = null;
        for (Station station : stations) {
            double dist = here.getDistance(station.getBWEMBase().getLocation());

            if (dist < distBest) {
                distBest = dist;
                bestStation = station;
            }
        }
        return bestStation;
    }

    /// Returns the closest main BWEB::Station to the given TilePosition.
    public static Station getClosestMainStation(TilePosition here) {
        double distBest = Double.MAX_VALUE;
        Station bestStation = null;
        for (Station station : mains) {
            double dist = here.getDistance(station.getBWEMBase().getLocation());

            if (dist < distBest) {
                distBest = dist;
                bestStation = station;
            }
        }
        return bestStation;
    }

    /// Returns the closest natural BWEB::Station to the given TilePosition.
    public static Station getClosestNaturalStation(TilePosition here) {
        double distBest = Double.MAX_VALUE;
        Station bestStation = null;
        for (Station station : naturals) {
            double dist = here.getDistance(station.getBWEMBase().getLocation());

            if (dist < distBest) {
                distBest = dist;
                bestStation = station;
            }
        }
        return bestStation;
    }

    /// Returns a vector containing every BWEB::Station.
    public static List<Station> getStations() {
        return stations;
    }

    /// Returns a vector containing every main BWEB::Station.
    public static List<Station> getMainStations() {
        return mains;
    }

    /// Returns a vector containing every natural BWEB::Station.
    public static List<Station> getNaturalStations() {
        return naturals;
    }
}
