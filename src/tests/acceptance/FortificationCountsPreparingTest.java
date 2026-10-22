package tests.acceptance;

import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.dynamic.protoss.reinforce.ShouldSecureProtossBase;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Owner 2026-10-16: fortification demand must see picked-but-builderless
 * sites. A PREPARING cannon (site reserved, waiting for a builder) is
 * planned coverage - but the securing check only counted standing and
 * NOT_STARTED units, so every 45-frame pass saw "not enough cannons" and
 * filed another row while the site waited (the PhotonC ADDED x15 shape).
 */
public class FortificationCountsPreparingTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @AfterEach
    public void clean() {
        ConstructionRequests.constructions.clear();
    }

    @Test
    public void waitingPreparingSiteSatisfiesDemand() {
        FakeUnit natural = fake(AUnitType.Protoss_Nexus, 16, 15);

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 7, 44),
            natural,
            fake(AUnitType.Protoss_Forge, 8, 44),
            fake(AUnitType.Protoss_Probe, 7, 47)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            ConstructionRequests.constructions.clear();

            currentMinerals = 300;
            currentSupplyUsed = 50;
            currentSupplyTotal = 60;

            assertTrue(
                new ShouldSecureProtossBase(natural).needsSecuring(),
                "control: no cannon anywhere near the natural, demand stands");

            Construction waiting = new Construction(AUnitType.Protoss_Photon_Cannon);
            waiting.setPositionToBuild(APosition.create(18, 16));
            waiting.setStatus(ConstructionOrderStatus.PREPARING);
            ConstructionRequests.addConstruction(waiting);

            assertFalse(
                new ShouldSecureProtossBase(natural).needsSecuring(),
                "one picked site waiting for a builder covers the single-cannon demand - "
                    + "re-firing files duplicate rows faster than builders arrive");
        });
    }

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }
}
