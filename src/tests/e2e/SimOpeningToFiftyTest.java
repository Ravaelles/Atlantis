package tests.e2e;

import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Test;
import tests.unit.AbstractTestWithUnits;
import tests.unit.sim.SimConfig;

import java.util.Arrays;
import java.util.List;

import static tests.e2e.OpeningRunner.at;
import static tests.e2e.OpeningRunner.Row;

/**
 * StarEngine opening gate: "Zealot into Goon" played from frame 1 to 50
 * supply in the sim, not in a game. The bot runs unmodified
 * ({@code ProductionEngine.updateFrame} every frame); the sim owns the
 * clock, the bank, mining, builders, warps, training and spawning.
 *
 * <p>Health is defined HERE, by this file - no live game is the reference.
 * A live run is a signal, never the verdict: the verdict is that every
 * row below warps in file order at or past its gate, no non-BO building
 * warps before the first Gateway, and the bank never tops 550 while the
 * opening plays out. A red test names the frame, the warp log says what
 * the bot did instead. The machinery (driver, matching, formatting) lives
 * in {@link OpeningRunner}.</p>
 */
public class SimOpeningToFiftyTest extends AbstractTestWithUnits {

    /**
     * The build order under test, row by row: which type, at which
     * supply gate. Buildings warp in exactly this order at exactly
     * these gates; the (#N) tag is the file position, read the same
     * way the live adjudication reads it.
     */
    private static final List<Row> BUILD_ORDER = Arrays.asList(
        at(AUnitType.Protoss_Pylon, 8), // #1
        at(AUnitType.Protoss_Gateway, 10), // #2
        at(AUnitType.Protoss_Gateway, 12), // #3
        at(AUnitType.Protoss_Zealot, 13), // #4
        at(AUnitType.Protoss_Pylon, 15), // #5
        at(AUnitType.Protoss_Zealot, 17), // #6
        at(AUnitType.Protoss_Zealot, 18), // #7
        at(AUnitType.Protoss_Pylon, 21), // #8
        at(AUnitType.Protoss_Cybernetics_Core, 22), // #9
        at(AUnitType.Protoss_Assimilator, 23), // #10
        at(AUnitType.Protoss_Pylon, 27)); // #11

    /**
     * Golden warp frames, one per row above, from the first green run.
     * The sim is deterministic: same code, same frames. A drift means the
     * bot or the sim changed - update deliberately, never to make red
     * green (same contract as the arch store).
     */
    private static final int[] GOLDEN_WARP_FRAMES = {
        1218, 2274, 3419, 3175, 3187, 3836, 4436, 4444, 6024, 4784, 5175,
    };

    /** Real supply (halved, like AGame): the opening plays to 50. */
    private static final int SUPPLY_TARGET = 50;
    private static final int FRAME_CAP = 16000;
    /**
     * Bank cap horizon, real supply: the opening ends with the last
     * build-order row (gate 27) plus warp slack. The natural Nexus at 49
     * is already mid-game economy (tuning, NEXT #217), not this gate.
     */
    private static final int BANK_HORIZON = 30;
    private static final int MAX_BANK = 550;

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @Test
    public void zealotIntoGoonReachesFiftySupplyInOrder() throws Exception {
        OpeningRunner.OpeningResult opening = OpeningRunner.playToSupply(
            BUILD_ORDER, SimConfig.fast(), numbers(), SUPPLY_TARGET, BANK_HORIZON, FRAME_CAP);

        opening.assertReachedSupply(SUPPLY_TARGET);
        opening.assertWarpsMatch(BUILD_ORDER, GOLDEN_WARP_FRAMES);
        opening.assertNoIntruderBeforeFirstGateway(BUILD_ORDER);
        opening.assertBankBelow(MAX_BANK);
        opening.assertNoInvalidOrders();
    }

    /**
     * FAITHFUL travel: the same opening start, but builders walk the
     * tile arena through the shared mover instead of teleporting along
     * a segment. Short horizon (first Pylon warp): it pins the mover
     * integration, not the economy - the full run above owns that.
     */
    @Test
    public void faithfulBuildersWalkToTheSite() throws Exception {
        OpeningRunner.assertFaithfulBuilderWalksToFirstPylon(numbers());
    }

    /** The number feeds the sim pushes into the harness fields every frame. */
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
