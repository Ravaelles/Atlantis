package tests.unit;

import atlantis.battle.BattleCommander;
import atlantis.battle.cluster.FightOrFlight;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Choke doctrine wiring (NEXT #170): with no contact, a BASED line near a
 * contested choke holds the standoff instead of bunching on the leader,
 * and one unit detaches as bait. Geometry is pinned so the contested
 * choke (28,18) is the unit's nearest: dragoons sit 11-13 tiles south of
 * it (inside DETECT_TILES 15 but outside the oursNear 12 ring, so the
 * line holds instead of crushing), the zealot sits 7 tiles north-east of
 * it (inside CONTEST_TILES 12) but 15+ tiles from the dragoons (outside
 * the 9-tile engage envelope and the 15-tile enemiesNear ring, so no
 * fight and no mid-fight bait veto).
 */
@Tag("combat")
public class ChokeStandoffTest extends WorldStubForTests {

    @Test
    public void lineHoldsStandoffInsteadOfBunching() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 28, 29);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 34, 14);
        zealot.setEnemy();

        world(5, fakeOurs(nexus, dragoon), fakeEnemies(zealot), () -> {
            for (int i = 0; i < 5; i++) {
                BattleCommander.act(dragoon, FightOrFlight.Tactic.FIGHT, null, dragoon);
            }
            APosition choke = ((atlantis.map.position.HasPosition) dragoon).nearestChoke().center();
            APosition dest = dragoon.targetPosition;
            assertNotNull(dest, "holds somewhere instead of idling: " + dragoon.lastCommand);
            assertTrue(dest.distTo(choke) >= 7 && dest.distTo(choke) <= 12,
                "standoff ring around the choke, not the center itself: " + dest + " vs " + choke);
            assertTrue(dest.distTo(dragoon.position()) < choke.distTo(dragoon.position()),
                "on our side of the center, between us and it");
        });
    }

    @Test
    public void baitDetachesWhileLineHolds() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit a = fake(AUnitType.Protoss_Dragoon, 28, 31);
        FakeUnit b = fake(AUnitType.Protoss_Dragoon, 29, 31);
        FakeUnit c = fake(AUnitType.Protoss_Dragoon, 30, 30);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 34, 14);
        zealot.setEnemy();

        world(5, fakeOurs(nexus, a, b, c), fakeEnemies(zealot), () -> {
            AUnit bait = atlantis.battle.strategy.SquadScout
                .pickBait(atlantis.units.select.Select.ourCombatUnits());
            assertNotNull(bait, "someone volunteers");
            for (AUnit u : new AUnit[] { a, b, c }) {
                BattleCommander.act((FakeUnit) u, FightOrFlight.Tactic.FIGHT, null, (FakeUnit) u);
            }
            APosition choke = ((atlantis.map.position.HasPosition) a).nearestChoke().center();
            APosition baitDest = ((FakeUnit) bait).targetPosition;
            assertNotNull(baitDest, "bait walks into the funnel mouth");
            assertTrue(baitDest.distTo(choke) < 5,
                "bait at the mouth, not wandering: " + baitDest + " vs " + choke);
            for (AUnit u : new AUnit[] { a, b, c }) {
                if (u.id() == bait.id()) continue;
                APosition hold = ((FakeUnit) u).targetPosition;
                assertNotNull(hold, "holders take the standoff, not the bunch");
                assertTrue(hold.distTo(choke) >= 7, "holders keep their distance: " + hold);
            }
        });
    }
}
