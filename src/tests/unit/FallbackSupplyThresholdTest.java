package tests.unit;

import atlantis.production.dynamic.supply.ProduceFallbackPylonWhenSupplyLow;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Fallback Pylons go out EARLY, not at the cap (owner ask: much earlier,
 * like the old ASupplyManager's 2/4/5/8/10/14/19 bands - the interim table
 * capped games the old one rode through).
 */
public class FallbackSupplyThresholdTest extends WorldStubForTests {

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }

    @Test
    public void holdsPylonWhenPlentyFree() {
        initSupply(28, 40);

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            assertFalse(
                new ProduceFallbackPylonWhenSupplyLow().shouldProduce(),
                "12 free at 28/40 is plenty - ordering now would be spam");
        });
    }

    @Test
    public void ordersPylonNineAheadInMidGame() {
        initSupply(31, 40);

        world(20, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            // shouldProduce acts only on its own frame (every 19th).
            if (atlantis.game.A.now() != 19) return;

            assertTrue(
                new ProduceFallbackPylonWhenSupplyLow().shouldProduce(),
                "9 free at 31/40 in the 46-band (trigger 10): one warp of"
                    + " headroom - waiting for the cap wastes a warp");
        });
    }

    @Test
    public void secondPylonOverlapsFirstWhilePoor() {
        // Pipeline doctrine (owner): the next Pylon goes out BEFORE the
        // previous completes. With one unfinished Pylon and low bank the old
        // cap (1 below 150) serialized everything into cap gaps.
        currentMinerals = 50;
        initSupply(38, 40);

        world(20, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 19) return;

            atlantis.production.constructions.Construction first =
                new atlantis.production.constructions.Construction(AUnitType.Protoss_Pylon);
            first.setStatus(atlantis.production.constructions.ConstructionOrderStatus.NOT_STARTED);
            atlantis.production.constructions.ConstructionRequests.constructions.add(first);

            try {
                assertTrue(
                    new ProduceFallbackPylonWhenSupplyLow().shouldProduce(),
                    "one Pylon unfinished + low bank must still allow the"
                        + " second (overlap), or supply sits at cap mid-warp");
            } finally {
                atlantis.production.constructions.ConstructionRequests.constructions.clear();
            }
        });
    }

    @Test
    public void thirdPylonWaitsItsTurn() {
        currentMinerals = 50;
        initSupply(38, 40);

        world(20, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 19) return;

            for (int i = 0; i < 2; i++) {
                atlantis.production.constructions.Construction c =
                    new atlantis.production.constructions.Construction(AUnitType.Protoss_Pylon);
                c.setStatus(atlantis.production.constructions.ConstructionOrderStatus.NOT_STARTED);
                atlantis.production.constructions.ConstructionRequests.constructions.add(c);
            }

            try {
                assertFalse(
                    new ProduceFallbackPylonWhenSupplyLow().shouldProduce(),
                    "two unfinished + poor bank: the pipeline holds at two,"
                        + " no spam");
            } finally {
                atlantis.production.constructions.ConstructionRequests.constructions.clear();
            }
        });
    }
}
