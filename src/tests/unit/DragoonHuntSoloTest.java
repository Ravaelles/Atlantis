package tests.unit;

import atlantis.combat.FormationHold;
import atlantis.combat.behaviors.DragoonHuntSolo;
import atlantis.combat.cluster.FightOrFlight;
import atlantis.units.AUnitType;
import bwapi.Race;
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
 * Dragoon solo hunt in the early game (owner 2026-11-09): a shielded
 * Dragoon whose gun is about ready meets the Zergling instead of
 * holding formation. Consulted from the hold chain below harass and
 * the kite gates - a threatened unit still kites, a healthy duel
 * still harasses with its twin. Melee-only boards only.
 */
@Tag("combat")
public class DragoonHuntSoloTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @Override
    public Race initEnemyRace() {
        return Race.Zerg;
    }

    private static FakeUnit[] threeReadyDragoons(int tx, int ty) {
        FakeUnit[] goons = new FakeUnit[]{
            fake(AUnitType.Protoss_Dragoon, tx, ty),
            fake(AUnitType.Protoss_Dragoon, tx + 1, ty),
            fake(AUnitType.Protoss_Dragoon, tx, ty + 1),
        };
        for (FakeUnit goon : goons) {
            goon.shields = 40;
            goon.cooldown = 5;
        }

        return goons;
    }

    @Test
    public void shieldedReadyDragoonHuntsInsteadOfHolding() {
        FakeUnit[] goons = threeReadyDragoons(20, 20);
        FakeUnit ling = fake(AUnitType.Zerg_Zergling, 40, 20);
        ling.setEnemy();

        world(2, fakeOurs(goons), fakeEnemies(ling), () -> {
            if (atlantis.game.A.now() != 1) return;

            assertTrue(DragoonHuntSolo.applies(goons[0], ling),
                "early, melee-only, shields 40, cold gun, cooldown 5: hunt");
            assertFalse(FormationHold.shouldHold(goons[0], ling, FightOrFlight.Tactic.FIGHT),
                "no formation slot against a ling a shielded dragoon can kill");
        });
    }

    @Test
    public void weakShieldsKeepTheHold() {
        FakeUnit[] goons = threeReadyDragoons(20, 20);
        goons[0].shields = 14;
        FakeUnit ling = fake(AUnitType.Zerg_Zergling, 40, 20);
        ling.setEnemy();

        world(2, fakeOurs(goons), fakeEnemies(ling), () -> {
            if (atlantis.game.A.now() != 1) return;

            assertFalse(DragoonHuntSolo.applies(goons[0], ling),
                "14 shields is not 15: the gate stays shut");
            assertTrue(FormationHold.shouldHold(goons[0], ling, FightOrFlight.Tactic.FIGHT),
                "a chewed dragoon keeps its slot");
        });
    }

    @Test
    public void hotGunKeepsTheDuel() {
        FakeUnit[] goons = threeReadyDragoons(20, 20);
        goons[0].cooldown = 9;
        FakeUnit ling = fake(AUnitType.Zerg_Zergling, 40, 20);
        ling.setEnemy();

        world(2, fakeOurs(goons), fakeEnemies(ling), () -> {
            if (atlantis.game.A.now() != 1) return;

            assertFalse(DragoonHuntSolo.applies(goons[0], ling),
                "cooldown 9 is not ready: finish the duel first");
        });
    }

    @Test
    public void rangedEnemyKeepsTheHold() {
        FakeUnit[] goons = threeReadyDragoons(20, 20);
        FakeUnit ling = fake(AUnitType.Zerg_Zergling, 40, 20);
        ling.setEnemy();
        FakeUnit hydra = fake(AUnitType.Zerg_Hydralisk, 60, 20);
        hydra.setEnemy();

        world(2, fakeOurs(goons), fakeEnemies(ling, hydra), () -> {
            if (atlantis.game.A.now() != 1) return;

            assertFalse(DragoonHuntSolo.applies(goons[0], ling),
                "a known ranged enemy keeps the hold");
        });
    }

    @Test
    public void nonLingTargetKeepsTheHold() {
        FakeUnit[] goons = threeReadyDragoons(20, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 40, 20);
        zealot.setEnemy();

        world(2, fakeOurs(goons), fakeEnemies(zealot), () -> {
            if (atlantis.game.A.now() != 1) return;

            assertFalse(DragoonHuntSolo.applies(goons[0], zealot),
                "the hunt names lings, not zealots");
        });
    }

    @Test
    public void nonDragoonNeverHunts() {
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 20, 20);
        FakeUnit ling = fake(AUnitType.Zerg_Zergling, 40, 20);
        ling.setEnemy();

        world(2, fakeOurs(zealot), fakeEnemies(ling), () -> {
            if (atlantis.game.A.now() != 1) return;

            assertFalse(DragoonHuntSolo.applies(zealot, ling),
                "the hunt is a dragoon job");
        });
    }
}
