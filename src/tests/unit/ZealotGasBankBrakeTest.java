package tests.unit;

import atlantis.production.dynamic.protoss.units.ProduceZealot;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Banked gas means Dragoons, not more Zealots (owner report: 14 Zealots
 * vs 6 Dragoons with 350 gas banked vs Zerg). The mineral-flood zealot
 * paths (LotsOfMinerals, WealthyGoZealots, NeedToProduceZealots...) have
 * no gas brake, so a rich bank keeps minting zealots while the gas that
 * would make them Dragoons sits idle. Past a banked-gas line with the
 * zealot floor already met, the zealot pump yields and the dragoon pump
 * (which runs first) takes the gateways.
 */
@Tag("production")
public class ZealotGasBankBrakeTest extends WorldStubForTests {

    @Test
    public void bankedGasStopsFloodZealotsVsZerg() {
        enemyRaceInWorld = Race.Zerg;

        currentMinerals = 600;
        currentGas = 350;
        currentSupplyUsed = 30;
        currentSupplyTotal = 40;

        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit gatewayA = fake(AUnitType.Protoss_Gateway, 24, 24);
        FakeUnit gatewayB = fake(AUnitType.Protoss_Gateway, 25, 24);
        FakeUnit core = fake(AUnitType.Protoss_Cybernetics_Core, 26, 26);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 21, 21);

        world(5, fakeOurs(
            nexus, gatewayA, gatewayB, core, probe,
            fake(AUnitType.Protoss_Zealot, 22, 21),
            fake(AUnitType.Protoss_Zealot, 22, 22),
            fake(AUnitType.Protoss_Zealot, 23, 21),
            fake(AUnitType.Protoss_Zealot, 23, 22),
            fake(AUnitType.Protoss_Zealot, 24, 21),
            fake(AUnitType.Protoss_Dragoon, 28, 22),
            fake(AUnitType.Protoss_Dragoon, 29, 22),
            fake(AUnitType.Protoss_Dragoon, 30, 22)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            assertFalse(ProduceZealot.zealot(), "350 banked gas with the floor met: no more flood zealots, goons take the gateways - "
                + ProduceZealot.reason);
        });
    }

    @Test
    public void leanGasKeepsZealotsFlowing() {
        enemyRaceInWorld = Race.Zerg;

        currentMinerals = 600;
        currentGas = 100;
        currentSupplyUsed = 30;
        currentSupplyTotal = 40;

        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit gatewayA = fake(AUnitType.Protoss_Gateway, 24, 24);
        FakeUnit gatewayB = fake(AUnitType.Protoss_Gateway, 25, 24);
        FakeUnit core = fake(AUnitType.Protoss_Cybernetics_Core, 26, 26);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 21, 21);

        world(5, fakeOurs(
            nexus, gatewayA, gatewayB, core, probe,
            fake(AUnitType.Protoss_Zealot, 22, 21),
            fake(AUnitType.Protoss_Zealot, 22, 22),
            fake(AUnitType.Protoss_Zealot, 23, 21),
            fake(AUnitType.Protoss_Zealot, 23, 22),
            fake(AUnitType.Protoss_Zealot, 24, 21),
            fake(AUnitType.Protoss_Dragoon, 28, 22),
            fake(AUnitType.Protoss_Dragoon, 29, 22),
            fake(AUnitType.Protoss_Dragoon, 30, 22)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            assertTrue(ProduceZealot.zealot(),
                "100 gas is not a bank: the mineral flood still zeals - " + ProduceZealot.reason);
        });
    }

    @Test
    public void floorStillZealsWhenThinDespiteBank() {
        enemyRaceInWorld = Race.Zerg;

        currentMinerals = 600;
        currentGas = 350;
        currentSupplyUsed = 30;
        currentSupplyTotal = 40;

        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit gatewayA = fake(AUnitType.Protoss_Gateway, 24, 24);
        FakeUnit gatewayB = fake(AUnitType.Protoss_Gateway, 25, 24);
        FakeUnit core = fake(AUnitType.Protoss_Cybernetics_Core, 26, 26);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 21, 21);
        FakeUnit ling1 = fake(AUnitType.Zerg_Zergling, 40, 20);
        FakeUnit ling2 = fake(AUnitType.Zerg_Zergling, 41, 20);
        FakeUnit ling3 = fake(AUnitType.Zerg_Zergling, 42, 20);
        FakeUnit ling4 = fake(AUnitType.Zerg_Zergling, 43, 20);
        FakeUnit ling5 = fake(AUnitType.Zerg_Zergling, 44, 20);
        FakeUnit ling6 = fake(AUnitType.Zerg_Zergling, 45, 20);
        ling1.setEnemy();
        ling2.setEnemy();
        ling3.setEnemy();
        ling4.setEnemy();
        ling5.setEnemy();
        ling6.setEnemy();

        world(5, fakeOurs(
            nexus, gatewayA, gatewayB, core, probe,
            fake(AUnitType.Protoss_Dragoon, 28, 22),
            fake(AUnitType.Protoss_Dragoon, 29, 22),
            fake(AUnitType.Protoss_Dragoon, 30, 22)
        ), fakeEnemies(ling1, ling2, ling3, ling4, ling5, ling6), () -> {
            if (atlantis.game.A.now() != 3) return;

            assertTrue(ProduceZealot.zealot(),
                "no zealots vs 6 lings: the floor still needs meat even banked - " + ProduceZealot.reason);
        });
    }
}
