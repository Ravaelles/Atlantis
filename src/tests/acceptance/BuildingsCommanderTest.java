package tests.acceptance;

import atlantis.production.BuildingsCommander;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

public class BuildingsCommanderTest extends AbstractTestWithWorld {
    @Test
    public void iteratesOverBuildings() {
        createWorld(1, () -> {
            (new BuildingsCommander()).invokeCommander();
        });
    }

    // =========================================================

    protected FakeUnit[] generateOur() {
        return fakeOurs(
            fake(AUnitType.Terran_SCV),
            fake(AUnitType.Terran_Command_Center),
            fake(AUnitType.Terran_Comsat_Station),
            fake(AUnitType.Terran_Science_Vessel),
            fake(AUnitType.Protoss_Shield_Battery)
        );
    }

    protected FakeUnit[] generateEnemies() {
        return fakeEnemies(
            fake(AUnitType.Zerg_Creep_Colony),
            fake(AUnitType.Zerg_Sunken_Colony)
        );
    }
}
