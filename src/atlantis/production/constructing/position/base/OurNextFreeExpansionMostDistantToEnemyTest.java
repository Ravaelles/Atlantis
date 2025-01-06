package atlantis.production.constructing.position.base;

import atlantis.combat.micro.avoid.EnemyUnitsToAvoid;
import atlantis.game.listeners.OnEnemyNewUnitDiscovered;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.enemy.EnemyUnitsUpdater;
import atlantis.map.base.ABaseLocation;
import atlantis.map.position.APosition;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.util.Angle;
import atlantis.util.Options;
import org.junit.Test;
import tests.acceptance.NonAbstractTestFakingGame;
import tests.fakes.FakeUnit;

import static org.junit.Assert.*;

public class OurNextFreeExpansionMostDistantToEnemyTest extends NonAbstractTestFakingGame {
//    @Test
//    public void testClosestBaseMostDistantFromEnemyIsReturned() {
//        createWorld(1,
//            () -> {
////                EnemyUnitsUpdater.weDiscoveredEnemyUnit(fake(AUnitType.Zerg_Zergling, 25, 10));
//                EnemyUnitsUpdater.weDiscoveredEnemyUnit(fake(AUnitType.Zerg_Creep_Colony, 95, 10));
//                EnemyUnitsUpdater.weDiscoveredEnemyUnit(fake(AUnitType.Zerg_Zergling, 20, 10));
////                EnemyUnits.discovered().print(" Discovered enemies INITIALLY");
//                EnemyUnitsUpdater.weDiscoveredEnemyUnit(fake(AUnitType.Zerg_Hatchery, 90, 10));
//
////                Select.enemy().print("Visible enemies");
//                EnemyUnits.discovered().print("Discovered enemies");
//
////                assertEquals(3, EnemyUnits.discovered().size());
//
////                Select.our().print("Our units");
////                Select.ourWithUnfinished().print("Our with unfinished");
////                Select.ourBasesWithUnfinished().print("Our bases with unfinished buildings");
//
//                APosition location = OurNextFreeExpansionMostDistantToEnemy.find();
//
//                assertNotNull(location);
//                assertEquals(14, location.tx());
//                assertEquals(13, location.ty());
//            },
//            () -> fakeOurs(
//                fake(AUnitType.Protoss_Nexus, 7, 44)
//            ),
//            () -> fakeEnemies(),
//            Options.create().set("supplyUsed", 18)
//        );
//    }
}
