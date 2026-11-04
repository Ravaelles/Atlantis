package tests.unit;

import atlantis.map.position.APosition;
import atlantis.placement.core.ReservedSlots;
import atlantis.placement.core.TileAvailabilityGrid;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.fakes.SnapshotTerrain;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pre-planned building slots (owner order 2026-11-04, Stardust
 * doctrine): instead of hunting a site every frame, the game picks
 * slots for pylons and 10 gateways once at game start, near the main,
 * numbered for the painter (G1..G10). Pure geometry over the TauCross
 * snapshot: deterministic, no game, no engine.
 */
@Tag("placement")
public class ReservedSlotsTest {

    private static List<ReservedSlots.Slot> plan() {
        SnapshotTerrain terrain = new SnapshotTerrain(SnapshotTerrain.tauCross());
        TileAvailabilityGrid grid = new TileAvailabilityGrid(terrain);
        APosition main = terrain.baseTile(0);

        return ReservedSlots.planOn(
            new atlantis.placement.core.BuildLocationCatalogue(grid), main,
            AUnitType.Protoss_Pylon, 8,
            AUnitType.Protoss_Gateway, 10);
    }

    @Test
    public void tenNumberedGatewaySlotsPlusPylonSlots() {
        List<ReservedSlots.Slot> slots = plan();

        long gateways = slots.stream().filter(s -> s.label.startsWith("G")).count();
        long pylons = slots.stream().filter(s -> s.label.startsWith("P")).count();
        assertEquals(10, gateways, "owner ordered ten gateways");
        assertTrue(pylons >= 1, "at least one pylon slot to power them");
    }

    @Test
    public void slotsAreDistinctBuildableTiles() {
        SnapshotTerrain terrain = new SnapshotTerrain(SnapshotTerrain.tauCross());
        Set<String> seen = new HashSet<>();

        for (ReservedSlots.Slot slot : plan()) {
            String key = slot.tile.tx() + ":" + slot.tile.ty();
            assertTrue(seen.add(key),
                "no two slots share a tile: " + slot.label + " at " + key);
            assertTrue(terrain.isBuildable(slot.tile.tx(), slot.tile.ty()),
                "slot sits on buildable ground: " + slot.label + " at " + key);
        }
    }

    @Test
    public void firstGatewaySlotIsNearestToMain() {
        List<ReservedSlots.Slot> slots = plan();

        ReservedSlots.Slot g1 = null;
        for (ReservedSlots.Slot slot : slots) {
            if (slot.label.equals("G1")) g1 = slot;
        }
        assertTrue(g1 != null, "G1 exists");

        SnapshotTerrain terrain = new SnapshotTerrain(SnapshotTerrain.tauCross());
        APosition main = terrain.baseTile(0);
        for (ReservedSlots.Slot slot : slots) {
            if (!slot.label.startsWith("G")) continue;
            assertTrue(main.distTo(g1.tile) <= main.distTo(slot.tile) + 0.001,
                "G1 is the nearest gateway slot: " + g1.tile);
        }
    }

    @Test
    public void layoutIsDeterministic() {
        List<ReservedSlots.Slot> first = plan();
        List<ReservedSlots.Slot> second = plan();

        assertEquals(first.size(), second.size(), "same count");
        for (int i = 0; i < first.size(); i++) {
            assertEquals(first.get(i).label, second.get(i).label, "same order");
            assertEquals(first.get(i).tile.tx(), second.get(i).tile.tx(), "same tile");
            assertEquals(first.get(i).tile.ty(), second.get(i).tile.ty(), "same tile");
        }
    }
}
