package tests.acceptance.protoss;

import atlantis.information.enemy.EnemyUnits;
import atlantis.information.enemy.EnemyUnitsUpdater;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.dynamic.protoss.ProtossSecureBasesCommander;
import atlantis.units.AUnitType;
import atlantis.util.Options;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProtossSecureBasesCommanderTest extends WorldStubForTests {
    @Test
    public void testNaturalBaseGetsCannons() {
        createWorld(1,
            () -> {
                assertEquals(0, EnemyUnits.discovered().size());

                EnemyUnitsUpdater.weDiscoveredEnemyUnit(fake(AUnitType.Zerg_Creep_Colony, 95, 10));
                assertEquals(1, EnemyUnits.discovered().size());

                EnemyUnitsUpdater.weDiscoveredEnemyUnit(fake(AUnitType.Zerg_Zergling, 20, 10));
                assertEquals(2, EnemyUnits.discovered().size());

                EnemyUnitsUpdater.weDiscoveredEnemyUnit(fake(AUnitType.Zerg_Hatchery, 90, 10));
                assertEquals(3, EnemyUnits.discovered().size());

//                EnemyUnits.discovered().print("Discovered enemies");

                assertEquals(0, ConstructionRequests.all().size());

                (new ProtossSecureBasesCommander()).forceHandle();

                assertEquals(1, ConstructionRequests.all().size());

//                System.out.println(ConstructionRequests.all().get(0));

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
