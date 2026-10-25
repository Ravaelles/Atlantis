package tests.unit.sim;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import atlantis.map.position.APosition;
import atlantis.map.region.ARegion;
import atlantis.map.region.Regions;
import atlantis.map.snapshot.MapSnapshot;
import atlantis.units.AUnitType;
import tests.fakes.FakeChoke;
import tests.fakes.FakeUnit;

/**
 * A stored map inside StarEngine (NEXT #191): loads a {@link
 * MapSnapshot} file (or inline JSON) and installs it as the sim's
 * ground - walkable + buildable tiles, region answers, region list -
 * plus spawn helpers for minerals, geysers, starts and preplaced
 * buildings. The caller's arena still owns physics; this owns
 * meaning.
 */
public final class SimMap {

    public final MapSnapshot snap;
    public final List<SimRectRegion> regions = new ArrayList<>();
    public final List<FakeChoke> chokes = new ArrayList<>();

    private SimMap(MapSnapshot snap) {
        this.snap = snap;

        Map<Integer, SimRectRegion> byId = new HashMap<>();
        for (MapSnapshot.Region r : snap.regions) {
            SimRectRegion region = new SimRectRegion("r" + r.id, APosition.create(r.cx, r.cy));
            regions.add(region);
            byId.put(r.id, region);
        }
        for (MapSnapshot.Region r : snap.regions) {
            SimRectRegion region = byId.get(r.id);
            if (region == null) continue;
            for (int neighbour : r.neighbors) {
                SimRectRegion other = byId.get(neighbour);
                if (other != null) region.link(other);
            }
        }
        for (MapSnapshot.Choke c : snap.chokes) {
            chokes.add(new FakeChoke(APosition.create(c.x, c.y), Math.max(1, c.w)));
        }
    }

    public static SimMap load(String path) throws Exception {
        String json = new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);

        return fromJson(json);
    }

    public static SimMap fromJson(String json) {
        return new SimMap(MapSnapshot.parse(json));
    }

    /** Installs tile + region answers backed by the snapshot. */
    public void install() {
        Regions.useSource((tx, ty) -> {
            if (tx < 0 || ty < 0 || tx >= snap.w || ty >= snap.h) return null;
            int id = snap.regionOf.get(ty * snap.w + tx);
            for (SimRectRegion r : regions) {
                if (r.name().equals("r" + id)) return r;
            }

            return null;
        });
        Regions.useRegions(new ArrayList<ARegion>(regions));

        atlantis.map.MapTiles.useSource(new atlantis.map.MapTiles.Source() {
            @Override
            public boolean isWalkable(atlantis.map.position.HasPosition at) {
                return tileChar(at) != 'x';
            }

            @Override
            public boolean isExplored(atlantis.map.position.HasPosition at) {
                return true;
            }

            @Override
            public boolean isVisible(atlantis.map.position.HasPosition at) {
                return true;
            }

            @Override
            public boolean hasPathBetween(atlantis.map.position.HasPosition from, atlantis.map.position.HasPosition to) {
                return true;
            }

            @Override
            public boolean isBuildable(atlantis.map.position.HasPosition at, boolean alsoCheckBuildings) {
                return tileChar(at) == '.';
            }

            @Override
            public boolean canBuildHere(atlantis.units.AUnit builder, AUnitType building, APosition at) {
                return tileChar(at) == '.';
            }

            private char tileChar(atlantis.map.position.HasPosition at) {
                try {
                    APosition p = at.position();
                    int tx = p.x() / 32;
                    int ty = p.y() / 32;
                    if (tx < 0 || ty < 0 || tx >= snap.w || ty >= snap.h) return 'x';

                    return snap.tiles.get(ty).charAt(tx);
                } catch (Exception ignored) {
                    return 'x';
                }
            }
        });
    }

    public static void uninstall() {
        Regions.useEngine();
        Regions.clearRegions();
        tests.fakes.FakeMapTiles.installAsSource();
    }

    /** Mineral fields as neutral fakes (amount travels in the file, not the unit). */
    public List<FakeUnit> minerals() {
        List<FakeUnit> out = new ArrayList<>();
        for (MapSnapshot.Res res : snap.resources) {
            if (!"M".equals(res.t)) continue;
            out.add(new FakeUnit(AUnitType.Resource_Mineral_Field, res.x, res.y).setNeutral());
        }

        return out;
    }

    public List<FakeUnit> geysers() {
        List<FakeUnit> out = new ArrayList<>();
        for (MapSnapshot.Res res : snap.resources) {
            if (!"G".equals(res.t)) continue;
            out.add(new FakeUnit(AUnitType.Resource_Vespene_Geyser, res.x, res.y).setNeutral());
        }

        return out;
    }

    public List<FakeUnit> preplaced() {
        List<FakeUnit> out = new ArrayList<>();
        Map<String, AUnitType> byName = new HashMap<>();
        try {
            for (java.lang.reflect.Field field : AUnitType.class.getFields()) {
                if (!java.lang.reflect.Modifier.isStatic(field.getModifiers())) continue;
                if (!AUnitType.class.isAssignableFrom(field.getType())) continue;
                Object value = field.get(null);
                if (value != null) byName.put(field.getName(), (AUnitType) value);
            }
        } catch (Exception ignored) {
        }
        for (MapSnapshot.Unit u : snap.units) {
            AUnitType type = byName.get(u.t);
            if (type == null) continue;
            out.add(new FakeUnit(type, u.x, u.y));
        }

        return out;
    }

    /** Tiles that accept a building, as positions. */
    public List<APosition> buildableTiles() {
        List<APosition> out = new ArrayList<>();
        for (int ty = 0; ty < snap.h; ty++) {
            String row = snap.tiles.get(ty);
            for (int tx = 0; tx < snap.w && tx < row.length(); tx++) {
                if (row.charAt(tx) == '.') out.add(APosition.create(tx, ty));
            }
        }

        return out;
    }

    /** Walkable tiles, for flood sanity checks. */
    public int walkableTiles() {
        int count = 0;
        for (String row : snap.tiles) {
            for (int i = 0; i < row.length(); i++) {
                if (row.charAt(i) != 'x') count++;
            }
        }

        return count;
    }
}
