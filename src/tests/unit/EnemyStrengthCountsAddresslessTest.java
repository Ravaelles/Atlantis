package tests.unit;

import atlantis.information.enemy.EnemyUnits;
import atlantis.information.enemy.EnemyUnitsUpdater;
import atlantis.information.generic.Army;
import atlantis.units.AUnitType;
import atlantis.units.fogged.AbstractFoggedUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Strength counts heads, not positions (owner live finding: 999 with a
 * known-but-unseen enemy). A remembered enemy whose address was cleared
 * (tile seen empty) still fields an army - only a truly unknown enemy
 * reads 999.
 */
@Tag("infra")
public class EnemyStrengthCountsAddresslessTest extends WorldStubForTests {

    @AfterEach
    public void cleanArchive() {
        EnemyUnits.clearCache();
        AbstractFoggedUnit.clearCache();
    }

    @Test
    public void addresslessKnownEnemiesStillCount() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 30);

        world(20, fakeOurs(nexus, dragoon), new FakeUnit[]{}, () -> {
            if (atlantis.game.A.now() != 5) return;

            FakeUnit foe = fake(AUnitType.Protoss_Zealot, 90, 90);
            foe.setEnemy();
            EnemyUnitsUpdater.addFoggedUnit(foe);

            int addressed = Army.strength();
            assertTrue(addressed < 999 && addressed > 0,
                "one known zealot vs one dragoon is a real number, not 999: " + addressed);

            AbstractFoggedUnit fogged = EnemyUnits.getFoggedUnit(foe);
            fogged.foggedUnitNoLongerWhereItWasBefore();

            assertEquals(addressed, Army.strength(),
                "losing the address must not lose the army");
        });

        EnemyUnits.clearCache();
        AbstractFoggedUnit.clearCache();
    }

    @Test
    public void trulyUnknownEnemyReads999() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 30);

        world(20, fakeOurs(nexus, dragoon), new FakeUnit[]{}, () -> {
            if (atlantis.game.A.now() != 5) return;

            assertEquals(999, Army.strength(), "no intel at all reads 999");
        });

        EnemyUnits.clearCache();
        AbstractFoggedUnit.clearCache();
    }
}
