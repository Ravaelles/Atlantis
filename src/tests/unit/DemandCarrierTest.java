package tests.unit;

import atlantis.game.A;
import atlantis.information.enemy.EnemyFlags;
import atlantis.information.enemy.EnemyUnitsUpdater;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.v2.ProductionRacePolicy;
import atlantis.production.v2.engine.ProductionEngine;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * V2 demand carrier (NEXT #228, WO-147): every construction the v2 engine
 * mints carries a {@code Demand} mirroring the synthetic order's type.
 * Readers migrate to the demand one file per commit; until the last one
 * moves, this pin keeps the dual write honest.
 */
@Tag("production")
public class DemandCarrierTest extends WorldStubForTests {

    @Override
    public bwapi.Race initRace() {
        return bwapi.Race.Protoss;
    }

    @AfterEach
    public void clean() {
        ConstructionRequests.constructions.clear();
        EnemyFlags.HAS_HIDDEN_COMBAT_UNIT = false;
    }

    @Test
    public void everyV2ConstructionCarriesAMirroringDemand() {
        FakeUnit lurker = fake(AUnitType.Zerg_Lurker, 60, 60);
        lurker.setEnemy();

        FakeUnit[] ours = fakeOurs(
            fake(AUnitType.Protoss_Nexus, 10, 10),
            fake(AUnitType.Protoss_Pylon, 12, 10),
            fake(AUnitType.Protoss_Gateway, 14, 10),
            fake(AUnitType.Protoss_Cybernetics_Core, 14, 12),
            fake(AUnitType.Protoss_Probe, 11, 10));

        currentMinerals = 800;
        currentGas = 400;

        ProductionEngine engine = new ProductionEngine(ProductionRacePolicy.forRace("Protoss"));

        world(30, ours, fakeEnemies(lurker), () -> {
            if (A.now() == 10) {
                EnemyUnitsUpdater.weDiscoveredEnemyUnit(lurker);
            }

            engine.updateFrame();
        });

        assertFalse(ConstructionRequests.constructions.isEmpty(),
            "the engine must mint constructions in this scene");

        for (Construction construction : ConstructionRequests.constructions) {
            assertNotNull(construction.demand(),
                "every v2 construction carries a demand: " + construction);
            assertTrue(construction.demand().type().equals(construction.buildingType()),
                "demand mirrors the synthetic order's type: " + construction.demand());
        }
    }
}
