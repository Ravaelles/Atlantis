package tests.unit;

import atlantis.battle.FormationHold;
import atlantis.battle.cluster.FightOrFlight;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Formation hold (owner 2026-10-26, Stardust doctrine): while the enemy
 * is melee-only and nobody is in contact, the line holds instead of
 * dissolving into a charge. Contact means 2.5 tiles, damage taken, or
 * own ranged able to fire.
 */
@Tag("combat")
public class FormationHoldTest extends WorldStubForTests {

    @Test
    public void meleeOnlyEnemyFarAwayHolds() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 20, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 40, 20);
        zealot.setEnemy();

        world(2, fakeOurs(dragoon), fakeEnemies(zealot), () -> {
            if (atlantis.game.A.now() != 1) return;

            assertTrue(FormationHold.shouldHold(dragoon, zealot, FightOrFlight.Tactic.FIGHT),
                "melee-only enemy, nobody in contact: hold the line");
        });
    }

    @Test
    public void rangedEnemyNeverHolds() {
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 20, 20);
        FakeUnit foeZealot = fake(AUnitType.Protoss_Zealot, 40, 20);
        foeZealot.setEnemy();
        FakeUnit foeDragoon = fake(AUnitType.Protoss_Dragoon, 42, 20);
        foeDragoon.setEnemy();

        world(2, fakeOurs(zealot), fakeEnemies(foeZealot, foeDragoon), () -> {
            if (atlantis.game.A.now() != 1) return;

            assertFalse(FormationHold.shouldHold(zealot, foeZealot, FightOrFlight.Tactic.FIGHT),
                "a ranged enemy means the arc would march into fire: fight as before");
        });
    }

    @Test
    public void fleeNeverHolds() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 20, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 40, 20);
        zealot.setEnemy();

        world(2, fakeOurs(dragoon), fakeEnemies(zealot), () -> {
            if (atlantis.game.A.now() != 1) return;

            assertFalse(FormationHold.shouldHold(dragoon, zealot, FightOrFlight.Tactic.FLEE),
                "retreats own the unit above the hold");
        });
    }

    @Test
    public void ownRangedAbleToFireBreaks() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 20, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 23, 20);
        zealot.setEnemy();

        world(2, fakeOurs(dragoon), fakeEnemies(zealot), () -> {
            if (atlantis.game.A.now() != 1) return;

            assertFalse(FormationHold.shouldHold(dragoon, zealot, FightOrFlight.Tactic.FIGHT),
                "dragoon reaches 3 tiles: the guns own the unit now");
        });
    }

    @Test
    public void contactBreaks() {
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 20, 20);
        FakeUnit foe = fake(AUnitType.Protoss_Zealot, 22, 20);
        foe.setEnemy();

        world(2, fakeOurs(zealot), fakeEnemies(foe), () -> {
            if (atlantis.game.A.now() != 1) return;

            assertFalse(FormationHold.shouldHold(zealot, foe, FightOrFlight.Tactic.FIGHT),
                "2 tiles is contact: the phalanx becomes the fight");
        });
    }
}
