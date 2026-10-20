package tests.unit;

import atlantis.map.position.PsiField;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The Psi field follows the engine's mask applied to the footprint CENTER,
 * not a Chebyshev radius around the origin tile.
 *
 * <p>
 * Read from {@code /sc-ai/OpenBW/openbw/bwgame.h} ({@code
 * is_in_psionic_matrix_range} + {@code psi_field_mask}) and from the client
 * that enforces it at issue time ({@code bwapi/Game.java}: {@code
 * canBuildHere} tests power via {@code hasPower(lt, type)}, which resolves to
 * the footprint center). All cases below use a Gateway (4x3, center offset
 * +64/+48px) against finished Pylons.
 * </p>
 */
@Tag("placement")
public class PsiFieldTest {

    private static final AUnitType GATEWAY = AUnitType.Protoss_Gateway;

    private static boolean covers(int px, int py, int tx, int ty) {
        return PsiField.covers(
            px, py, tx, ty, GATEWAY.getTilesWidth(), GATEWAY.getTilesHeights());
    }

    @Test
    public void adjacentTilesArePowered() {
        assertTrue(covers(16, 46, 16, 48), "two tiles below the Pylon");
        assertTrue(covers(16, 46, 14, 48), "diagonal, well inside");
        assertTrue(covers(16, 46, 9, 46), "7 tiles west - the engine reaches 8");
    }

    @Test
    public void theGameCaseGateway1451IsNotPowered() {
        // Measured 20261013_112034_long: Gateway at [14,51] refused 315 times
        // with hasPower=false while a finished Pylon stood at [16,46].
        assertFalse(
            covers(16, 46, 14, 51),
            "center dy is 208px, outside the engine's 160px vertical bound"
        );

        assertFalse(
            covers(16, 46, 14, 50),
            "origin dy is 128px (inside) but center dy is 176px (outside) - "
                + "this is why the origin tile is the wrong input"
        );
    }

    @Test
    public void farTilesAndTaperedCornersAreNotPowered() {
        assertFalse(covers(50, 50, 62, 50), "12 tiles away is out");
        assertFalse(covers(60, 60, 67, 67), "the tapered corner is out");
        assertFalse(covers(16, 46, 10, 50), "tapered corner on the real map shape");
    }
}
