package tests.unit;

import atlantis.production.dynamic.expansion.protoss.ProtossShouldExpand;
import atlantis.production.orders.production.queue.Queue;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * The 2-Nexus goal: with the bank covering the Nexus (400+), an army
 * standing (4+), one base, nothing in production, and no enemy pressure
 * anywhere (natural, choke, base), the second base must be decided now -
 * not after 11+ combat units (DelayNaturalVsZerg) or 550+ bank (TooFewArmy).
 * Rush safety stays on observed pressure, not on unit-count proxies: a
 * 4pool never coexists with a 400 bank (minerals gate), and pressure at
 * the base/choke/natural blocks through BaseUnderAttack and the choke
 * reads.
 */
@Tag("production")
public class ExpandSecondBaseWhenSafeTest extends AbstractTestWithWorld {

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
    public void safeAndAffordableSecondBase() {
        currentMinerals = 450;
        currentGas = 100;
        currentSupplyUsed = 20;
        currentSupplyTotal = 40;

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Gateway, 24, 20),
            fake(AUnitType.Protoss_Zealot, 30, 20),
            fake(AUnitType.Protoss_Zealot, 31, 20),
            fake(AUnitType.Protoss_Zealot, 32, 20),
            fake(AUnitType.Protoss_Zealot, 33, 20),
            fake(AUnitType.Protoss_Zealot, 34, 20)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();

            assertTrue(
                ProtossShouldExpand.shouldExpand(),
                "450 bank + 5 Zealots + one base + no pressure must decide the natural");
        });
    }
}
