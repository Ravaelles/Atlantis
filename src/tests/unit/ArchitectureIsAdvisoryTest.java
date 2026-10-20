package tests.unit;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The architecture scope warns in the fast loop; it does not fail the build.
 *
 * <p>
 * Owner's ruling (2026-10-11, {@code CONVENTIONS.md} §5): "we should be moving
 * towards fixing
 * it, but it should not break whole builds - that slows the work down; it is a
 * hint, not a
 * requirement." A new boundary crossing in unrelated work turned the inner loop
 * red and cost a
 * detour to diagnose - twice in one session, both times while instrumenting a
 * diagnostic.
 * </p>
 *
 * <p>
 * <b>What must be true, and is pinned here:</b>
 * </p>
 *
 * <ol>
 * <li>the architecture scope is run in its own pass, so a boundary failure can
 * be downgraded
 * to a warning;</li>
 * <li>a NORMAL test failure is never downgraded - that would hide real
 * regressions, which is
 * the opposite of the intent. The normal pass exits with its own code before
 * the advisory
 * pass runs;</li>
 * <li>the authoritative gate stays strict: {@code run-architecture-tests.sh}
 * has no advisory
 * logic, so the owner still has a hard check.</li>
 * </ol>
 *
 * <p>
 * Source-pinned on purpose: the behaviour is a property of how the scope is
 * INVOKED, and the
 * stub world has no way to make a boundary rule fire. Both directions were
 * verified by hand
 * against the real scripts (a planted boundary crossing exits 0 with a warning;
 * a planted
 * failing unit test still exits non-zero) - this test keeps the arrangement
 * from silently
 * changing back.
 * </p>
 */
@Tag("infra")
public class ArchitectureIsAdvisoryTest {

    private static final String FAST = "scripts/run-tests.sh";
    private static final String STRICT = "scripts/run-architecture-tests.sh";

    @Test
    public void theFastLoopDowngradesOnlyTheArchitectureScope() throws Exception {
        String source = read(FAST);

        assertTrue(source.contains("ARCHITECTURE WARNING"),
                "the fast runner must report a boundary failure as a warning");

        assertTrue(source.contains("ARCH_ADVISORY"),
                "the architecture scope must be tracked separately (ARCH_ADVISORY), not mixed into "
                        + "the normal scope - a shared pass would have to downgrade unit failures too");

        assertTrue(source.contains("exit $?") || source.contains("|| exit"),
                "the normal scope must exit with its OWN code before the advisory pass runs, so a"
                        + " real unit/e2e regression still fails the build");

        // The advisory pass must come after the normal one; otherwise a boundary
        // warning could
        // short-circuit the tests that actually matter.
        int normalRun = source.indexOf("org.junit.platform.console.ConsoleLauncher");
        int advisoryRun = source.lastIndexOf("org.junit.platform.console.ConsoleLauncher");
        assertTrue(normalRun >= 0 && advisoryRun > normalRun,
                "the normal scope must run before the advisory architecture scope");
    }

    @Test
    public void theAuthoritativeGateStaysStrict() throws Exception {
        String source = read(STRICT);

        assertTrue(!source.contains("ARCHITECTURE WARNING"),
                "run-architecture-tests.sh is the authoritative gate and must not downgrade a"
                        + " failure to a warning - that is the inner loop's concession, not the gate's");

        assertTrue(source.contains("set -euo pipefail"),
                "the authoritative gate must keep failing hard");
    }

    private static String read(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }
}
