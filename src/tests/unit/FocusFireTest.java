package tests.unit;

import atlantis.battle.formation.FocusFire;
import atlantis.map.bullets.DeadMan;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.attacked_by.Bullets;
import bwapi.Race;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeBullet;
import tests.fakes.FakeBullets;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Pins the shared focus target (formation Phase A): the weakest valid
 * enemy in reach wins; equal health breaks toward the enemy in reach of
 * the most guns; dead men (in-flight damage already lethal) never win.
 * Single-enemy situations answer that one enemy, so duels cannot change.
 */
public class FocusFireTest extends AbstractTestWithWorld {

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @Test
    public void weakestEnemyInReachWins() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 10, 10);
        FakeUnit healthy = fake(AUnitType.Protoss_Zealot, 13, 10);
        FakeUnit wounded = fake(AUnitType.Protoss_Zealot, 14, 10);
        wounded.setHp(20);

        world(1, fakeOurs(dragoon), fakeEnemies(healthy, wounded), () -> {
            AUnit shared = FocusFire.sharedTarget(dragoon, 9);

            assertEquals(wounded, shared,
                    "the wounded zealot draws the shared fire, not the nearest healthy one");
        });
    }

    @Test
    public void singleEnemyAnswersItself() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 10, 10);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 13, 10);

        world(1, fakeOurs(dragoon), fakeEnemies(zealot), () -> {
            assertEquals(zealot, FocusFire.sharedTarget(dragoon, 9),
                    "one enemy means the shared target is that enemy - duels cannot change");
        });
    }

    @Test
    public void noValidEnemyAnswersNull() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 10, 10);
        FakeUnit far = fake(AUnitType.Protoss_Zealot, 40, 40);

        world(1, fakeOurs(dragoon), fakeEnemies(far), () -> {
            assertNull(FocusFire.sharedTarget(dragoon, 9),
                    "nothing in reach means no shared target - the caller falls back");
        });
    }

    @Test
    public void deadManNeverWinsFocus() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 10, 10);
        FakeUnit healthy = fake(AUnitType.Protoss_Zealot, 13, 10);
        FakeUnit doomed = fake(AUnitType.Protoss_Zealot, 13, 11);
        doomed.setHp(25);

        world(15, fakeOurs(dragoon), fakeEnemies(healthy, doomed), () -> {
            if (atlantis.game.A.now() != 10) return;

            for (int i = 0; i < 3; i++) {
                FakeBullet bullet = FakeBullet.fromPosition(dragoon.position, dragoon, doomed);
                FakeBullets.allBullets.add(bullet);
            }
            Bullets.updateKnown();

            if (!DeadMan.isDeadMan(doomed)) return;

            assertEquals(healthy, FocusFire.sharedTarget(dragoon, 9),
                    "in-flight damage already kills the doomed zealot - focus stays on the healthy one");
        });
    }

    @Test
    public void mostGunsBreakEqualHealth() {
        FakeUnit d1 = fake(AUnitType.Protoss_Dragoon, 10, 10);
        FakeUnit d2 = fake(AUnitType.Protoss_Dragoon, 16, 10);
        FakeUnit e1 = fake(AUnitType.Protoss_Zealot, 14, 10);
        FakeUnit e2 = fake(AUnitType.Protoss_Zealot, 10, 13);
        e1.setHp(50);
        e2.setHp(50);

        world(1, fakeOurs(d1, d2), fakeEnemies(e1, e2), () -> {
            assertEquals(e1, FocusFire.sharedTarget(d1, 4),
                    "equal health: e1 is in reach of both guns, e2 of one - pile onto e1");
        });
    }

    @Test
    public void uncommittedUnitJoinsWeakestThroughAct() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 10, 10);
        dragoon.cooldown = 0;
        FakeUnit strong = fake(AUnitType.Protoss_Zealot, 12, 10);
        FakeUnit weak = fake(AUnitType.Protoss_Zealot, 14, 10);
        weak.setHp(20);

        world(1, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            dragoon
        ), fakeEnemies(strong, weak), () -> {
            atlantis.battle.BattleCommander.act(
                dragoon, atlantis.battle.FightOrFlight.Tactic.FIGHT, null, dragoon);

            assertEquals(weak, dragoon.target(),
                    "no current target, no squad: the weaker zealot draws the fire, "
                        + "not the nearer healthy one");
        });
    }

    @Test
    public void committedUnitFinishesItsOwnTarget() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 10, 10);
        dragoon.cooldown = 0;
        FakeUnit strong = fake(AUnitType.Protoss_Zealot, 13, 10);
        FakeUnit weak = fake(AUnitType.Protoss_Zealot, 14, 10);
        weak.setHp(20);

        world(1, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            dragoon
        ), fakeEnemies(strong, weak), () -> {
            dragoon.attackUnit(strong);
            ((tests.fakes.FakeOrderSink) dragoon.orderSink()).clear();

            atlantis.battle.BattleCommander.act(
                dragoon, atlantis.battle.FightOrFlight.Tactic.FIGHT, null, dragoon);

            assertEquals(strong, dragoon.target(),
                    "sticky first: the committed target survives a weaker distraction");
        });
    }
}
