package bweb;

import java.util.ArrayList;
import java.util.List;

public class Stations {
    private static List<Station> stations = new ArrayList<>();
    public static List<Station> getStations() { return stations; }
    public static Station getClosestStation(BWAPI.TilePosition here) {
        double distBest = Double.MAX_VALUE;
        Station bestStation = null;
        for (Station station : stations) {
            double dist = here.getDistance(station.getBase().Location());
            if (dist < distBest) {
                distBest = dist;
                bestStation = station;
            }
        }
        return bestStation;
    }
    public static Station getClosestMainStation(BWAPI.TilePosition here) {
        double distBest = Double.MAX_VALUE;
        Station bestStation = null;
        for (Station station : stations) {
            if (!station.isMain()) continue;
            double dist = here.getDistance(station.getBase().Location());
            if (dist < distBest) {
                distBest = dist;
                bestStation = station;
            }
        }
        return bestStation;
    }
    public static Station getClosestNaturalStation(BWAPI.TilePosition here) {
        double distBest = Double.MAX_VALUE;
        Station bestStation = null;
        for (Station station : stations) {
            if (!station.isNatural()) continue;
            double dist = here.getDistance(station.getBase().Location());
            if (dist < distBest) {
                distBest = dist;
                bestStation = station;
            }
        }
        return bestStation;
    }
    public static void findStations() {
        // Placeholder: implement real station finding logic if available
        stations.clear();
        // Example: add a sample station at (20,20) as main, and (30,30) as natural
        stations.add(new Station(new BWEM.Base() {
            @Override public BWAPI.TilePosition Location() { return new BWAPI.TilePosition(20, 20); }
        }, true, false));
        stations.add(new Station(new BWEM.Base() {
            @Override public BWAPI.TilePosition Location() { return new BWAPI.TilePosition(30, 30); }
        }, false, true));
    }
    public static void draw() {
        for (Station station : stations) station.draw();
    }
}