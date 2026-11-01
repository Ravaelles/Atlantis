package tests.e2e;

import atlantis.map.position.APosition;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import tests.fakes.FakeUnit;
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
 * only), then pure dynamic demand to 70. Four independent assertions -
 * each names a different author (Gateway demand, army demand, expansion
 * demand, gas demand) - so the failure tells WHICH demand dies, not just
 * that something does.</p>
 *
 * <p>Owner time budget: one of at most 3 mega-tests (40 s). Everything
 * the suite needs past 50 supply lives here, in one run.</p>
 */
@Tag("mega-test")
@Timeout(40)
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

    /**
     * One-shot supply milestones: each fires at most once, the first failure
     * aborts the run with the snapshot attached. The sim never runs to the
     * cap just to re-prove a clog it already passed.
     */
    private static final class Milestone {
        final int supply;
        final java.util.function.Predicate<tests.unit.sim.SimDriver> met;
        final String demand;
        boolean fired;

        Milestone(int supply,
                java.util.function.Predicate<tests.unit.sim.SimDriver> met,
                String demand) {
            this.supply = supply;
            this.met = met;
            this.demand = demand;
        }
    }

    @Test
    public void reachesSeventySupplyWithThirdGatewayArmyAndExpansion() throws Exception {
        // Designed length ~11 game-minutes (opening ~11800 frames at sim pace
        // plus 8000 extra): the frame cap only catches hangs - the bank-stuck
        // guard still fails a stalled economy fast.
        // Zerg pressure once the line exists: the mix doctrine answers lings
        // with Zealots (never Dragoons), Zealots keep the Gateways busy, and
        // busy Gateways plus a rich bank want the 3rd. Without pressure the
        // sim idles on gas-trickled Dragoons and the Gateway assertion would
        // pin an empty sky, not a decision.
        boolean[] lingsSent = new boolean[]{false};
        java.util.List<Milestone> milestones = java.util.Arrays.asList(
            new Milestone(55, d -> warpsOf(d, "Assimilator") >= 1,
                "gas online by 55 (Assimilator warped)"),
            new Milestone(60, d -> countOurs(d, AUnitType.Protoss_Nexus) >= 2,
                "expansion by 60 (2nd Nexus standing)"),
            new Milestone(65, d -> warpsOf(d, "Dragoon") >= 2,
                "army flowing by 65 (Dragoons warping)"),
            new Milestone(70, d -> warpsOf(d, "Gateway") >= 3,
                "3rd Gateway by 70 (rich + busy)"));
        OpeningRunner.BankAndSupply bank = numbers();
        OpeningRunner.PostFiftyResult game = OpeningRunner.playToSupplyThenExtraHooked(
            BUILD_ORDER, SimConfig.fast().withStride(60), bank, 50, 8000, 24000,
            driver -> {
                if (!lingsSent[0] && countOurs(driver, AUnitType.Protoss_Dragoon) >= 4) {
                    lingsSent[0] = true;
                    APosition home = ourBase(driver);
                    for (int i = 0; i < 6; i++) {
                        FakeUnit ling = new FakeUnit(
                            AUnitType.Zerg_Zergling, home.tx() + 12 + (i % 3), home.ty() + (i / 3));
                        ling.setEnemy();
                        ling.setCompleted(true);
                        driver.units().enemies().add(ling);
                    }
                }
                for (Milestone m : milestones) {
                    if (m.fired || bank.supplyUsed() < m.supply) continue;
                    m.fired = true;
                    if (!m.met.test(driver)) {
                        throw new AssertionError(
                            "milestone @" + m.supply + " supply failed: " + m.demand
                                + " - snapshot: " + snapshot(driver, bank));
                    }
                }
            });

        int gateways = game.warpsOfType("Gateway");
        int dragoonsAfterFifty = game.dragoonWarpsAfterMark();
        int expands = game.warpsOfType("Nexus");
        int refineries = game.warpsOfType("Assimilator");

        StringBuilder table = new StringBuilder();
        for (String row : game.table) table.append(row).append("\n");

        assertTrue(gateways >= 3,
            "a rich mid-game with busy Gateways wants a 3rd (warps: " + gateways + "):\n" + table);
        assertTrue(dragoonsAfterFifty >= 4,
            "combat production must continue past 47 supply (dragoons after 50: "
                + dragoonsAfterFifty + "):\n" + table);
        assertTrue(expands >= 1,
            "the economy must expand (Nexus warps: " + expands + "):\n" + table);
        assertTrue(refineries >= 1,
            "gas must flow or Dragoons can never exist (Assimilator warps: "
                + refineries + "):\n" + table);
    }

    private static int warpsOf(tests.unit.sim.SimDriver driver, String type) {
        int count = 0;
        for (tests.unit.sim.SimDriver.WarpRecord record : driver.warpLog()) {
            if (record.type.equals(type)) count++;
        }
        return count;
    }

    private static String snapshot(tests.unit.sim.SimDriver driver, OpeningRunner.BankAndSupply bank) {
        int workers = 0, nexuses = 0, busyNexuses = 0, gateways = 0, busyGateways = 0;
        for (FakeUnit unit : driver.units().ours()) {
            if (!unit.isAlive()) continue;
            if (unit.type().isWorker()) workers++;
            if (unit.type().equals(AUnitType.Protoss_Nexus)) {
                nexuses++;
                if (!unit.trainingQueue().isEmpty()) busyNexuses++;
            }
            if (unit.type().equals(AUnitType.Protoss_Gateway)) {
                gateways++;
                if (!unit.trainingQueue().isEmpty()) busyGateways++;
            }
        }
        return "f" + driver.frame() + " bank=" + bank.minerals() + " gas=" + bank.gas()
            + " supply=" + bank.supplyUsed() + " workers=" + workers
            + " nexus=" + nexuses + "(busy " + busyNexuses + ")"
            + " gates=" + gateways + "(busy " + busyGateways + ")"
            + " warps[P/D/Z/Py/G/N/A]=P" + warpsOf(driver, "Probe")
            + "/D" + warpsOf(driver, "Dragoon") + "/Z" + warpsOf(driver, "Zealot")
            + "/Py" + warpsOf(driver, "Pylon") + "/G" + warpsOf(driver, "Gateway")
            + "/N" + warpsOf(driver, "Nexus") + "/A" + warpsOf(driver, "Assimilator");
    }

    private static int countOurs(tests.unit.sim.SimDriver driver, AUnitType type) {
        int count = 0;
        for (FakeUnit unit : driver.units().ours()) {
            if (unit != null && unit.isAlive() && unit.isCompleted() && unit.type().equals(type)) count++;
        }
        return count;
    }

    private static APosition ourBase(tests.unit.sim.SimDriver driver) {
        for (FakeUnit unit : driver.units().ours()) {
            if (unit != null && unit.isAlive() && unit.type().equals(AUnitType.Protoss_Nexus)) {
                return unit.position();
            }
        }
        return APosition.create(95, 119);
    }

    private OpeningRunner.BankAndSupply numbers() {
        return new SimBank(this);
    }
}
