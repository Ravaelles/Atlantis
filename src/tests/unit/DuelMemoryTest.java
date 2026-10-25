package tests.unit;

import atlantis.combat.BattleCommander;
import atlantis.battle.DuelMemory;
import atlantis.game.A;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;

/**
 * Battle-hook damage window (NEXT #183): the latch answers from
 * per-frame hp snapshots taken in the battle hook, not from the
 * manager-updated history - which reads fresh by construction for a
 * steady unit and can never fire.
 */
@Tag("combat")
public class DuelMemoryTest extends WorldStubForTests {

    @Test
    public void damageLatchOpensOnHpDropAndClosesAfterTheWindow() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 60, 20);
        zealot.setEnemy();
        dragoon.hp = 100;
        dragoon.shields = 0;
        BattleCommander.clearForTests();

        try {
            world(60, fakeOurs(dragoon), fakeEnemies(zealot), () -> {
                int now = A.now();
                BattleCommander.act(
                    dragoon, atlantis.combat.cluster.FightOrFlight.Tactic.FIGHT, null, dragoon);

                if (now == 10) dragoon.hp = 90;
                if (now == 11) {
                    assertTrue(DuelMemory.damagedLessThanAgo(dragoon, 45),
                        "hp dropped 100 -> 90 between battle frames: the latch opens");
                }
                if (now == 56) {
                    assertTrue(DuelMemory.damagedLessThanAgo(dragoon, 45),
                        "45 frames after the drop the window still holds (inclusive edge)");
                }
                if (now == 57) {
                    assertFalse(DuelMemory.damagedLessThanAgo(dragoon, 45),
                        "46 frames after the drop with no new damage: the latch closes");
                }
            });
        }
        finally {
            BattleCommander.clearForTests();
        }
    }

    @Test
    public void undamagedUnitNeverTripsTheLatch() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 60, 20);
        zealot.setEnemy();
        dragoon.hp = 100;
        dragoon.shields = 0;
        BattleCommander.clearForTests();

        try {
            world(3, fakeOurs(dragoon), fakeEnemies(zealot), () -> {
                BattleCommander.act(
                    dragoon, atlantis.combat.cluster.FightOrFlight.Tactic.FIGHT, null, dragoon);
                if (A.now() == 2) {
                    assertFalse(DuelMemory.damagedLessThanAgo(dragoon, 45),
                        "no hp drop, no latch - a calm holder reads clean");
                }
            });
        }
        finally {
            BattleCommander.clearForTests();
        }
    }
}
