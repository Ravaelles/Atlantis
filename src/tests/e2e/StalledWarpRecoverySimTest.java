package tests.e2e;

import atlantis.game.A;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.commanders.ConstructionStatusChanger;
import atlantis.units.AUnitType;
import atlantis.units.AUnit;
import bwapi.Race;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.unit.AbstractTestWithUnits;
import tests.unit.sim.SimConfig;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.e2e.OpeningRunner.Row;
import static tests.e2e.OpeningRunner.at;

/**
 * Closed-loop proof for the frozen-warp re-arm (Wine 184529 cid 10: Pylon
 * IN_PROGRESS 8+ minutes, builder gone, supply frozen - no cancel, no
 * retry, the supply goal suppressed by the phantom coming count).
 *
 * <p>The sim plays a real V2 opening, freezes a warping Pylon mid-flight
 * (engine-side stall), ticks the real demote every frame like the live
 * chain does, and demands recovery: the demote lands only once the
 * frozen branch proves it (2400f age + static hp - the 90f gone-branch
 * would fire almost immediately), a new warp follows on release, and a
 * Pylon completes after the stall.</p>
 */
public class StalledWarpRecoverySimTest extends AbstractTestWithUnits {

    private static final List<Row> ROWS = Arrays.asList(
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

    private Construction frozen;
    private int hookFrames;
    private int firstHookFrame = -1;
    private int lastHookFrame = -1;
    private int freezeFrame = -1;
    private int demoteFrame = -1;
    private int pylonsDoneAtFreeze;
    private int pylonsDoneAtEnd;

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    private OpeningRunner.BankAndSupply numbers() {
        return new SimBank(this);
    }

    private static int completedPylons(tests.unit.sim.SimDriver driver) {
        int done = 0;
        for (AUnit unit : driver.units().ours()) {
            if (unit != null && unit.isAlive() && unit.isCompleted()
                && unit.type() == AUnitType.Protoss_Pylon) done++;
        }

        return done;
    }

    @Test
    public void frozenWarpRearmsAndSupplyRecovers() throws Exception {
        OpeningRunner.playToSupplyThenExtraHooked(ROWS, SimConfig.fast(), numbers(), 50, 7000, 30000,
            driver -> {
                int now = driver.frame();
                hookFrames++;
                if (firstHookFrame < 0) firstHookFrame = now;
                lastHookFrame = now;
                pylonsDoneAtEnd = completedPylons(driver);

                if (frozen == null) {
                    for (Construction c : ConstructionRequests.constructions) {
                        if (c != null && c.buildingType() == AUnitType.Protoss_Pylon
                            && c.status() == ConstructionOrderStatus.IN_PROGRESS
                            && c.buildingUnit() != null) {
                            frozen = c;
                            freezeFrame = now;
                            pylonsDoneAtFreeze = completedPylons(driver);
                            driver.holdWarp(c, true);

                            break;
                        }
                    }

                    return;
                }

                if (demoteFrame < 0) {
                    ConstructionStatusChanger.demoteStalledInProgress(frozen, frozen.builder());
                    if (frozen.status() == ConstructionOrderStatus.NOT_STARTED) {
                        demoteFrame = now;
                        driver.holdWarp(frozen, false);
                    }
                }
            });

        assertTrue(freezeFrame > 0, "a Pylon warp must present to freeze");
        assertTrue(demoteFrame - freezeFrame >= 2400,
            "only the frozen branch waits 2400f - the 90f gone-branch would fire almost immediately: "
                + freezeFrame + " -> " + demoteFrame);
        assertTrue(demoteFrame - freezeFrame <= 2900,
            "re-arm lands promptly once the freeze is proven: " + freezeFrame + " -> " + demoteFrame);
        assertTrue(pylonsDoneAtEnd > pylonsDoneAtFreeze,
            "a Pylon completes after the stall (recovery, not just re-arm)");
    }
}
