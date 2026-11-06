package tests.e2e;

import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Test;
import tests.unit.AbstractTestWithUnits;
import tests.unit.sim.SimConfig;

import java.util.Arrays;
import java.util.List;

import static tests.e2e.OpeningRunner.Row;
import static tests.e2e.OpeningRunner.at;

/**
 * NEXT #217: the post-expansion economy must keep producing. The doctrine
 * (decided 2026-11-01, #234b): a saturated single base SAVES for expansion
 * instead of trickling. Workers cap at saturation (25 on one base), the
 * bank climbs toward the Nexus price instead of feeding a trickle, the
 * Nexus warps, and probes resume on the new base. The old shape this
 * replaces (1 probe per 1800 frames with the bank hoarding at 849 and no
 * expansion ever warping) fails the assertions below: no Nexus, or probes
 * that never resume after it.
 *
 * <p>Same Zealot-into-Goon rows as {@link SimOpeningToFiftyTest} (the
 * contract that owns the golden frames - this test owns only what happens
 * AFTER supply 50). The sample table prints on failure and names the clog
 * without guessing: supply, bank, workers, Nexus busy count, warp deltas.</p>
 */
public class PostFiftyProductionTest extends AbstractTestWithUnits {

    /**
     * Copy of the opening under test - the rows (and only the rows);
     * golden frames and intruder/bank rules live in
     * {@link SimOpeningToFiftyTest}.
     */
    private static final List<Row> BUILD_ORDER = Arrays.asList(
        at(AUnitType.Protoss_Pylon, 8),
        at(AUnitType.Protoss_Gateway, 10),
        at(AUnitType.Protoss_Gateway, 12),
        at(AUnitType.Protoss_Zealot, 13),
        at(AUnitType.Protoss_Pylon, 15),
        at(AUnitType.Protoss_Zealot, 17),
        at(AUnitType.Protoss_Zealot, 18),
        at(AUnitType.Protoss_Pylon, 21),
        at(AUnitType.Protoss_Cybernetics_Core, 22),
        at(AUnitType.Protoss_Assimilator, 23),
        at(AUnitType.Protoss_Pylon, 27));

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @Test
    public void economyKeepsProducingAfterFiftySupply() throws Exception {
        // Window 4000 (was 2500): pylon-center power (engine measures its
        // psi range from the Pylon's sprite, not its top-left tile) makes
        // the opening wait for real power (Gateway@12 5460->6300) and lets
        // two post-50 Gateways ([9,48]@12300, [15,48]@13740, both powered
        // by Pylon [17,51]) warp before the Nexus (@15000, mark+3240) -
        // the same gates-before-Nexus shape the live game shows (3 gates
        // before Nexus@6552 on TauCross). The doctrine stands (Nexus warps,
        // probes resume, no hoard), it just needs the engine-truth clock.
        OpeningRunner.PostFiftyResult postFifty = OpeningRunner.playToSupplyThenExtra(
            BUILD_ORDER, SimConfig.fast().withStride(60), numbers(), 50, 4000, 30000);

        int nexusFrame = postFifty.firstWarpFrameAfterMark("Nexus");
        int probesAfterNexus = nexusFrame < 0 ? -1
            : postFifty.warpsOfTypeAfterFrame("Probe", nexusFrame);
        if (nexusFrame < 0 || probesAfterNexus < 1) {
            StringBuilder table = new StringBuilder();
            for (String row : postFifty.table) table.append(row).append("\n");
            org.junit.jupiter.api.Assertions.assertTrue(nexusFrame >= 0,
                "saturated single base saves for expansion - a Nexus warps "
                    + "instead of the bank hoarding:\n" + table);
            org.junit.jupiter.api.Assertions.assertTrue(probesAfterNexus >= 1,
                "probes resume once the natural warps (Nexus f" + nexusFrame + "):\n" + table);
        }
    }

    /**
     * The number feeds the sim pushes into the harness fields every frame.
     */
    private OpeningRunner.BankAndSupply numbers() {
        return new SimBank(this);
    }
}
