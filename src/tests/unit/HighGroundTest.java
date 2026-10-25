package tests.unit;

import atlantis.combat.strategy.HighGround;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Escape-adjacent formation slice (NEXT #181): holding the high side of a
 * choke outscores the low side; wider funnels cost more to cover.
 */
@Tag("combat")
public class HighGroundTest {

    @Test
    public void highSideOutscoresLowSide() {
        double high = HighGround.defensiveValue(2, 0, 6);
        double low = HighGround.defensiveValue(0, 2, 6);

        assertTrue(high > low, "uphill sees down and shoots down: " + high + " vs " + low);
    }

    @Test
    public void widerFunnelCostsMore() {
        double narrow = HighGround.defensiveValue(1, 0, 4);
        double wide = HighGround.defensiveValue(1, 0, 8);

        assertTrue(narrow > wide, "more front to cover: " + narrow + " vs " + wide);
    }

    @Test
    public void steppedWideBeatsNarrowFlat() {
        // The entry's headline case (NEXT #171): one step of height pays
        // for four extra tiles of funnel (10 - 8 = 2 vs 0 - 2 = -2).
        double steppedWide = HighGround.defensiveValue(1, 0, 8);
        double narrowFlat = HighGround.defensiveValue(0, 0, 2);

        assertTrue(steppedWide > narrowFlat,
            "a step is worth the wider mouth: " + steppedWide + " vs " + narrowFlat);
    }

    @Test
    public void symmetricGroundScoresFunnelOnly() {
        assertEquals(-6.0, HighGround.defensiveValue(1, 1, 6), 0.001);
    }

    @Test
    public void nullTileGivesUnknown() {
        assertEquals(-1, HighGround.groundHeightOf(null));
    }

    @Test
    public void unknownGroundKeepsOurSide() {
        assertTrue(!HighGround.preferFoeSide(-1, 2), "blind: hold ours, never guess");
        assertTrue(!HighGround.preferFoeSide(2, -1), "blind: hold ours, never guess");
        assertTrue(!HighGround.preferFoeSide(-1, -1), "blind: hold ours, never guess");
    }

    @Test
    public void higherFoeSideWins() {
        assertTrue(HighGround.preferFoeSide(0, 2), "the high lip shoots down");
    }

    @Test
    public void tiesAndHigherOursKeepOurSide() {
        assertTrue(!HighGround.preferFoeSide(2, 2), "flat: the 10-tile rule stands");
        assertTrue(!HighGround.preferFoeSide(2, 0), "already high: stay");
    }

    @Test
    public void chooseHoldTakesFarLipOnlyWhenKnownHigher() {
        atlantis.map.position.APosition ours = atlantis.map.position.APosition.create(20, 20);
        atlantis.map.position.APosition foeHold = atlantis.map.position.APosition.create(30, 20);

        assertEquals(foeHold, atlantis.combat.strategy.ChokeStandoff.chooseHold(ours, foeHold, 0, 2));
        assertEquals(ours, atlantis.combat.strategy.ChokeStandoff.chooseHold(ours, foeHold, 2, 2));
        assertEquals(ours, atlantis.combat.strategy.ChokeStandoff.chooseHold(ours, foeHold, -1, 2));
        assertEquals(ours, atlantis.combat.strategy.ChokeStandoff.chooseHold(ours, null, 0, 2));
    }
}
