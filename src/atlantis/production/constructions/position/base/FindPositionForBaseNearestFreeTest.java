package atlantis.production.constructions.position.base;

import atlantis.information.enemy.EnemyUnits;
import atlantis.information.enemy.EnemyUnitsUpdater;
import atlantis.map.position.APosition;
import atlantis.units.AUnitType;
import atlantis.util.Options;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FindPositionForBaseNearestFreeTest extends WorldStubForTests {
    @Test
    public void testNaturalBaseIsReturnedIfWeHaveOnlyOneBase() {
        createWorld(1,
            () -> {
                EnemyUnitsUpdater.weDiscoveredEnemyUnit(fake(AUnitType.Zerg_Creep_Colony, 95, 10));
                EnemyUnitsUpdater.weDiscoveredEnemyUnit(fake(AUnitType.Zerg_Zergling, 20, 10));
                EnemyUnitsUpdater.weDiscoveredEnemyUnit(fake(AUnitType.Zerg_Hatchery, 90, 10));

                EnemyUnits.discovered().print("Discovered enemies");

                APosition location = FindPositionForBaseNearestFree.find(
                    AUnitType.Protoss_Nexus,
                    fake(AUnitType.Protoss_Probe, 9, 46),
                    null
                );

                assertNotNull(location);
                assertEquals(14, location.tx());
                assertEquals(13, location.ty());
            },
            () -> fakeOurs(
                fake(AUnitType.Protoss_Nexus, 7, 44)
            ),
            () -> fakeEnemies(),
            Options.create().set("supplyUsed", 18)
        );
    }

    @Test
    public void testClosestBaseMostDistantFromEnemyIsReturned() {
        createWorld(1,
            () -> {
//                EnemyUnitsUpdater.weDiscoveredEnemyUnit(fake(AUnitType.Zerg_Zergling, 25, 10));
                EnemyUnitsUpdater.weDiscoveredEnemyUnit(fake(AUnitType.Zerg_Creep_Colony, 95, 10));
                EnemyUnitsUpdater.weDiscoveredEnemyUnit(fake(AUnitType.Zerg_Zergling, 20, 10));
//                EnemyUnits.discovered().print(" Discovered enemies INITIALLY");
                EnemyUnitsUpdater.weDiscoveredEnemyUnit(fake(AUnitType.Zerg_Hatchery, 90, 10));

//                Select.enemy().print("Visible enemies");
                EnemyUnits.discovered().print("Discovered enemies");

//                assertEquals(3, EnemyUnits.discovered().size());

//                Select.our().print("Our units");
//                Select.ourWithUnfinished().print("Our with unfinished");
//                Select.ourBasesWithUnfinished().print("Our bases with unfinished buildings");

                APosition location = FindPositionForBaseNearestFree.find(
                    AUnitType.Protoss_Nexus,
                    fake(AUnitType.Protoss_Probe, 9, 46),
                    null
                );

                assertNotNull(location);
                assertEquals(14, location.tx());
                assertEquals(13, location.ty());
            },
            () -> fakeOurs(
                fake(AUnitType.Protoss_Nexus, 7, 44), // Main
//                fake(AUnitType.Protoss_Nexus, 16, 15) // Natural
                fake(AUnitType.Protoss_Nexus, 7, 80)
            ),
            () -> fakeEnemies(),
            Options.create().set("supplyUsed", 18)
        );
    }
}
