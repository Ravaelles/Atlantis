package tests.unit.sim;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import atlantis.map.position.APosition;
import atlantis.map.region.Regions;
import atlantis.map.snapshot.MapSnapshot;
import atlantis.units.AUnitType;
import tests.unit.AbstractTestWithUnits;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * StarEngine map snapshots (NEXT #191): the map library reads a stored
 * copy of a real map (tiles, regions, chokes, resources, starts) and
 * installs it as the sim's ground. This pins the schema + loader on a
 * hand-written micro map - no game needed; the TauCross file test
 * proves the same path on real data.
 */
@Tag("combat")
public class SimMapSnapshotTest extends AbstractTestWithUnits {

    static String microJson() {
        StringBuilder tiles = new StringBuilder("[");
        StringBuilder regionOf = new StringBuilder("[");
        for (int ty = 0; ty < 12; ty++) {
            if (ty > 0) {
                tiles.append(',');
                regionOf.append(',');
            }
            StringBuilder row = new StringBuilder();
            StringBuilder reg = new StringBuilder();
            for (int tx = 0; tx < 16; tx++) {
                boolean gap = ty >= 5 && ty <= 6;
                boolean band = (tx == 7 || tx == 8) && !gap;
                char tile = band ? 'x' : '.';
                if (tx == 0 && ty == 0) tile = 'w';
                row.append(tile);
                if (tx > 0) reg.append(',');
                reg.append(tx < 7 || ((tx == 7 || tx == 8) && ty < 6) ? 7 : 9);
            }
            tiles.append('"').append(row).append('"');
            regionOf.append(reg);
        }
        tiles.append(']');
        regionOf.append(']');

        return "{\"map\":\"Micro\",\"w\":16,\"h\":12,"
            + "\"tiles\":" + tiles + ","
            + "\"regionOf\":" + regionOf + ","
            + "\"regions\":["
            + "{\"id\":7,\"cx\":3,\"cy\":6,\"neighbors\":[9]},"
            + "{\"id\":9,\"cx\":12,\"cy\":6,\"neighbors\":[7]}],"
            + "\"chokes\":[{\"x\":7,\"y\":5,\"w\":2,\"r1\":7,\"r2\":9,"
            + "\"x1\":7,\"y1\":5,\"x2\":7,\"y2\":6}],"
            + "\"resources\":["
            + "{\"t\":\"M\",\"x\":3,\"y\":3,\"amount\":1500},"
            + "{\"t\":\"M\",\"x\":4,\"y\":3,\"amount\":1500},"
            + "{\"t\":\"G\",\"x\":3,\"y\":8,\"amount\":5000}],"
            + "\"starts\":[{\"x\":2,\"y\":2},{\"x\":13,\"y\":2}],"
            + "\"units\":[{\"t\":\"Protoss_Nexus\",\"x\":2,\"y\":9}]}";
    }

    @Test
    public void microMapParsesCompletely() {
        MapSnapshot snap = MapSnapshot.parse(microJson());

        assertEquals("Micro", snap.map);
        assertEquals(16, snap.w);
        assertEquals(12, snap.h);
        assertEquals(12, snap.tiles.size());
        assertEquals(192, snap.regionOf.size());
        assertEquals(2, snap.regions.size());
        assertEquals(1, snap.chokes.size());
        assertEquals(3, snap.resources.size());
        assertEquals(2, snap.starts.size());
        assertEquals(1, snap.units.size());

        assertEquals(20, countChar(snap, 'x', false), "blocked tiles");
        assertEquals(171, countChar(snap, '.', true), "buildable tiles");

        assertEquals(7, snap.chokes.get(0).r1);
        assertEquals(9, snap.chokes.get(0).r2);
        assertEquals(1500, snap.resources.get(0).amount);
        assertEquals("G", snap.resources.get(2).t);
    }

    @Test
    public void microMapRoundTrips() {
        MapSnapshot snap = MapSnapshot.parse(microJson());
        MapSnapshot reread = MapSnapshot.parse(snap.toJson());

        assertEquals(snap.w, reread.w);
        assertEquals(snap.h, reread.h);
        assertEquals(snap.tiles, reread.tiles);
        assertEquals(snap.regionOf, reread.regionOf);
        assertEquals(snap.regions.size(), reread.regions.size());
        assertEquals(snap.chokes.size(), reread.chokes.size());
        assertEquals(snap.resources.size(), reread.resources.size());
        assertEquals(snap.starts.size(), reread.starts.size());
        assertEquals(9, reread.regions.get(0).neighbors.get(0));
    }

    @Test
    public void microMapInstallsAsSimGround() {
        SimMap map = SimMap.fromJson(microJson());
        map.install();
        try {
            assertEquals("r7", regionName(APosition.create(3, 3)));
            assertEquals("r9", regionName(APosition.create(13, 3)));

            assertTrue(APosition.create(2, 2).isWalkable(), "open ground walks");
            assertTrue(APosition.create(2, 2).isBuildableNotIncludingBuildings(), "open ground builds");
            assertFalse(APosition.create(7, 2).isWalkable(), "the wall band blocks");
            assertFalse(APosition.create(7, 2).isBuildableNotIncludingBuildings(), "the wall band builds nothing");
            assertTrue(APosition.create(7, 5).isWalkable(), "the gap stays open");

            assertEquals(1, map.chokes.size());
            assertEquals(2, map.minerals().size());
            assertEquals(1, map.geysers().size());
            assertEquals(1, map.preplaced().size());
            assertEquals(AUnitType.Resource_Mineral_Field, map.minerals().get(0).type());
        }
        finally {
            SimMap.uninstall();
        }
    }

    private static String regionName(APosition pos) {
        atlantis.map.region.ARegion region = Regions.getRegion(pos);
        assertNotNull(region, "every tile answers a region");
        assertTrue(region instanceof tests.unit.sim.SimRectRegion, "the snapshot map answers: " + region.getClass());

        return ((tests.unit.sim.SimRectRegion) region).name();
    }

    /**
     * The real-file proof (NEXT #191): tonight's TauCross dump loads
     * through the same loader and answers as sim ground. The file is a
     * committed fixture (out/ gets wiped), read like other source-pinned
     * tests via a repo-relative path.
     */
    @Test
    public void realTauCrossFileLoadsAndAnswers() throws Exception {
        String json = new String(
            java.nio.file.Files.readAllBytes(
                java.nio.file.Paths.get("src/tests/resources/taucross-snapshot.json")),
            java.nio.charset.StandardCharsets.UTF_8);
        MapSnapshot snap = MapSnapshot.parse(json);

        assertEquals(128, snap.w);
        assertEquals(128, snap.h);
        assertEquals(21, snap.regions.size(), "TauCross regions");
        assertEquals(21, snap.chokes.size(), "TauCross chokes");
        assertEquals(102, snap.resources.size(), "minerals + geysers");
        assertEquals(3, snap.starts.size());
        assertEquals(128 * 128, snap.regionOf.size());

        MapSnapshot reread = MapSnapshot.parse(snap.toJson());
        assertEquals(snap.regionOf, reread.regionOf, "tiles survive the round trip");

        SimMap map = SimMap.fromJson(json);
        map.install();
        try {
            assertTrue(firstTileOf(snap, '.').isWalkable(), "open ground walks");
            assertFalse(firstTileOf(snap, 'x').isWalkable(), "walls block");
            assertEquals(21, map.chokes.size());
            assertFalse(map.minerals().isEmpty(), "mineral lines spawn");
            assertFalse(map.geysers().isEmpty(), "geysers spawn");
        }
        finally {
            SimMap.uninstall();
        }
    }

    private static APosition firstTileOf(MapSnapshot snap, char tile) {
        for (int ty = 0; ty < snap.h; ty++) {
            String row = snap.tiles.get(ty);
            for (int tx = 0; tx < row.length(); tx++) {
                if (row.charAt(tx) == tile) return APosition.create(tx, ty);
            }
        }

        throw new IllegalStateException("no '" + tile + "' tile in the snapshot");
    }

    private static int countChar(MapSnapshot snap, char c, boolean buildable) {
        int count = 0;
        for (String row : snap.tiles) {
            for (int i = 0; i < row.length(); i++) {
                if (buildable ? row.charAt(i) == '.' : row.charAt(i) == 'x') count++;
            }
        }

        return count;
    }
}
