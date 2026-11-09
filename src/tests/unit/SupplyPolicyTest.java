package tests.unit;

import atlantis.production.dynamic.supply.SupplyPolicy;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Single-door Pylon cap as one pure rule (SRP slice of the 2026-10-19
 * SOLID review, S3): the bands both legacy doors read, pinned without
 * a game. No behavior change - the legacy method delegates below with
 * identical semantics.
 */
@Tag("production")
public class SupplyPolicyTest {

    @Test
    public void earlyGameOneAtATime() {
        assertFalse(SupplyPolicy.tooManyPylonsInFlight(0, 20, false));
        assertTrue(SupplyPolicy.tooManyPylonsInFlight(1, 20, false),
            "one unfinished Pylon below 40 is the cap");
        assertTrue(SupplyPolicy.tooManyPylonsInFlight(3, 39, false));
    }

    @Test
    public void richBankLiftsTheEarlyCapAsIs() {
        assertFalse(SupplyPolicy.tooManyPylonsInFlight(1, 20, true),
            "rich bank still allows the pair below 40");
        // As-is corner (owner call to tighten): today a rich bank lifts
        // the cap entirely below 40, not just to the pair. Pinned as-is,
        // not as it should be.
        assertFalse(SupplyPolicy.tooManyPylonsInFlight(5, 20, true));
    }

    @Test
    public void lateGamePipelinePair() {
        assertFalse(SupplyPolicy.tooManyPylonsInFlight(0, 40, false));
        assertFalse(SupplyPolicy.tooManyPylonsInFlight(1, 40, false));
        assertTrue(SupplyPolicy.tooManyPylonsInFlight(2, 40, false),
            "at 40+ the pair flies, the third waits");
        assertTrue(SupplyPolicy.tooManyPylonsInFlight(2, 100, true));
    }
}
