package tests.unit;

import atlantis.map.position.PsiField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The Psi field follows the engine's mask, not a Chebyshev radius.
 *
 * <p>
 * Read from {@code /sc-ai/OpenBW/openbw/bwgame.h} ({@code
 * is_in_psionic_matrix_range} + {@code psi_field_mask}): |dx| &lt; 256px by
 * |dy| &lt; 160px around the Pylon's sprite position, then an 8x5 tile mask
 * with tapered corners. The old "6 tiles Chebyshev from the footprint" rule
 * was wrong in both axes: it allowed 6 vertically where the engine allows
 * under 5, and cut 8 horizontally down to 6.
 * </p>
 */
public class PsiFieldTest {

    @Test
    public void adjacentTilesArePowered() {
        assertTrue(PsiField.covers(16, 46, 16, 48), "two tiles below the Pylon");
        assertTrue(PsiField.covers(16, 46, 14, 48), "diagonal, well inside");
        assertTrue(PsiField.covers(16, 46, 9, 46), "7 tiles west - the engine reaches 8");
    }

    @Test
    public void theGameCaseGateway1451IsNotPowered() {
        // Measured 20261013_112034_long: Gateway at [14,51] refused 315 times
        // with hasPower=false while a finished Pylon stood at [16,46]. The old
        // rule said powered (Chebyshev 4 from the footprint); the engine's
        // 160px vertical bound says otherwise (dy is exactly 160px).
        assertFalse(
            PsiField.covers(16, 46, 14, 51),
            "5 tiles below the Pylon is outside the engine's 160px vertical bound"
        );

        assertTrue(
            PsiField.covers(16, 46, 14, 50),
            "one tile nearer is inside - the boundary is razor-thin here"
        );
    }

    @Test
    public void farTilesAndTaperedCornersAreNotPowered() {
        assertFalse(PsiField.covers(50, 50, 62, 50), "12 tiles away is out");
        assertFalse(PsiField.covers(16, 46, 8, 46), "8 tiles west is exactly out (256px bound)");
        assertFalse(
            PsiField.covers(60, 60, 67, 67),
            "the tapered corner is out - the old footprint rule wrongly accepted this"
        );
        assertFalse(PsiField.covers(16, 46, 10, 50), "tapered corner on the real map shape");
    }
}
