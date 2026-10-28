package tests.unit;

import atlantis.game.listeners.OnEnemyUnitShow;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.enemy.EnemyUnitsUpdater;
import atlantis.units.AUnitType;
import atlantis.units.fogged.AbstractFoggedUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Fog memory must heal on re-sight (NEXT #2 follow-up: live strength
 * halves when contact is lost, e.g. 182 to 364, while the remembered
 * part stands still - units visible to the engine never entered the fog
 * map, so leaving sight halves the sum with zero deaths).
 *
 * <p>A re-sight arrives as show, never discover (the id was seen before),
 * and show never wrapped - so any unit the map lost stays lost no matter
 * how often the army looks at it. Show of an unknown unit must wrap it;
 * show of a known unit stays a no-op.</p>
 */
@Tag("infra")
public class FogMemoryResightTest extends WorldStubForTests {

    @Override
    public bwapi.Race initRace() {
        return bwapi.Race.Protoss;
    }

    @Override
    public bwapi.Race initEnemyRace() {
        return bwapi.Race.Zerg;
    }

    @BeforeEach
    public void clearMemory() {
        EnemyUnits.clearCache();
        AbstractFoggedUnit.clearCache();
        atlantis.util.log.EventLog.clearForTests();
    }

    @AfterEach
    public void clearMemoryAgain() {
        EnemyUnits.clearCache();
        AbstractFoggedUnit.clearCache();
        atlantis.util.log.EventLog.clearForTests();
    }

    @Test
    public void showWrapsAnUnknownUnit() {
        FakeUnit sunken = fake(AUnitType.Zerg_Sunken_Colony, 60, 60);
        sunken.setEnemy();

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 10, 10),
            fake(AUnitType.Protoss_Zealot, 12, 10)
        ), fakeEnemies(sunken), () -> {
            if (atlantis.game.A.now() != 1) return;

            assertNull(EnemyUnits.getFoggedUnit(sunken),
                "never discovered, never wrapped");

            OnEnemyUnitShow.update(sunken);

            assertNotNull(EnemyUnits.getFoggedUnit(sunken),
                "re-sight heals the map: show wraps what it sees");
            assertTrue(atlantis.util.log.EventLog.toJsonLines().contains("\"key\":\"SHOW_HEAL\""),
                "the heal rows, so the mechanism proves itself live: "
                    + atlantis.util.log.EventLog.toJsonLines());
        });
    }

    @Test
    public void showIgnoresOurUnits() {
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 60, 60);

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 10, 10),
            probe
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 1) return;

            OnEnemyUnitShow.update(probe);

            assertNull(EnemyUnits.getFoggedUnit(probe),
                "show is for every unit - only enemies enter the fog map");
            assertTrue(!atlantis.util.log.EventLog.toJsonLines().contains("\"key\":\"SHOW_HEAL\""),
                "our units never heal the enemy map: "
                    + atlantis.util.log.EventLog.toJsonLines());
        });
    }

    @Test
    public void showKeepsAKnownUnitUntouched() {
        FakeUnit drone = fake(AUnitType.Zerg_Drone, 60, 60);
        drone.setEnemy();

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 10, 10),
            fake(AUnitType.Protoss_Zealot, 12, 10)
        ), fakeEnemies(drone), () -> {
            if (atlantis.game.A.now() != 1) return;

            EnemyUnitsUpdater.weDiscoveredEnemyUnit(drone);
            AbstractFoggedUnit before = EnemyUnits.getFoggedUnit(drone);

            OnEnemyUnitShow.update(drone);

            assertNotNull(EnemyUnits.getFoggedUnit(drone),
                "known units stay remembered");
            assertTrue(!atlantis.util.log.EventLog.toJsonLines().contains("\"key\":\"SHOW_HEAL\""),
                "known re-sights stay silent - heals row only lost units: "
                    + atlantis.util.log.EventLog.toJsonLines());
        });
    }
}
