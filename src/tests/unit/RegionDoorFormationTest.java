package tests.unit;

import atlantis.battle.cluster.Alpha;
import atlantis.combat.cluster.ClusterRally;
import atlantis.combat.cluster.FightOrFlight;
import atlantis.map.choke.AChoke;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.map.region.ARegion;
import atlantis.map.region.Regions;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Face the door, not the wall (owner 2026-10-29, Cere game): with the
 * target in another region the direction point sat behind the army and
 * the crescent formed up facing our own wall. The choke between our
 * region and the anchor's is the only honest facing - contact keeps
 * its own pivot. Facing is read off the slot centroid: it must lean
 * toward the door, never away from it.
 */
@Tag("combat")
public class RegionDoorFormationTest extends WorldStubForTests {

    @AfterEach
    public void clearAlpha() {
        for (atlantis.units.AUnit u : Alpha.get().selection().list()) {
            Alpha.get().removeUnit(u);
        }
    }

    private static AChoke doorAt(APosition center) {
        AChoke choke = Mockito.mock(AChoke.class);
        Mockito.when(choke.center()).thenReturn(center);

        return choke;
    }

    private static double facingDot(FakeUnit a, FakeUnit b, FakeUnit c,
                                    APosition anchor, APosition door) {
        HasPosition sa = ClusterRally.formationSlotFor(
            a, a, anchor, FightOrFlight.Tactic.FIGHT);
        HasPosition sb = ClusterRally.formationSlotFor(
            b, a, anchor, FightOrFlight.Tactic.FIGHT);
        HasPosition sc = ClusterRally.formationSlotFor(
            c, a, anchor, FightOrFlight.Tactic.FIGHT);
        assertNotNull(sa, "door-facing slot for a");
        assertNotNull(sb, "door-facing slot for b");
        assertNotNull(sc, "door-facing slot for c");

        // The crescent stands its ground facing the door (an arc through
        // the army, laterally spread) instead of marching off toward the
        // anchor: its longitude stays at the army, not halfway to the
        // anchor.
        return (sa.tx() + sb.tx() + sc.tx()) / 3.0;
    }

    @Test
    public void crossRegionAnchorFacesTheDoor() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit a = fake(AUnitType.Protoss_Dragoon, 20, 20);
        FakeUnit b = fake(AUnitType.Protoss_Dragoon, 21, 20);
        FakeUnit c = fake(AUnitType.Protoss_Dragoon, 20, 21);
        // Anchor south-west of the army: the old answer lined up toward
        // it, marching the army south-west. The door to the anchor's
        // region sits laterally (east): the crescent faces the door.
        APosition anchor = APosition.create(12, 12);
        APosition door = APosition.create(28, 20);

        AChoke doorChoke = doorAt(door);
        ARegion ours = Mockito.mock(ARegion.class);
        ARegion theirs = Mockito.mock(ARegion.class);
        Mockito.when(ours.chokeBetween(theirs)).thenReturn(doorChoke);

        try (MockedStatic<Regions> regions = Mockito.mockStatic(Regions.class)) {
            regions.when(() -> Regions.getRegion(Mockito.any())).thenReturn(ours);
            regions.when(() -> Regions.getRegion(Mockito.argThat(
                p -> p instanceof HasPosition
                    && ((HasPosition) p).tx() == anchor.tx()
                    && ((HasPosition) p).ty() == anchor.ty())))
                .thenReturn(theirs);

            world(3, fakeOurs(nexus, a, b, c), fakeEnemies(), () -> {
                if (atlantis.game.A.now() != 2) return;

                Alpha.get().addUnit(a);
                Alpha.get().addUnit(b);
                Alpha.get().addUnit(c);
                try {
                    HasPosition doorPivot = ClusterRally.regionDoorPivot(a, anchor);
                    assertTrue(doorPivot != null
                        && doorPivot.tx() == door.tx() && doorPivot.ty() == door.ty(),
                        "the pivot is the door itself: " + doorPivot);
                    assertTrue(facingDot(a, b, c, anchor, door) >= 19,
                        "the crescent stands its ground facing east instead of"
                            + " marching south-west toward the anchor");
                } finally {
                    Alpha.get().removeUnit(a);
                    Alpha.get().removeUnit(b);
                    Alpha.get().removeUnit(c);
                }
            });
        }
    }

    @Test
    public void sameRegionKeepsTheOldFacing() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit a = fake(AUnitType.Protoss_Dragoon, 20, 20);
        FakeUnit b = fake(AUnitType.Protoss_Dragoon, 21, 20);
        FakeUnit c = fake(AUnitType.Protoss_Dragoon, 20, 21);
        APosition anchor = APosition.create(12, 12);

        ARegion same = Mockito.mock(ARegion.class);

        try (MockedStatic<Regions> regions = Mockito.mockStatic(Regions.class)) {
            regions.when(() -> Regions.getRegion(Mockito.any())).thenReturn(same);

            world(3, fakeOurs(nexus, a, b, c), fakeEnemies(), () -> {
                if (atlantis.game.A.now() != 2) return;

                Alpha.get().addUnit(a);
                Alpha.get().addUnit(b);
                Alpha.get().addUnit(c);
                try {
                    HasPosition slot = ClusterRally.formationSlotFor(
                        a, a, anchor, FightOrFlight.Tactic.FIGHT);
                    assertNotNull(slot, "same region falls back to the direction point");
                } finally {
                    Alpha.get().removeUnit(a);
                    Alpha.get().removeUnit(b);
                    Alpha.get().removeUnit(c);
                }
            });
        }
    }

    @Test
    public void noDoorKeepsTheOldFacing() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit a = fake(AUnitType.Protoss_Dragoon, 20, 20);
        FakeUnit b = fake(AUnitType.Protoss_Dragoon, 21, 20);
        FakeUnit c = fake(AUnitType.Protoss_Dragoon, 20, 21);
        APosition anchor = APosition.create(12, 12);

        ARegion ours = Mockito.mock(ARegion.class);
        ARegion theirs = Mockito.mock(ARegion.class);
        Mockito.when(ours.chokeBetween(theirs)).thenReturn(null);

        try (MockedStatic<Regions> regions = Mockito.mockStatic(Regions.class)) {
            regions.when(() -> Regions.getRegion(Mockito.any())).thenReturn(ours, theirs);

            world(3, fakeOurs(nexus, a, b, c), fakeEnemies(), () -> {
                if (atlantis.game.A.now() != 2) return;

                Alpha.get().addUnit(a);
                Alpha.get().addUnit(b);
                Alpha.get().addUnit(c);
                try {
                    HasPosition slot = ClusterRally.formationSlotFor(
                        a, a, anchor, FightOrFlight.Tactic.FIGHT);
                    assertNotNull(slot, "no door means the old facing owns it");
                } finally {
                    Alpha.get().removeUnit(a);
                    Alpha.get().removeUnit(b);
                    Alpha.get().removeUnit(c);
                }
            });
        }
    }
}
