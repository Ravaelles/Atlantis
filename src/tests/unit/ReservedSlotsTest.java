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
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
            AUnitType.Protoss_Gateway, 10);
    }

    @Test
    public void tenNumberedGatewaySlotsPlusPylonSlots() {
        List<ReservedSlots.Slot> slots = plan();

        long gateways = slots.stream().filter(s -> s.label.startsWith("G")).count();
        long pylons = slots.stream().filter(s -> s.label.startsWith("P")).count();
        long supply = slots.stream().filter(s -> s.label.startsWith("S")).count();
        assertEquals(10, gateways, "owner ordered ten gateways");
        assertTrue(pylons >= 1, "at least one pylon slot to power them");
        assertEquals(ReservedSlots.SUPPLY_PYLON_SLOTS, supply,
            "dedicated supply pool, so supply demand never eats pair cover");
    }

    @Test
    public void supplySlotsComeFirstAndNearestFirst() {
        List<ReservedSlots.Slot> slots = plan();

        int firstP = -1;
        int lastS = -1;
        ReservedSlots.Slot s1 = null;
        for (int i = 0; i < slots.size(); i++) {
            ReservedSlots.Slot slot = slots.get(i);
            if (slot.label.startsWith("S")) {
                lastS = i;
                if (slot.label.equals("S1")) s1 = slot;
            }
            if (slot.label.startsWith("P") && firstP < 0) firstP = i;
        }
        assertTrue(s1 != null, "S1 exists");
        assertTrue(lastS < firstP, "supply pool is eaten before pair cover");

        SnapshotTerrain terrain = new SnapshotTerrain(SnapshotTerrain.tauCross());
        APosition main = terrain.baseTile(0);
        for (ReservedSlots.Slot slot : slots) {
            if (!slot.label.startsWith("S")) continue;
            assertTrue(main.distTo(s1.tile) <= main.distTo(slot.tile) + 0.001,
                "S1 is the nearest supply slot: " + s1.tile);
        }
    }

    @Test
    public void firstSupplyPylonCoversFirstGateway() {
        List<ReservedSlots.Slot> slots = plan();
        ReservedSlots.Slot s1 = null;
        ReservedSlots.Slot g1 = null;
        for (ReservedSlots.Slot slot : slots) {
            if (slot.label.equals("S1")) s1 = slot;
            if (slot.label.equals("G1")) g1 = slot;
        }
        assertNotNull(s1, "S1 exists");
        assertNotNull(g1, "G1 exists");
        assertTrue(Math.max(Math.abs(s1.tile.tx() - g1.tile.tx()),
            Math.abs(s1.tile.ty() - g1.tile.ty())) <= 6,
            "the BO opens Pylon into Gateway: S1 powers G1 with no support detour, S1="
                + s1.tile + " G1=" + g1.tile);
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
    public void everyGatewaySlotSitsWithinPowerReachOfAPylonSlot() {
        SnapshotTerrain terrain = new SnapshotTerrain(SnapshotTerrain.tauCross());
        TileAvailabilityGrid grid = new TileAvailabilityGrid(terrain);
        APosition main = terrain.baseTile(0);

        java.util.List<ReservedSlots.Slot> layout = ReservedSlots.planOn(
            new atlantis.placement.core.BuildLocationCatalogue(grid), main,
            AUnitType.Protoss_Gateway, 10);

        java.util.List<APosition> pylons = new java.util.ArrayList<APosition>();
        java.util.List<ReservedSlots.Slot> gateways = new java.util.ArrayList<ReservedSlots.Slot>();
        for (ReservedSlots.Slot slot : layout) {
            if (slot.type.isPylon()) pylons.add(slot.tile);
            else gateways.add(slot);
        }
        assertEquals(10, gateways.size(), "owner ordered ten gateways, all powered");

        for (ReservedSlots.Slot gateway : gateways) {
            assertTrue(ReservedSlots.coveredByPylonSlots(
                gateway.tile.tx(), gateway.tile.ty(), pylons),
                gateway.label + " at " + gateway.tile + " must sit within power reach of a Pylon slot");
        }
    }

    @Test
    public void coveredSlotsArePrunedBeforeNumbering() {
        SnapshotTerrain terrain = new SnapshotTerrain(SnapshotTerrain.tauCross());
        TileAvailabilityGrid grid = new TileAvailabilityGrid(terrain);
        APosition main = terrain.baseTile(0);

        java.util.List<ReservedSlots.Slot> layout = ReservedSlots.planOn(
            new atlantis.placement.core.BuildLocationCatalogue(grid), main,
            AUnitType.Protoss_Gateway, 10);
        assertTrue(layout.size() > 0, "a layout exists to prune");

        ReservedSlots.Slot g1 = null;
        for (ReservedSlots.Slot slot : layout) {
            if (slot.label.equals("G1")) g1 = slot;
        }
        assertNotNull(g1, "G1 exists before pruning");
        final APosition coveredTile = g1.tile;

        java.util.List<ReservedSlots.Slot> pruned = ReservedSlots.pruneCoveredSlots(
            layout, (t, b) -> t.tx() == coveredTile.tx() && t.ty() == coveredTile.ty());

        for (ReservedSlots.Slot slot : pruned) {
            assertTrue(slot.tile.tx() != coveredTile.tx() || slot.tile.ty() != coveredTile.ty(),
                "the covered slot is gone: " + slot.label);
        }
        assertEquals(layout.size() - 1, pruned.size(), "exactly the covered slot drops");
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
