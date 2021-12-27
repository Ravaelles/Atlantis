package atlantis.tests.acceptance;

import atlantis.production.ABuildingManager;
import atlantis.tests.unit.FakeUnit;
import atlantis.units.AUnitType;
import org.junit.Test;

public class ABuildingManagerTest extends AbstractTestFakingGame {

    @Test
    public void iteratesOverBuildings() {
        createWorld(1, () -> {
            ABuildingManager.update();
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