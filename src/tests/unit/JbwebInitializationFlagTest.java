package tests.unit;

import jbweb.JBWEB;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * {@code JBWEB.isInitialized()} must mean "onStart finished", not "onStart was
 * entered".
 *
 * <p>
 * {@code onStart} assigns {@code game} on its first line and fills the grids
 * afterwards, and several of those calls are JNI-backed natives that do not
 * exist
 * on Linux - where {@code InitJBWEB.init()} throws and {@code AMap} catches it
 * and
 * continues ({@code _AI/LOCAL-STARCRAFT.md} 187-189). With the old
 * {@code return game != null}, OpenBW therefore reported a usable JBWEB with
 * half-built (or empty) grids, {@code MapTiles.canBuildHere} took the
 * {@code JBWEB.isPlaceable} path, and every valid tile was refused:
 *
 * <pre>
 * 0:39: Can't find place for `Pylon` ... (reason: Can't physically build here)
 * </pre>
 *
 * <p>
 * This test pins the flag's meaning without needing a game: nothing has been
 * started in this JVM, so it must be false. The OpenBW end-to-end run is what
 * proves the positive case.
 * </p>
 */
public class JbwebInitializationFlagTest {

    @Test
    public void jbwebIsNotInitializedBeforeOnStart() {
        assertFalse(JBWEB.isInitialized(),
                "isInitialized() must be false until onStart has run to completion; a bare "
                        + "'game != null' is true partway through onStart, which is how OpenBW "
                        + "ended up trusting grids that were never filled");
    }
}
