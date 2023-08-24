package tests.acceptance;

import atlantis.combat.micro.avoid.terran.TerranFightInsteadAvoid;
import atlantis.production.BuildingsCommander;
import atlantis.units.AUnitType;
import org.junit.Test;
import tests.unit.FakeUnit;

import static org.junit.Assert.assertTrue;

public class TerranFightInsteadAvoidTest extends AbstractTestFakingGame {
    private FakeUnit ourWraith;
    private FakeUnit enemyWraith;

    @Test
    public void iteratesOverBuildings() {
        createWorld(1, () -> {
            TerranFightInsteadAvoid manager = new TerranFightInsteadAvoid(ourWraith);

            System.out.println("manager.applies() = " + manager.applies());
            System.out.println("manager.invoke() = " + manager.invoke());
            assertTrue(manager.applies());
            assertTrue(manager.equals(manager.invoke()));

            System.out.println(ourWraith.managerLogs().toString());
        });
    }

    // =========================================================

    protected FakeUnit[] generateOur() {
        return fakeOurs(
            fake(AUnitType.Terran_SCV, 10),
            fake(AUnitType.Terran_Command_Center, 11),
            fake(AUnitType.Terran_Comsat_Station, 12),
            fake(AUnitType.Terran_Science_Vessel, 13),
            fake(AUnitType.Protoss_Shield_Battery, 14),
            ourWraith = fake(AUnitType.Terran_Wraith, 20)
        );
    }

    protected FakeUnit[] generateEnemies() {
        return fakeEnemies(
            fake(AUnitType.Zerg_Creep_Colony, 5),
            fake(AUnitType.Zerg_Sunken_Colony, 6),
            fake(AUnitType.Zerg_Spore_Colony, 7),
            enemyWraith = fake(AUnitType.Terran_Wraith, 24)
        );
    }
}
