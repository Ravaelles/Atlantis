package tests.unit;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import atlantis.battle.escape.EscapeDestination;
import atlantis.battle.escape.EscapeRays;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;

/**
 * Owner rule (Steamhammer loss: two Dragoons died shooting nothing,
 * glued on a Gateway): an escape step never ends within 1.5 tiles of
 * a building's edge. Edge-aware (footprint center + half-extent):
 * a Gateway rejects wider than a Pylon.
 */
@Tag("combat")
public class EscapeBuildingClearanceTest extends WorldStubForTests {

    private static EscapeRays.Walkability openField() {
        return (x, y) -> x >= 0 && y >= 0;
    }

    private static List<HasPosition> noEnemies() {
        return new ArrayList<>();
    }

    private static double edgeDist(APosition dest, FakeUnit building) {
        double half = building.type().bodyRadiusPx() / 32.0;

        return dest.distTo(building) - half;
    }

    @Test
    public void openGroundStaysBitIdentical() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 36, 20);
        FakeUnit threat = fake(AUnitType.Zerg_Zergling, 30, 20);
        threat.setEnemy();

        APosition dest = EscapeDestination.pickEscape(
            dragoon, threat, 2, openField(), new ArrayList<>(), noEnemies());

        assertNotNull(dest, "open ground still steps");
        assertEquals(38, dest.tx(), "away-east, as before");
        assertTrue(dest.ty() == 18 || dest.ty() == 22,
            "pressure prefers the slightly-farther diagonal, unchanged: " + dest.ty());
    }

    @Test
    public void gatewayHuggingDestIsVetoed() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 36, 20);
        FakeUnit threat = fake(AUnitType.Zerg_Zergling, 30, 20);
        threat.setEnemy();
        FakeUnit gateway = fake(AUnitType.Protoss_Gateway, 40, 20);
        List<HasPosition> blockers = new ArrayList<>();
        blockers.add(gateway);

        APosition dest = EscapeDestination.pickEscape(
            dragoon, threat, 2, openField(), blockers, noEnemies());

        if (dest != null) {
            assertTrue(edgeDist(dest, gateway) >= 1.5,
                "no step onto the Gateway's shirt: dest " + dest.tx() + "," + dest.ty()
                    + " edge " + edgeDist(dest, gateway));
            assertFalse(dest.tx() == 38 && dest.ty() == 20,
                "the straight-back tile hugs the Gateway wall and must not win");
        }
    }

    @Test
    public void mineralsKeepOnlyTheSoftPenalty() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 36, 20);
        FakeUnit threat = fake(AUnitType.Zerg_Zergling, 30, 20);
        threat.setEnemy();
        FakeUnit mineral = fake(AUnitType.Resource_Mineral_Field, 38, 20);
        List<HasPosition> blockers = new ArrayList<>();
        blockers.add(mineral);

        assertFalse(EscapeDestination.violatesBuildingClearance(
            APosition.create(38, 20), blockers),
            "minerals are walk-around cover, not a hard veto");
    }

    @Test
    public void nullInputsNeverVeto() {
        FakeUnit gateway = fake(AUnitType.Protoss_Gateway, 40, 20);
        List<HasPosition> blockers = new ArrayList<>();
        blockers.add(gateway);

        assertFalse(EscapeDestination.violatesBuildingClearance(null, blockers),
            "no dest: the caller owns that exit");
        assertFalse(EscapeDestination.violatesBuildingClearance(
            APosition.create(38, 20), null),
            "no blockers: the step stands");
        assertFalse(EscapeDestination.violatesBuildingClearance(
            APosition.create(38, 20), new ArrayList<>()),
            "empty blockers: the step stands");
    }

    @Test
    public void pylonRejectsNarrowerThanGateway() {
        FakeUnit pylon = fake(AUnitType.Protoss_Pylon, 40, 20);
        List<HasPosition> blockers = new ArrayList<>();
        blockers.add(pylon);

        // Pylon edge at ~1 tile from center: 2.5 out is clear, 2.0 is not.
        assertFalse(EscapeDestination.violatesBuildingClearance(
            APosition.create(43, 20), blockers), "2.5 + edge room: clear");
        assertTrue(EscapeDestination.violatesBuildingClearance(
            APosition.create(42, 20), blockers), "2.0 from center hugs the Pylon");
    }
}
