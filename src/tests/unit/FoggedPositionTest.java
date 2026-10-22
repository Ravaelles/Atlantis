package tests.unit;

import atlantis.information.enemy.EnemyUnits;
import atlantis.information.enemy.EnemyUnitsUpdater;
import atlantis.units.AUnitType;
import atlantis.units.fogged.AbstractFoggedUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Fogged positions are own snapshots, never engine echoes (owner
 * 2026-10-26): losing sight freezes the last-seen tile instead of
 * copying invisible coordinates over it, and a tile seen empty
 * clears the address instead of re-snapshotting garbage. The unit
 * itself stays known - only the address goes.
 */
@Tag("infra")
public class FoggedPositionTest extends WorldStubForTests {

    @AfterEach
    public void cleanArchive() {
        EnemyUnits.clearCache();
        AbstractFoggedUnit.clearCache();
    }

    @Test
    public void snapshotFreezesWhenSightIsLost() {
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 29, 29);
        zealot.setEnemy();

        world(2, fakeOurs(fake(AUnitType.Protoss_Dragoon, 28, 29)), fakeEnemies(zealot), () -> {
            if (atlantis.game.A.now() != 1) return;

            EnemyUnitsUpdater.addFoggedUnit(zealot);
            AbstractFoggedUnit fogged = EnemyUnits.getFoggedUnit(zealot);
            assertNotNull(fogged, "known enemies are wrapped");
            assertEquals(zealot.position(), fogged.position(), "visible: snapshot the tile");

            zealot.isVisibleUnitOnMap = false;
            zealot.position = atlantis.map.position.APosition.createFromPixels(50 * 32, 20 * 32);
            fogged.updatePosition(zealot);

            assertEquals(atlantis.map.position.APosition.create(29, 29), fogged.position(),
                "invisible: the snapshot freezes, the ghost is not re-sighted");
        });

        EnemyUnits.clearCache();
        AbstractFoggedUnit.clearCache();
    }

    @Test
    public void emptyTileClearsTheAddressButKeepsTheUnit() {
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 29, 29);
        zealot.setEnemy();

        world(2, fakeOurs(fake(AUnitType.Protoss_Dragoon, 28, 29)), fakeEnemies(zealot), () -> {
            if (atlantis.game.A.now() != 1) return;

            EnemyUnitsUpdater.addFoggedUnit(zealot);
            AbstractFoggedUnit fogged = EnemyUnits.getFoggedUnit(zealot);
            assertTrue(fogged.hasPosition(), "seen once: addressed");

            fogged.foggedUnitNoLongerWhereItWasBefore();

            assertFalse(fogged.hasPosition(), "tile seen empty: address cleared, not re-sighted");
            assertNotNull(EnemyUnits.getFoggedUnit(zealot), "the unit stays known, only the address goes");
        });

        EnemyUnits.clearCache();
        AbstractFoggedUnit.clearCache();
    }
}
