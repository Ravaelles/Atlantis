package tests.unit;

import atlantis.combat.micro.terran.TerranComsatStation;
import atlantis.units.AUnitType;
import org.junit.Test;
import tests.fakes.FakeUnit;

import static org.junit.Assert.assertEquals;

public class TerranComsatTest extends AbstractTestWithUnits {

    private FakeUnit lurker1 = null;
    private FakeUnit lurker2 = null;

    private void setupEnemyLurkers(FakeUnit[] ours, Runnable runnable) {
        FakeUnit[] enemies = fakeEnemies(
            fake(AUnitType.Zerg_Drone, 12),
            fake(AUnitType.Zerg_Larva, 11),
            fake(AUnitType.Zerg_Lurker_Egg, 11),
            fake(AUnitType.Zerg_Drone, 13),
            lurker1 = fake(AUnitType.Zerg_Lurker, 11).setEffCloaked(false),
            fake(AUnitType.Zerg_Hydralisk, 14),
            lurker2 = fake(AUnitType.Zerg_Lurker, 13).setDetected(false).setEffCloaked(true),
            fake(AUnitType.Zerg_Sunken_Colony, 18)
        );

        usingFakeOursAndFakeEnemies(ours, enemies, () -> {
            runnable.run();
        });
    }

    private void updateComsat(FakeUnit comsat, int energy) {
        comsat.target = null;
        comsat.setEnergy(energy);
        (new TerranComsatStation(comsat)).invokeFrom(this);
    }

    // =========================================================

    @Test
    public void lurkerWhenFewUnitsAround() {
        FakeUnit comsat;
        FakeUnit[] ours = fakeOurs(
            fake(AUnitType.Terran_Command_Center, 1),
            comsat = fake(AUnitType.Terran_Comsat_Station, 3),
            fake(AUnitType.Terran_Marine, 8),
            fake(AUnitType.Terran_Marine, 10),
            fake(AUnitType.Terran_Marine, 11)
        );

        setupEnemyLurkers(ours, () -> {
            updateComsat(comsat, 140);

            assertEquals(null, comsat.target);

            updateComsat(comsat, 200);

            assertEquals(lurker2, comsat.target);

            updateComsat(comsat, 90);

            assertEquals(null, comsat.target);

            updateComsat(comsat, 60);

            assertEquals(null, comsat.target);
        });
    }

    @Test
    public void lurkerWhenManyUnitsAround() {
        FakeUnit comsat;
        FakeUnit[] ours = fakeOurs(
            fake(AUnitType.Terran_Command_Center, 1),
            comsat = fake(AUnitType.Terran_Comsat_Station, 3),
            fake(AUnitType.Terran_Marine, 6),
            fake(AUnitType.Terran_Marine, 7),
            fake(AUnitType.Terran_Marine, 8),
            fake(AUnitType.Terran_Marine, 10),
            fake(AUnitType.Terran_Marine, 11)
        );

        setupEnemyLurkers(ours, () -> {
            updateComsat(comsat, 200);

            assertEquals(lurker2, comsat.target);

            updateComsat(comsat, 140);

            assertEquals(lurker2, comsat.target);

            updateComsat(comsat, 90);

            assertEquals(lurker2, comsat.target);

            updateComsat(comsat, 60);

            assertEquals(lurker2, comsat.target);
        });
    }

}
