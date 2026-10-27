package tests.unit;

import atlantis.combat.cluster.CorsairCluster;
import atlantis.information.enemy.EnemyUnitsUpdater;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Corsair cluster membership (NEXT #201, owner 2026-10-26): a Corsair in
 * the main cluster trails the army's anchor instead of hunting, so the
 * default is a solo cluster that merges ONLY when threatening air
 * exists. Threatening is any non-Wraith air combat (a Mutalisk never
 * flies alone) or numerous Wraiths - a lone Wraith is a duel the Corsair
 * takes alone, three focus it down and it needs Dragoon cover.
 *
 * <p>Sim-first: the rule pins here before the {@code Alpha.gather} split
 * reads it, so the boundary (2 flies solo, 3 merges) has a name to aim
 * the next tuning at instead of a bare literal.</p>
 */
@Tag("combat")
public class CorsairClusterTest extends WorldStubForTests {

    @Test
    public void emptySkyFliesSolo() {
        assertFalse(
            CorsairCluster.mergeWithArmy(0, 0),
            "no air, no merge - the default is the solo hunting cluster"
        );
    }

    @Test
    public void anyNonWraithAirCombatMerges() {
        assertTrue(
            CorsairCluster.mergeWithArmy(1, 0),
            "one Mutalisk is never alone - under the army's cover now"
        );
    }

    @Test
    public void aLoneWraithIsADuelNotAThreat() {
        assertFalse(
            CorsairCluster.mergeWithArmy(0, 1),
            "a single known Wraith is hunted or dodged alone"
        );
    }

    @Test
    public void twoWraithsStillFlySolo() {
        assertFalse(
            CorsairCluster.mergeWithArmy(0, 2),
            "below numerousness the Corsairs stay out - this is the pinned boundary"
        );
    }

    @Test
    public void numerousWraithsMerge() {
        assertTrue(
            CorsairCluster.mergeWithArmy(0, CorsairCluster.NUMEROUS_WRAITHS),
            "at numerousness the solo flight ends - Dragoon cover"
        );
    }

    @Test
    public void aScoutBehindWraithsStillMerges() {
        assertTrue(
            CorsairCluster.mergeWithArmy(1, 5),
            "either condition alone is enough, together all the more"
        );
    }

    @Test
    public void liveSkyWithNoEnemiesFliesSolo() {
        FakeUnit corsair = fake(AUnitType.Protoss_Corsair, 30, 30);

        world(5, fakeOurs(corsair), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;

            assertFalse(
                CorsairCluster.mergeWithArmy(),
                "empty live sky keeps the solo default"
            );
        });
    }

    @Test
    public void liveMutaliskOverheadMerges() {
        FakeUnit corsair = fake(AUnitType.Protoss_Corsair, 30, 30);
        FakeUnit muta = fake(AUnitType.Zerg_Mutalisk, 60, 60);

        world(5, fakeOurs(corsair), fakeEnemies(muta), () -> {
            if (atlantis.game.A.now() != 2) return;

            assertTrue(
                CorsairCluster.mergeWithArmy(),
                "a visible Mutalisk is threatening air - merge"
            );
        });
    }

    @Test
    public void liveNumerousKnownWraithsMerge() {
        enemyRaceInWorld = bwapi.Race.Terran;

        FakeUnit corsair = fake(AUnitType.Protoss_Corsair, 30, 30);
        FakeUnit wraithA = fake(AUnitType.Terran_Wraith, 60, 60);
        FakeUnit wraithB = fake(AUnitType.Terran_Wraith, 62, 60);
        FakeUnit wraithC = fake(AUnitType.Terran_Wraith, 64, 60);

        world(5, fakeOurs(corsair), fakeEnemies(wraithA, wraithB, wraithC), () -> {
            if (atlantis.game.A.now() != 2) return;

            // Known, not just visible: the fogged record is what the live
            // rule reads, so the pin registers it the same way a sighting
            // would (addFoggedUnit only - no strategy side effects).
            EnemyUnitsUpdater.addFoggedUnit(wraithA);
            EnemyUnitsUpdater.addFoggedUnit(wraithB);
            EnemyUnitsUpdater.addFoggedUnit(wraithC);

            assertTrue(
                CorsairCluster.mergeWithArmy(),
                "three known Wraiths are numerous - merge"
            );
        });
    }

    @Test
    public void liveLoneKnownWraithFliesSolo() {
        enemyRaceInWorld = bwapi.Race.Terran;

        FakeUnit corsair = fake(AUnitType.Protoss_Corsair, 30, 30);
        FakeUnit wraith = fake(AUnitType.Terran_Wraith, 60, 60);

        world(5, fakeOurs(corsair), fakeEnemies(wraith), () -> {
            if (atlantis.game.A.now() != 2) return;

            EnemyUnitsUpdater.addFoggedUnit(wraith);

            assertFalse(
                CorsairCluster.mergeWithArmy(),
                "one known Wraith is a duel - the solo flight stays out"
            );
        });
    }
}
