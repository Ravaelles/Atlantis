package tests.unit.decision;

import atlantis.production.demand.ExpansionTiming;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The expansion timing story: one class answers "build a Nexus now?",
 * in the Protoss doctrine's own words.
 *
 * <p>Each test is a battlefield situation in plain language; the
 * technical snapshot hides in {@link ExpansionStory}. The rows pin the
 * real {@code ExpansionTiming.decide} - the same method the legacy
 * commander, the board goal and the V2 demand all read - so a passing
 * story proves the three authors agree, without running a game.</p>
 */
@Tag("production")
public class ExpansionStoryTest {

    @Test
    public void bankedWindfallExpandsWhateverThePressure() {
        ExpansionStory banked = ExpansionStory.protoss().minerals(800).oneBase().quiet();

        assertEquals(ExpansionTiming.Verdict.WANT_BANK, banked.decide(),
            banked.says("a 777 bank expands, no thicket consulted"));
    }

    @Test
    public void poorBaseHolds() {
        ExpansionStory poor = ExpansionStory.protoss().minerals(299).oneBase().quiet();

        assertEquals(ExpansionTiming.Verdict.HOLD, poor.decide(),
            poor.says("under 300 minerals there is nothing to discuss"));
    }

    @Test
    public void mutasVetoARichBaseUnlessCovered() {
        ExpansionStory exposed = ExpansionStory.protoss().minerals(900).oneBase()
            .planned(4).mutas(3).cannons(0);

        assertEquals(ExpansionTiming.Verdict.HOLD, exposed.decide(),
            exposed.says("mutas punish a banking base with thin cover"));

        ExpansionStory covered = ExpansionStory.protoss().minerals(400).oneBase()
            .mutas(3).cannons(9);

        assertEquals(ExpansionTiming.Verdict.THICKET, covered.decide(),
            covered.says("poor and covered: back to the thicket, nothing to defend with"));
    }

    @Test
    public void rushDefenseHoldsWhenPoorAndWeak() {
        ExpansionStory rushed = ExpansionStory.protoss().minerals(300).oneBase()
            .army(100).earlyGame();

        assertEquals(ExpansionTiming.Verdict.HOLD, rushed.decide(),
            rushed.says("poor and weak against a rush: defend first"));
    }

    @Test
    public void unfinishedBasePlusThinBankHolds() {
        ExpansionStory saturated = ExpansionStory.protoss().minerals(400)
            .oneBaseUnfinished().quiet();

        assertEquals(ExpansionTiming.Verdict.HOLD, saturated.decide(),
            saturated.says("a base already coming plus a thin bank: wait"));
    }

    @Test
    public void sevenBasesCap() {
        ExpansionStory capped = ExpansionStory.protoss().minerals(900)
            .sevenBases().quiet();

        assertEquals(ExpansionTiming.Verdict.HOLD, capped.decide(),
            capped.says("seven bases cap the game"));
    }

    @Test
    public void ordinarySecondBaseGoesToTheThicket() {
        ExpansionStory normal = ExpansionStory.protoss().minerals(400).oneBase().quiet();

        assertEquals(ExpansionTiming.Verdict.THICKET, normal.decide(),
            normal.says("an ordinary frame belongs to the natural thicket, not to this matrix"));
    }
}
