package atlantis.production.dynamic.protoss;

import atlantis.information.enemy.EnemyUnitsUpdater;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.units.AUnitType;
import atlantis.util.Options;
import org.junit.Test;
import tests.acceptance.NonAbstractTestFakingGame;

import static org.junit.Assert.assertEquals;

public class ProtossSecureBasesCommanderTest extends NonAbstractTestFakingGame {
    @Test
    public void testNaturalBaseGetsCannons() {
        createWorld(1,
            () -> {
                EnemyUnitsUpdater.weDiscoveredEnemyUnit(fake(AUnitType.Zerg_Creep_Colony, 95, 10));
                EnemyUnitsUpdater.weDiscoveredEnemyUnit(fake(AUnitType.Zerg_Zergling, 20, 10));
                EnemyUnitsUpdater.weDiscoveredEnemyUnit(fake(AUnitType.Zerg_Hatchery, 90, 10));

//                EnemyUnits.discovered().print("Discovered enemies");

                assertEquals(0, ConstructionRequests.all().size());

                (new ProtossSecureBasesCommander()).handle();

                assertEquals(1, ConstructionRequests.all().size());

                System.out.println(ConstructionRequests.all().get(0));

//                assertNotNull(location);
//                assertEquals(14, location.tx());
//                assertEquals(13, location.ty());
            },
            () -> fakeOurs(
                fake(AUnitType.Protoss_Nexus, 7, 44), // Main
                fake(AUnitType.Protoss_Nexus, 16, 15), // Natural
                fake(AUnitType.Protoss_Probe, 7, 47)
            ),
            () -> fakeEnemies(),
            Options.create().set("supplyUsed", 18)
        );
    }
}
