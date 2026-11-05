package tests.e2e;
import atlantis.production.scheduling.EconomyModel;
import atlantis.production.engine.ProductionEngine;

import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import tests.unit.AbstractTestWithUnits;
import tests.unit.sim.SimConfig;

import java.util.Arrays;
import java.util.List;

import static tests.e2e.OpeningRunner.Row;
import static tests.e2e.OpeningRunner.at;

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
 *
 * <p>Owner time budget: one of at most 3 mega-tests (40 s). The opening
 * gate plus the faithful-walk pin, in two methods of one run.</p>
 */
@Tag("mega-test")
@Timeout(40)
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
     * green (same contract as the arch store). Re-goldened 2026-11-01 for
     * the EconomyModel re-rate (owner numbers: 8 minerals per 300 frames
     * per worker, was 8 per ~180): every warp lands later, order and gates
     * unchanged - slower income, same decisions.
     */
    /**
     * Re-goldened 2026-11-01 for the stride-60 fast sim: the same rows in
     * the same order, every frame on the 60-frame grid (all slightly
     * earlier - less re-plan dither per decision). Same contract as ever:
     * order, gates and no-churn pin the opening, not the clock.
     *
     * Re-goldened 2026-11-06 for the V2 earlier-reserve guard (mint only
     * what covers price plus unpaid earlier rows): refusing marginal
     * mints moves bank across rows, so Gateway@12 warps 900f earlier
     * (bank no longer sunk into a contested mint) and the tail re-times
     * around it. Same rows, same order, supply 50 reached, bank capped,
     * no intruder, no invalid orders - the full test proves health, not
     * just the clock.
     */
    private static final int[] GOLDEN_WARP_FRAMES = {
        1980, 3720, 5460, 5280, 4200, 6360, 6960, 5880, 7800, 8040, 7080,
    };

    /**
     * Real supply (halved, like AGame): the opening plays to 50.
     */
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
            BUILD_ORDER, SimConfig.fast().withStride(60), numbers(), SUPPLY_TARGET, BANK_HORIZON, FRAME_CAP
        );

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

    /**
     * The number feeds the sim pushes into the harness fields every frame.
     */
    private OpeningRunner.BankAndSupply numbers() {
        return new SimBank(this);
    }
}
