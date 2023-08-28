package tests.acceptance;

import atlantis.combat.micro.avoid.terran.TerranFightInsteadAvoid;
import atlantis.combat.micro.avoid.terran.TerranFightInsteadAvoidAsWraith;
import atlantis.production.BuildingsCommander;
import atlantis.units.AUnitType;
import org.junit.Test;
import tests.unit.FakeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TerranFightInsteadAvoidTest extends AbstractTestFakingGame {
    private FakeUnit ourWraith;
    private FakeUnit enemyWraith;

    @Test
    public void iteratesOverBuildings() {
        createWorld(1, () -> {
            TerranFightInsteadAvoid manager = new TerranFightInsteadAvoid(ourWraith);

//            System.err.println("manager.applies() = " + manager.applies());
//            System.err.println("manager.invoke() = " + manager.invoke());

            assertTrue(manager.applies());
            assertEquals(new TerranFightInsteadAvoidAsWraith(ourWraith), manager.invoke());
            
//            System.err.println(ourWraith.managerLogs().toString());
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
            fake(AUnitType.Zerg_Creep_Colony, 35),
            fake(AUnitType.Zerg_Sunken_Colony, 36),
            fake(AUnitType.Zerg_Spore_Colony, 37),
            enemyWraith = fake(AUnitType.Terran_Wraith, 38)
        );
    }
}