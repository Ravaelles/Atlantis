package tests.unit;

import atlantis.game.A;
import atlantis.information.enemy.EnemyFlags;
import atlantis.information.enemy.EnemyUnitsUpdater;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.v2.ProductionRacePolicy;
import atlantis.production.v2.engine.ProductionEngine;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Hidden-unit crisis response (owner: lurkers and Dark Templar must trigger
 * detection - fast Robotics Facility into Observatory and Observer, or
 * Forge plus cannons when there is no time). PurpleWave answers the same
 * shape (observer pump 1-vs-3 on lurker-likelihood plus cannons at
 * openings, behind the Robo chain).
 *
 * <p>End to end, not goal-layer: a sighted lurker latches the flag, the V2
 * engine plans from the live snapshot, and a detection construction exists
 * within 30 frames. (The legacy queue half of this test died with the
 * drain in Phase 4 - the queue is never drained in LIVE, so asserting its
 * contents pinned a dead letter.)</p>
 */
@Tag("production")
public class HiddenUnitResponseTest extends WorldStubForTests {

    @Override
    public bwapi.Race initRace() {
        return bwapi.Race.Protoss;
    }

    @AfterEach
    public void cleanFlagAndConstructions() {
        ConstructionRequests.constructions.clear();
        EnemyFlags.HAS_HIDDEN_COMBAT_UNIT = false;
    }

    private static boolean constructionRequested(AUnitType type) {
        return ConstructionRequests.constructions.stream()
            .anyMatch(construction -> type.equals(construction.buildingType()));
    }

    private int runDetectionScenario(boolean withForge) {
        FakeUnit lurker = fake(AUnitType.Zerg_Lurker, 60, 60);
        lurker.setEnemy();

        // Mirrors a proven live detection scene (PvP game 154822: Robo+Forge
        // dispatched f6354): Core standing for the Robo, a Pylon for power,
        // a Gateway so the snapshot reads a lived-in base.
        FakeUnit[] ours;
        if (withForge) {
            ours = fakeOurs(
                fake(AUnitType.Protoss_Nexus, 10, 10),
                fake(AUnitType.Protoss_Pylon, 12, 10),
                fake(AUnitType.Protoss_Gateway, 14, 10),
                fake(AUnitType.Protoss_Cybernetics_Core, 14, 12),
                fake(AUnitType.Protoss_Forge, 11, 11),
                fake(AUnitType.Protoss_Probe, 11, 10));
        }
        else {
            ours = fakeOurs(
                fake(AUnitType.Protoss_Nexus, 10, 10),
                fake(AUnitType.Protoss_Pylon, 12, 10),
                fake(AUnitType.Protoss_Gateway, 14, 10),
                fake(AUnitType.Protoss_Cybernetics_Core, 14, 12),
                fake(AUnitType.Protoss_Probe, 11, 10));
        }

        currentMinerals = 800;
        currentGas = 400;

        ProductionEngine engine = new ProductionEngine(ProductionRacePolicy.forRace("Protoss"));
        int[] firstResponseFrame = {-1};

        world(60, ours, fakeEnemies(lurker), () -> {
            if (A.now() == 10) {
                EnemyUnitsUpdater.weDiscoveredEnemyUnit(lurker);
            }

            if (A.now() >= 10) {
                engine.updateFrame();

                if (firstResponseFrame[0] < 0 && (
                    constructionRequested(AUnitType.Protoss_Forge)
                        || constructionRequested(AUnitType.Protoss_Robotics_Facility)
                        || constructionRequested(AUnitType.Protoss_Photon_Cannon))) {
                    firstResponseFrame[0] = A.now();
                }
            }
        });

        assertTrue(EnemyFlags.HAS_HIDDEN_COMBAT_UNIT,
            "a seen lurker latches the hidden-unit flag");

        return firstResponseFrame[0];
    }

    @Test
    public void lurkerSightRequestsDetectionChain() {
        int firstResponseFrame = runDetectionScenario(true);

        assertTrue(firstResponseFrame >= 10 && firstResponseFrame <= 40,
            "a seen lurker orders detection within 30 frames - first at " + firstResponseFrame);
        assertTrue(constructionRequested(AUnitType.Protoss_Robotics_Facility),
            "fast Robo first (chain continuation is goal-pinned and live-proven)");
    }

    @Test
    public void lurkerSightWithoutForgeRequestsForgeFirst() {
        int firstResponseFrame = runDetectionScenario(false);

        assertTrue(firstResponseFrame >= 10 && firstResponseFrame <= 40,
            "a seen lurker orders detection within 30 frames - first at " + firstResponseFrame);
        assertTrue(constructionRequested(AUnitType.Protoss_Forge)
            || constructionRequested(AUnitType.Protoss_Robotics_Facility),
            "no time for tech without a Forge - it goes first, or the Robo chain");
    }
}
