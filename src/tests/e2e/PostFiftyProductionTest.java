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
 * NEXT #217: the post-expansion economy must keep producing. After the
 * natural Nexus warps (~f9393, supply 49) the sim built one Probe in
 * ~1800 frames while the bank climbed to 849; live 104542 keeps producing
 * through the same window (max bank below 50 is 178).
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
        OpeningRunner.PostFiftyResult postFifty = OpeningRunner.playToSupplyThenExtra(
            BUILD_ORDER, SimConfig.fast(), numbers(), 50, 2500, 30000);

        int probes = postFifty.probeWarpsAfterMark();
        if (probes < 6) {
            StringBuilder table = new StringBuilder();
            for (String row : postFifty.table) table.append(row).append("\n");
            org.junit.jupiter.api.Assertions.fail(
                "post-50 economy stalls: " + probes + " Probe warps in 2500 frames"
                    + " after f" + postFifty.markFrame + ":\n" + table);
        }
    }

    /**
     * The number feeds the sim pushes into the harness fields every frame.
     */
    private OpeningRunner.BankAndSupply numbers() {
        return new OpeningRunner.BankAndSupply() {
            @Override
            public void setMinerals(int minerals) {
                currentMinerals = minerals;
            }

            @Override
            public void setGas(int gas) {
                currentGas = gas;
            }

            @Override
            public void setSupply(int used, int total) {
                currentSupplyUsed = used;
                currentSupplyTotal = total;
            }

            @Override
            public int minerals() {
                return currentMinerals;
            }

            @Override
            public int supplyUsed() {
                return currentSupplyUsed;
            }
        };
    }
}
