package tests.e2e;

import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Test;
import tests.unit.AbstractTestWithUnits;
import tests.unit.sim.SimConfig;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.e2e.OpeningRunner.Row;
import static tests.e2e.OpeningRunner.at;

/**
 * NEXT #202/#217 family, owner order: the long StarEngine game to 70
 * supply. Live mid-games park at ~47/57 supply with no 3rd Gateway, no
 * combat units past 47 and no second base - this test decides whether
 * the V2 pipeline itself stalls there or the stall needs a live-only
 * cause (engine refusals, legacy interference, siting).
 *
 * <p>Same Zealot-into-Goon opening as {@link SimOpeningToFiftyTest} (rows
 * only), then pure dynamic demand to 70. Three independent assertions -
 * each names a different author (Gateway demand, army demand, expansion
 * demand) - so the failure tells WHICH demand dies, not just that
 * something does.</p>
 */
public class LongGameToSeventyTest extends AbstractTestWithUnits {

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
    public void midGameBuildsThirdGatewayTrainsPast47AndExpands() throws Exception {
        OpeningRunner.PostFiftyResult game = OpeningRunner.playToSupplyThenExtra(
            BUILD_ORDER, SimConfig.fast(), numbers(), 50, 12000, 60000);

        int gateways = 0;
        int dragoonsAfterFifty = 0;
        int expands = 0;
        for (tests.unit.sim.SimDriver.WarpRecord record : game.log) {
            if (record.kind.equals("building") && record.type.equals("Gateway")) gateways++;
            if (record.kind.equals("building") && record.type.equals("Nexus")) expands++;
            if (record.frame > game.markFrame && record.type.equals("Dragoon")) dragoonsAfterFifty++;
        }

        StringBuilder table = new StringBuilder();
        for (String row : game.table) table.append(row).append("\n");

        assertTrue(gateways >= 3,
            "a rich mid-game with busy Gateways wants a 3rd (warps: " + gateways + "):\n" + table);
        assertTrue(dragoonsAfterFifty >= 4,
            "combat production must continue past 47 supply (dragoons after 50: "
                + dragoonsAfterFifty + "):\n" + table);
        assertTrue(expands >= 1,
            "the economy must expand (Nexus warps: " + expands + "):\n" + table);
    }

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
