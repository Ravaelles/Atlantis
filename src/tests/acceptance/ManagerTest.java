package tests.acceptance;

import atlantis.architecture.Manager;
import atlantis.combat.micro.terran.TerranComsatStation;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.special.SpecialUnitsManager;
import org.junit.Test;
import tests.unit.FakeUnit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ManagerTest extends NonAbstractTestFakingGame {
    @Test
    public void noExceptionIsThrown() {
        boolean status = false;
        FakeUnit comsat = fake(AUnitType.Terran_Comsat_Station, 11.9);
        TerranComsatStation comsatManager = null;

        try {
            TerranComsatStation.class.getDeclaredConstructor(AUnit.class);
            comsatManager = TerranComsatStation.class.getDeclaredConstructor(AUnit.class).newInstance(comsat);

            SpecialUnitsManager.class.getDeclaredConstructor(AUnit.class).newInstance(comsat);

            status = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertTrue(status);
        assertNotNull(comsatManager);
    }

    @Test
    public void canInstantiateManagers() {
        FakeUnit comsat;
        FakeUnit[] our = fakeOurs(
            comsat = fake(AUnitType.Terran_Comsat_Station, 11.9)
        );
        FakeUnit[] enemies = fakeEnemies(
            fake(AUnitType.Zerg_Sunken_Colony, 12)
        );

        createWorld(1, () -> {
                Manager comsatManager = (new TerranComsatStation(comsat)).instantiateManager(TerranComsatStation.class);

                assertNotNull(comsatManager);
            },
            () -> our,
            () -> enemies
        );
    }

}