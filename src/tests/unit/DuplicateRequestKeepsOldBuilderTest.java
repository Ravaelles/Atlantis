package tests.unit;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A refused duplicate must not steal: the request path assigned the
 * optimal builder BEFORE asking whether the row already exists, so a
 * duplicate stole the live row's builder on its way to being
 * cancelled - the old row re-armed with a second worker and two
 * probes walked to one site (owner live game: second Gateway never
 * warped, both workers gave up to minerals). Only a surviving row
 * may take a builder, so the assignment must come after both
 * duplicate checks. Pinned structurally: the behavior needs a
 * siting-shaped world, the order is what carries it.
 */
@Tag("production")
public class DuplicateRequestKeepsOldBuilderTest {

    private static final String FUNNEL =
        "src/atlantis/production/constructions/NewConstructionRequest.java";

    private static String read() throws Exception {
        return new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(FUNNEL)),
            java.nio.charset.StandardCharsets.UTF_8);
    }

    @Test
    public void optimalBuilderIsAssignedOnlyToASurvivingRow() throws Exception {
        String funnel = read();

        int assign = funnel.indexOf("newConstruction.assignOptimalBuilder()");
        int firstCheck = funnel.indexOf("alreadyExists(newConstruction, false)");
        int strictCheck = funnel.indexOf("alreadyExists(newConstruction, true)");

        assertTrue(assign > 0, "the assignment must exist");
        assertTrue(firstCheck > 0 && strictCheck > 0, "both duplicate checks must exist");
        assertTrue(assign > strictCheck,
            "assignOptimalBuilder must come AFTER both duplicate checks, or a "
                + "refused duplicate steals the live row's builder on its way "
                + "to the cancel (two workers, one site, both give up)");
    }
}
