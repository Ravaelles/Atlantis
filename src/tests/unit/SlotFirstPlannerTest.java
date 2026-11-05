package tests.unit;

import atlantis.map.position.APosition;
import atlantis.placement.core.BuildLocationCatalogue;
import atlantis.placement.core.ReservedSlots;
import atlantis.placement.core.TileAvailabilityGrid;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.fakes.SnapshotTerrain;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * V3 S6: the slot order the single door serves. Pure decision pin
 * (no world, no Select): oldest planned first, covered slots
 * skipped, nothing planned for unknown types, null-safe throughout.
 * The occupancy predicate is the caller's (footprint overlap in the
 * planner and the funnel); here a scripted set stands in, so the
 * test pins the ORDER, not the engine query. The hunt-vs-slot
 * outcome is indistinguishable in sim (the ranker converges on the
 * same tile) and is proved live (V2_DISPATCH tile == slot tile).
 */
@Tag("placement")
public class SlotFirstPlannerTest {

    private static List<ReservedSlots.Slot> layout() {
        SnapshotTerrain terrain = new SnapshotTerrain(SnapshotTerrain.tauCross());
        TileAvailabilityGrid grid = new TileAvailabilityGrid(terrain);
        return ReservedSlots.planOn(
            new BuildLocationCatalogue(grid),
            terrain.baseTile(0),
            AUnitType.Protoss_Gateway, 10);
    }

    @AfterEach
    public void clean() {
        ReservedSlots.clearForTests();
    }

    @Test
    public void firstFreeSlotIsG1() {
        List<ReservedSlots.Slot> layout = layout();
        ReservedSlots.setSlotsForTests(layout);

        APosition tile = ReservedSlots.firstFreeSlotOfType(
            AUnitType.Protoss_Gateway, (t, b) -> false);

        assertNotNull(tile, "an empty board serves the first gateway slot");
        assertEquals(label(layout, "G1").tile.tx(), tile.tx(), "oldest planned first");
        assertEquals(label(layout, "G1").tile.ty(), tile.ty(),
            "oldest planned first");
    }

    @Test
    public void coveredSlotIsSkipped() {
        List<ReservedSlots.Slot> layout = layout();
        ReservedSlots.setSlotsForTests(layout);
        final APosition g1 = label(layout, "G1").tile;
        final APosition g2 = label(layout, "G2").tile;

        Set<String> covered = new HashSet<String>();
        covered.add(g1.tx() + "," + g1.ty());
        APosition tile = ReservedSlots.firstFreeSlotOfType(
            AUnitType.Protoss_Gateway,
            (t, b) -> covered.contains(t.tx() + "," + t.ty()));

        assertNotNull(tile, "a covered G1 skips, never declines while slots stay free");
        assertEquals(g2.tx(), tile.tx(), "the next free slot");
        assertEquals(g2.ty(), tile.ty(), "the next free slot");
    }

    @Test
    public void allCoveredMeansNoSlot() {
        List<ReservedSlots.Slot> layout = layout();
        ReservedSlots.setSlotsForTests(layout);

        Set<String> covered = new HashSet<String>();
        for (ReservedSlots.Slot slot : layout) {
            if (slot.type.equals(AUnitType.Protoss_Gateway)) {
                covered.add(slot.tile.tx() + "," + slot.tile.ty());
            }
        }
        APosition tile = ReservedSlots.firstFreeSlotOfType(
            AUnitType.Protoss_Gateway,
            (t, b) -> covered.contains(t.tx() + "," + t.ty()));

        assertNull(tile, "full slots fall through to the hunt, never invent a tile");
    }

    @Test
    public void unknownTypeAndNullsReadAsNoSlot() {
        ReservedSlots.setSlotsForTests(layout());

        assertNull(ReservedSlots.firstFreeSlotOfType(
            AUnitType.Protoss_Forge, (t, b) -> false),
            "no planned slots for tech - the hunt owns it");
        assertNull(ReservedSlots.firstFreeSlotOfType(null, (t, b) -> false),
            "null type never serves");
        assertNull(ReservedSlots.firstFreeSlotOfType(AUnitType.Protoss_Gateway, null),
            "null occupancy never serves");
        ReservedSlots.clearForTests();
        assertNull(ReservedSlots.firstFreeSlotOfType(
            AUnitType.Protoss_Gateway, (t, b) -> false),
            "no layout means no slot");
        assertTrue(true, "fail-closed paths never throw");
    }

    private static ReservedSlots.Slot label(List<ReservedSlots.Slot> layout, String label) {
        for (ReservedSlots.Slot slot : layout) {
            if (slot.label.equals(label)) return slot;
        }
        throw new IllegalArgumentException("no slot " + label);
    }
}
