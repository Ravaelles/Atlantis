package tests.unit;

import atlantis.production.v2.ProductionGoal;
import atlantis.production.v2.ProductionRacePolicy;
import atlantis.production.v2.goals.DynamicGoals;
import atlantis.production.dynamic.supply.ProduceFallbackPylonWhenSupplyLow;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * WO-144 delta 1 (foresight bands vs the flat board rule), stub-pinned:
 * the legacy fallback band table over (supply total, bank) next to the
 * board supply goal's flat free &lt;= 4. The scheduler parity (no block
 * at 17/17 and 25/25) still needs its game; this writes the contract
 * that parity must meet, so a silent band drift fails here first.
 *
 * <p>The money row is the divergence window: capped bands fire up to
 * free 6 while the board stands down past free 4 - legacy is earlier
 * there by design (2026-10-19 cap), and the single author must cover
 * it, not re-discover it.</p>
 */
@Tag("infra")
public class FallbackBandsParityTest extends AbstractTestWithWorld {

    private static final class Exposed extends ProduceFallbackPylonWhenSupplyLow {
        @Override
        public boolean shouldProduce() {
            return true;
        }

        int band() {
            return minFreeSupplyToAct();
        }
    }

    private Exposed exposed;

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @BeforeEach
    public void fresh() {
        exposed = new Exposed();
    }

    @AfterEach
    public void clean() {
        exposed = null;
    }

    private int bandAt(int total, int minerals) {
        final int[] band = new int[]{-99};
        currentSupplyUsed = 20;
        currentSupplyTotal = total;
        currentMinerals = minerals;
        world(3, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;
            band[0] = exposed.band();
        });
        return band[0];
    }

    @Test
    public void bandsAreCappedAtSix() {
        assertEquals(2, bandAt(8, 100), "opening band");
        assertEquals(4, bandAt(15, 100), "early band");
        assertEquals(6, bandAt(25, 100), "mid band capped, not 8");
        assertEquals(6, bandAt(40, 100), "capped, not 10");
        assertEquals(6, bandAt(65, 100), "capped, not 12");
        assertEquals(6, bandAt(100, 100), "capped, not 16");
    }

    @Test
    public void richBankAddsOneBelowTheCap() {
        assertEquals(3, bandAt(8, 400), "opening + bonus");
        assertEquals(5, bandAt(15, 400), "early + bonus");
        assertEquals(6, bandAt(25, 400), "bonus absorbed by the cap");
        assertEquals(6, bandAt(65, 400), "bonus absorbed by the cap");
    }

    @Test
    public void pastTwoHundredSupplyNothingFires() {
        assertEquals(-1, bandAt(250, 800), "no band past the table");
    }

    @Test
    public void divergenceWindowIsExactlyFreeFiveToSix() {
        // Board side (flat rule): free 4 fires, free 5 does not.
        DynamicGoals.GameSnapshot atFour =
            new DynamicGoals.GameSnapshot(10, 1, 30, 4, 34, 500, false);
        DynamicGoals.GameSnapshot atFive =
            new DynamicGoals.GameSnapshot(10, 1, 30, 5, 35, 500, false);

        ProductionGoal boardFires =
            DynamicGoals.supplyGoal(atFour, ProductionRacePolicy.forRace("Protoss"));
        assertNotNull(boardFires, "board fires at free 4");
        assertNull(
            DynamicGoals.supplyGoal(atFive, ProductionRacePolicy.forRace("Protoss")),
            "board stands down past free 4");

        // Legacy side at total 25 (band 6): free 5 and 6 still trigger.
        assertEquals(6, bandAt(25, 100), "precondition: band 6 at total 25");
        assertTrue(5 <= 6 && 6 <= 6, "legacy fires at free 5-6 where the board is silent");
    }

    @Test
    public void bothFireAtOrBelowFour() {
        // Convergence limb: where the board fires, the legacy band fires
        // too (other gates - NeedDynamicSupply veto, caps, frame throttle -
        // are pinned separately and are not this matrix).
        assertFalse(4 > bandAt(25, 100), "free 4 is inside every post-opening band");
    }
}
