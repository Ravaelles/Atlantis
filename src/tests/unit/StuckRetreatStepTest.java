package tests.unit;

import atlantis.battle.DuelMemory;
import atlantis.combat.retreating.RetreatFromLostFight;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Stuck-retreat escape (owner 2026-10-29, Cere game): dragoons piled into
 * each other for ~190 frames churning ClusterFlee/OrganizedRetreat/Kite
 * while dying - a retreat that does not displace is not a retreat. A
 * retreating unit stuck in place gets a local step away from the nearest
 * enemy combat unit instead of re-marching the far rally it never reaches.
 */
@Tag("combat")
public class StuckRetreatStepTest extends WorldStubForTests {

    @AfterEach
    public void clearMotion() {
        DuelMemory.clear();
    }

    private static FakeUnit dragoon() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);

        return dragoon;
    }

    private static FakeUnit ling() {
        FakeUnit ling = fake(AUnitType.Zerg_Zergling, 32, 20);
        ling.setEnemy();

        return ling;
    }

    @Test
    public void freshRetreatKeepsTheRally() {
        FakeUnit dragoon = dragoon();
        FakeUnit ling = ling();
        HasPosition rally = APosition.create(10, 10);

        world(3, fakeOurs(dragoon), fakeEnemies(ling), () -> {
            if (DuelMemory.lastMovedFrame(dragoon) == null) {
                DuelMemory.noteMoved(dragoon, atlantis.game.A.now(), dragoon.position());
            }

            HasPosition dest = RetreatFromLostFight.stuckRetreatLocalStep(
                dragoon, rally, atlantis.game.A.now());

            assertEquals(rally, dest, "a moving unit marches the rally, not a local step");
        });
    }

    @Test
    public void stuckRetreatStepsAwayLocally() {
        FakeUnit dragoon = dragoon();
        FakeUnit ling = ling();
        HasPosition rally = APosition.create(10, 10);

        boolean[] stepped = {false};

        world(60, fakeOurs(dragoon), fakeEnemies(ling), () -> {
            int now = atlantis.game.A.now();

            if (now == 0) {
                atlantis.information.enemy.EnemyUnitsUpdater.weDiscoveredEnemyUnit(ling);
            }
            if (DuelMemory.lastMovedFrame(dragoon) == null) {
                DuelMemory.noteMoved(dragoon, now, dragoon.position());
            }

            HasPosition dest = RetreatFromLostFight.stuckRetreatLocalStep(
                dragoon, rally, now);

            if (!rally.equals(dest)) stepped[0] = true;
        });

        assertTrue(stepped[0],
            "48f without displacement means the rally is unreachable - step away instead");
    }

    @Test
    public void stuckRetreatWithoutThreatKeepsTheRally() {
        FakeUnit dragoon = dragoon();
        HasPosition rally = APosition.create(10, 10);

        world(60, fakeOurs(dragoon), fakeEnemies(), () -> {
            int now = atlantis.game.A.now();

            if (DuelMemory.lastMovedFrame(dragoon) == null) {
                DuelMemory.noteMoved(dragoon, now, dragoon.position());
            }

            HasPosition dest = RetreatFromLostFight.stuckRetreatLocalStep(
                dragoon, rally, now);

            assertEquals(rally, dest, "nothing to step from - same as today, never worse");
        });
    }
}
