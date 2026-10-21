package tests.unit;

import atlantis.battle.strategy.HighGround;
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
}
