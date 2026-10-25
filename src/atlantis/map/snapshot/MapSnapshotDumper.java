package atlantis.map.snapshot;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import atlantis.Atlantis;
import atlantis.config.env.Env;
import atlantis.map.AMap;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.util.log.ErrorLog;

/**
 * One-shot map snapshot writer (NEXT #191): captures the full static
 * copy of the running map - tiles, regions, chokes, resources, starts
 * - to {@code out/map-snapshots/<Map>.json} for the StarEngine map
 * library. Runs only with the {@code MAP_SNAPSHOT} ENV flag, once per
 * JVM, and never throws: a failed dump must not touch the game.
 *
 * <p>Why a game run at all: ground truth (walkability, BWEM areas,
 * choke geometry) lives behind the engine - parsing the .scx alone
 * cannot reproduce it. One ~60 s attach-and-dump per map replaces
 * whole test matches for map-dependent work.</p>
 */
public final class MapSnapshotDumper {

    private static boolean done = false;

    private MapSnapshotDumper() {
    }

    /** Captures and writes the snapshot; true when the file landed. */
    public static boolean dumpOnce() {
        if (done) return true;
        done = true;

        if (!Env.isMapSnapshot()) return false;

        try {
            MapSnapshot snap = capture();
            if (snap == null) return false;

            String dir = "out" + File.separator + "map-snapshots";
            new File(dir).mkdirs();
            String file = dir + File.separator + sanitize(ActiveMapName.name()) + ".json";
            java.nio.file.Files.write(
                new File(file).toPath(),
                snap.toJson().getBytes(java.nio.charset.StandardCharsets.UTF_8));

            System.out.println("[MAP_SNAPSHOT] wrote " + file
                + " (" + snap.w + "x" + snap.h
                + ", regions=" + snap.regions.size()
                + ", chokes=" + snap.chokes.size()
                + ", resources=" + snap.resources.size() + ")");

            return true;
        } catch (Throwable t) {
            try {
                ErrorLog.printMaxOncePerMinute("map snapshot failed: " + t);
            } catch (Throwable ignored) {
            }

            return false;
        }
    }

    private static String sanitize(String name) {
        if (name == null || name.isEmpty()) return "unknown-map";

        return name.replaceAll("[^A-Za-z0-9._()\\[\\] -]", "_");
    }

    private static MapSnapshot capture() {
        MapSnapshot snap = new MapSnapshot();
        try {
            snap.map = ActiveMapName.name();
        } catch (Exception ignored) {
            snap.map = "unknown";
        }
        snap.w = Atlantis.game().mapWidth();
        snap.h = Atlantis.game().mapHeight();
        if (snap.w <= 0 || snap.h <= 0 || snap.w > 512 || snap.h > 512) return null;

        // --- tiles: walkable + buildable, terrain only ----------------
        for (int ty = 0; ty < snap.h; ty++) {
            StringBuilder row = new StringBuilder(snap.w);
            for (int tx = 0; tx < snap.w; tx++) {
                boolean walkable;
                boolean buildable;
                try {
                    walkable = Atlantis.game().isWalkable(tx, ty);
                } catch (Exception ignored) {
                    walkable = false;
                }
                try {
                    buildable = Atlantis.game().isBuildable(tx, ty);
                } catch (Exception ignored) {
                    buildable = false;
                }
                row.append(!walkable ? 'x' : (buildable ? '.' : 'w'));
            }
            snap.tiles.add(row.toString());
        }

        captureRegions(snap);
        captureChokes(snap);
        captureResources(snap);
        captureStarts(snap);
        captureBuildings(snap);

        return snap;
    }

    private static void captureRegions(MapSnapshot snap) {
        try {
            java.util.List<bwem.Area> areas = AMap.getMap().getAreas();
            if (areas == null) return;

            for (bwem.Area area : areas) {
                if (area == null) continue;
                MapSnapshot.Region region = new MapSnapshot.Region();
                try {
                    region.id = area.getId().intValue();
                } catch (Exception ignored) {
                    continue;
                }
                try {
                    APosition center = atlantis.map.region.ARegion.create(area).center();
                    if (center != null) {
                        region.cx = center.tx();
                        region.cy = center.ty();
                    }
                } catch (Exception ignored) {
                }
                try {
                    for (bwem.Area neighbour : area.getAccessibleNeighbors()) {
                        if (neighbour != null) region.neighbors.add(neighbour.getId().intValue());
                    }
                } catch (Exception ignored) {
                }
                snap.regions.add(region);
            }

            for (int ty = 0; ty < snap.h; ty++) {
                for (int tx = 0; tx < snap.w; tx++) {
                    int id = -1;
                    try {
                        bwem.Area area = AMap.getMap().getArea(new bwapi.TilePosition(tx, ty));
                        if (area != null) id = area.getId().intValue();
                    } catch (Exception ignored) {
                    }
                    snap.regionOf.add(id);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private static void captureChokes(MapSnapshot snap) {
        try {
            for (atlantis.map.choke.AChoke choke : atlantis.map.choke.Chokes.chokes()) {
                if (choke == null || choke.center() == null) continue;
                MapSnapshot.Choke c = new MapSnapshot.Choke();
                c.x = choke.center().tx();
                c.y = choke.center().ty();
                c.w = choke.width();
                try {
                    bwapi.Pair<bwem.Area, bwem.Area> areas = choke.rawChoke().getAreas();
                    if (areas != null) {
                        if (areas.getLeft() != null) c.r1 = areas.getLeft().getId().intValue();
                        if (areas.getRight() != null) c.r2 = areas.getRight().getId().intValue();
                    }
                } catch (Exception ignored) {
                }
                try {
                    if (choke.firstPoint() != null) {
                        c.x1 = choke.firstPoint().tx();
                        c.y1 = choke.firstPoint().ty();
                    }
                    if (choke.lastPoint() != null) {
                        c.x2 = choke.lastPoint().tx();
                        c.y2 = choke.lastPoint().ty();
                    }
                } catch (Exception ignored) {
                }
                snap.chokes.add(c);
            }
        } catch (Exception ignored) {
        }
    }

    private static void captureResources(MapSnapshot snap) {
        try {
            for (AUnit mineral : Select.minerals().list()) {
                if (mineral == null || mineral.position() == null) continue;
                MapSnapshot.Res res = new MapSnapshot.Res();
                res.t = "M";
                res.x = mineral.position().tx();
                res.y = mineral.position().ty();
                try {
                    res.amount = mineral.u() == null ? 0 : mineral.u().getResources();
                } catch (Exception ignored) {
                    res.amount = 0;
                }
                snap.resources.add(res);
            }
        } catch (Exception ignored) {
        }
        try {
            for (AUnit geyser : Select.geysers().list()) {
                if (geyser == null || !geyser.isAlive() || geyser.position() == null) continue;
                MapSnapshot.Res res = new MapSnapshot.Res();
                res.t = "G";
                res.x = geyser.position().tx();
                res.y = geyser.position().ty();
                try {
                    res.amount = geyser.u() == null ? 0 : geyser.u().getResources();
                } catch (Exception ignored) {
                    res.amount = 0;
                }
                snap.resources.add(res);
            }
        } catch (Exception ignored) {
        }
    }

    private static void captureStarts(MapSnapshot snap) {
        try {
            java.util.List<bwapi.TilePosition> starts = Atlantis.game().getStartLocations();
            if (starts == null) return;
            for (bwapi.TilePosition start : starts) {
                if (start == null) continue;
                MapSnapshot.Pos pos = new MapSnapshot.Pos();
                pos.x = start.x;
                pos.y = start.y;
                snap.starts.add(pos);
            }
        } catch (Exception ignored) {
        }
    }

    private static void captureBuildings(MapSnapshot snap) {
        try {
            List<AUnit> buildings = new ArrayList<>(Select.ourBuildings().list());
            try {
                for (AUnit enemy : Select.enemy().buildings().list()) buildings.add(enemy);
            } catch (Exception ignored) {
            }
            for (AUnit building : buildings) {
                if (building == null || !building.isAlive() || building.position() == null) continue;
                AUnitType type = building.type();
                if (type == null) continue;
                MapSnapshot.Unit unit = new MapSnapshot.Unit();
                unit.t = type.name();
                unit.x = building.position().tx();
                unit.y = building.position().ty();
                snap.units.add(unit);
            }
        } catch (Exception ignored) {
        }
    }

    /** ActiveMap without importing the config package into map code. */
    private static final class ActiveMapName {
        static String name() {
            try {
                return atlantis.config.ActiveMap.name();
            } catch (Exception ignored) {
                return "unknown";
            }
        }
    }
}
