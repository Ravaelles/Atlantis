package tests.unit;

import atlantis.battle.kite.ZealotWithdraw;
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
 * Zealot withdrawal (NEXT #178, owner 2026-10-21): lings attack faster
 * than zealots, so a zealot that just attacked stands through its whole
 * cooldown taking free hits. After attacking with cooldown left, a
 * WOUNDED zealot steps back; an unwounded one keeps trading contact.
 */
@Tag("combat")
public class ZealotWithdrawTest extends WorldStubForTests {

    @Test
    public void unwoundedZealotHoldsContact() {
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 30, 20);
        FakeUnit ling = fake(AUnitType.Zerg_Zergling, 32, 20);
        ling.setEnemy();

        world(1, fakeOurs(zealot), fakeEnemies(ling), () -> {
            zealot.cooldown = 15;
            assertFalse(ZealotWithdraw.should(zealot, ling),
                "full health: trading contact beats stepping back");
        });
    }

    @Test
    public void scratchedZealotHoldsContact() {
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 30, 20);
        FakeUnit ling = fake(AUnitType.Zerg_Zergling, 32, 20);
        ling.setEnemy();

        world(1, fakeOurs(zealot), fakeEnemies(ling), () -> {
            zealot.cooldown = 15;
            zealot.setHp(zealot.maxHp() - 10);
            assertFalse(ZealotWithdraw.should(zealot, ling),
                "10 damage is not a wound (owner): hold the line");
        });
    }

    @Test
    public void woundedZealotWithdrawsOnCooldown() {
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 30, 20);
        FakeUnit ling = fake(AUnitType.Zerg_Zergling, 32, 20);
        ling.setEnemy();

        world(1, fakeOurs(zealot, fake(AUnitType.Protoss_Zealot, 31, 20)), fakeEnemies(ling), () -> {
            zealot.cooldown = 15;
            zealot.setHp(zealot.maxHp() - 11);
            assertTrue(ZealotWithdraw.should(zealot, ling),
                "real wound + spent weapon: step back, save the HP");
        });
    }

    @Test
    public void freshWeaponNeverWithdraws() {
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 30, 20);
        FakeUnit ling = fake(AUnitType.Zerg_Zergling, 32, 20);
        ling.setEnemy();

        world(1, fakeOurs(zealot), fakeEnemies(ling), () -> {
            zealot.cooldown = 0;
            zealot.setHp(zealot.maxHp() - 50);
            assertFalse(ZealotWithdraw.should(zealot, ling),
                "weapon ready: shoot now, the step would cancel DPS");
        });
    }

    @Test
    public void dragoonsUnaffected() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit ling = fake(AUnitType.Zerg_Zergling, 32, 20);
        ling.setEnemy();

        world(1, fakeOurs(dragoon), fakeEnemies(ling), () -> {
            dragoon.cooldown = 15;
            assertFalse(ZealotWithdraw.should(dragoon, ling),
                "dragoon doctrine untouched by the zealot rule");
        });
    }
}
