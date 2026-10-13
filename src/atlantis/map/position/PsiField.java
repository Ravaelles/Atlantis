package atlantis.map.position;

/**
 * The Protoss Psi field exactly as the engine enforces it.
 *
 * <p>
 * Read from {@code /sc-ai/OpenBW/openbw/bwgame.h}, not tuned: {@code
 * is_in_psionic_matrix_range} allows {@code |dx| < 256px} by {@code |dy| <
 * 160px} around the Pylon's sprite position and then applies {@code
 * psi_field_mask} (an 8-wide by 5-tall tile mask with tapered corners).
 * {@code can_place_building} refuses a power-needing building outside it, and
 * the build order dies the same way server-side in {@code
 * order_PlaceProtossBuilding} - so a finder that disagrees requests tiles the
 * engine will never accept.
 * </p>
 *
 * <p>
 * This replaces the old "6 tiles Chebyshev from the footprint" rule both
 * placement paths used. That rule was wrong in both axes: it allowed 6 tiles
 * vertically where the engine allows under 5 (which is how Gateway [14,51]
 * with a Pylon at [16,46] passed our check and died on the engine's), and it
 * cut the field at 6 horizontally where the engine reaches 8.
 * </p>
 *
 * <p>
 * The client enforces the same rule at issue time ({@code bwapi/Game.java}:
 * {@code canBuildHere} tests power via {@code hasPower(lt, type)}, which
 * resolves to the footprint CENTER, not the origin tile) - so the mask is
 * applied to the SITE CENTER here too. A site whose origin is inside but
 * whose center is outside (Gateway [14,50] with a Pylon at [16,46]: origin
 * dy=128px in, center dy=176px out) is refused by the engine; testing the
 * origin would repeat the old mistake one tile over.
 * </p>
 *
 * <p>
 * Pure and dependency-free on purpose: both placement paths (legacy exactly,
 * catalogue approximately - its power source has no footprint at the call
 * site) consult this rather than keeping their own radius guesses.
 * </p>
 */
public class PsiField {

    private static final boolean[][] PSI_FIELD_MASK = {
        {true, true, true, true, true, true, true, true},
        {true, true, true, true, true, true, true, true},
        {true, true, true, true, true, true, true, false},
        {true, true, true, true, true, true, false, false},
        {true, true, true, false, false, false, false, false},
    };

    private static final int HALF_WIDTH_PX = 256;
    private static final int HALF_HEIGHT_PX = 160;

    /**
     * Does a finished Pylon at (pylonTx, pylonTy) power a building of the
     * given footprint placed at (siteTx, siteTy)?
     *
     * <p>
     * The mask is applied to the footprint CENTER, mirroring the client. See
     * the class javadoc for why the origin tile is the wrong input.
     * </p>
     */
    public static boolean covers(
        int pylonTx, int pylonTy, int siteTx, int siteTy, int siteWTiles, int siteHTiles
    ) {
        int centerX = siteTx * 32 + siteWTiles * 16;
        int centerY = siteTy * 32 + siteHTiles * 16;

        int dx = Math.abs(centerX - pylonTx * 32);
        int dy = Math.abs(centerY - pylonTy * 32);

        if (dx >= HALF_WIDTH_PX || dy >= HALF_HEIGHT_PX) return false;

        return PSI_FIELD_MASK[dy / 32][dx / 32];
    }
}
