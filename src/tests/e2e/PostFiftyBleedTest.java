package tests.e2e;

import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Test;
import tests.unit.AbstractTestWithUnits;
import tests.unit.sim.SimConfig;
import tests.unit.sim.SimDriver;

import java.util.Arrays;
import java.util.List;

import static tests.e2e.OpeningRunner.Row;
import static tests.e2e.OpeningRunner.at;

/**
 * NEXT #217 no-enemy half: with nobody dying the army target is met and
 * the plateau is correct - but losses must pull replacements. Bleed one
 * finished Dragoon every 400 frames after supply 50 and watch the loop:
 * every loss replaced, the bank cycling on spends instead of piling up.
 *
 * <p>Same Zealot-into-Goon rows as {@link SimOpeningToFiftyTest}; this
 * test owns only the loss response AFTER supply 50. The sample table
 * prints on failure.</p>
 */
public class PostFiftyBleedTest extends AbstractTestWithUnits {

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
    public void bleedingArmyPullsReplacements() throws Exception {
        int[] kills = {0};
        int[] maxBank = {0};
        tests.unit.sim.SimDriver[] lastDriver = {null};

        OpeningRunner.PostFiftyResult postFifty = OpeningRunner.playToSupplyThenExtraHooked(
            BUILD_ORDER, SimConfig.fast(), numbers(), 50, 2500, 30000,
            driver -> {
                lastDriver[0] = driver;
                maxBank[0] = Math.max(maxBank[0], currentMinerals);
                if (driver.frame() % 400 == 0 && bleedOneDragoon(driver)) kills[0]++;
            });

        // A kill in the last training window is still in a Gateway when
        // the run ends - it is a replacement, not a stall. Count warps
        // plus Dragoons in training against the bled.
        int inTraining = 0;
        for (tests.fakes.FakeUnit unit : lastDriver[0].units().ours()) {
            if (unit == null || !unit.isAlive() || !unit.isCompleted()) continue;
            if (!unit.type().equals(AUnitType.Protoss_Gateway)) continue;
            if (unit.trainingQueue().contains(AUnitType.Protoss_Dragoon)) inTraining++;
        }
        int dragoons = postFifty.dragoonWarpsAfterMark() + inTraining;
        if (dragoons < kills[0]) {
            StringBuilder table = new StringBuilder();
            for (String row : postFifty.table) table.append(row).append("\n");
            org.junit.jupiter.api.Assertions.fail(
                "post-50 bleed stalls: " + kills[0] + " Dragoons bled but only "
                    + dragoons + " replaced (warps plus " + inTraining
                    + " in training) in 2500 frames after f" + postFifty.markFrame
                    + " (max bank " + maxBank[0] + "):\n" + table);
        }

        // The stall shape banks while producing nothing (849 with one
        // Probe in 1800f). Here every loss pulls a replacement and the
        // bank cycles on spends - a runaway bank means the loop stopped
        // answering.
        if (maxBank[0] >= 1000) {
            org.junit.jupiter.api.Assertions.fail(
                "post-50 bleed hoards: bank " + maxBank[0] + " after "
                    + kills[0] + " bled and " + dragoons + " replaced");
        }
    }

    private static boolean bleedOneDragoon(SimDriver driver) {
        tests.fakes.FakeUnit victim = null;
        for (tests.fakes.FakeUnit unit : driver.units().ours()) {
            if (unit == null || !unit.isAlive() || !unit.isCompleted()) continue;
            if (!unit.type().equals(AUnitType.Protoss_Dragoon)) continue;

            victim = unit;
        }
        if (victim == null) return false;

        victim.setHp(0);

        return true;
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
