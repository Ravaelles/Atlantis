package tests.unit.sim;

import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Feature snapshot pins: fractions for cross-unit policies, counts for
 * pack pressure, -1/zeros (never an exception) for the dead or the
 * unknown. The learner gates on these rows (NEXT #185 Stage C).
 */
@Tag("combat")
public class BattleFeaturesTest extends WorldStubForTests {

    @Test
    public void snapshotReadsTheWorld() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit d = fake(AUnitType.Protoss_Dragoon, 30, 20);
        d.hp = 60;
        d.shields = 40;
        d.cooldown = 15;
        FakeUnit a = fake(AUnitType.Protoss_Zealot, 32, 20);
        FakeUnit l1 = fake(AUnitType.Zerg_Zergling, 31, 20);
        l1.setEnemy();
        FakeUnit l2 = fake(AUnitType.Zerg_Zergling, 38, 20);
        l2.setEnemy();

        world(3, fakeOurs(nexus, d, a), fakeEnemies(l1, l2), () -> {
            if (atlantis.game.A.now() != 1) return;

            BattleFeatures f = BattleFeatures.snapshot(d);
            assertTrue(Math.abs(f.hpFrac - 60.0 / d.maxHp()) < 0.01, "hp fraction: " + f.hpFrac);
            assertTrue(Math.abs(f.cooldownFrac - 15.0 / d.cooldownAbsolute()) < 0.01,
                "cooldown fraction: " + f.cooldownFrac);
            assertEquals(1, f.foesMelee2, "one ling inside 2: " + f.foesMelee2);
            assertEquals(2, f.foesNear9, "two lings inside 9: " + f.foesNear9);
            assertTrue(f.nearestFoeDist > 0 && f.nearestFoeDist < 2, "nearest: " + f.nearestFoeDist);
            assertTrue(f.friendsNear >= 1, "the zealot counts: " + f.friendsNear);
            assertEquals(7, f.toCsv().split(",").length, "seven-column manifest row: " + f.toCsv());
        });
    }

    @Test
    public void deadOrNullReadsZero() {
        FakeUnit d = fake(AUnitType.Protoss_Dragoon, 30, 20);
        d.hp = 0;

        BattleFeatures dead = BattleFeatures.snapshot(d);
        assertEquals(0, dead.hpFrac, 0.001, "corpses read zero");
        assertEquals(-1, dead.nearestFoeDist, 0.001, "no foe known");

        BattleFeatures nil = BattleFeatures.snapshot(null);
        assertEquals(0, nil.hpFrac, 0.001, "null never throws");
    }
}
