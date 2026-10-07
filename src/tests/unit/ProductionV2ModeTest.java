package tests.unit;

import atlantis.config.env.ProductionV2Mode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins the cutover switch of the production redesign (M6 of
 * _AI/redesign/01_PRODUCTION.md): {@code PRODUCTION_V2} in ENV chooses between
 * the legacy queue, a dry run of the new engine, and the new engine playing
 * alone.
 *
 * <p>
 * The parse rule is the part worth testing, and the reason is a real risk
 * rather than a formality: a flag that silently interprets a typo as "enabled"
 * would put an unverified production policy into a tournament game. The default
 * here is OFF and anything unrecognised stays OFF.
 * </p>
 */
public class ProductionV2ModeTest {

    @Test
    public void offIsTheDefaultForAnythingUnrecognised() {
        assertEquals(ProductionV2Mode.OFF, ProductionV2Mode.parse(null));
        assertEquals(ProductionV2Mode.OFF, ProductionV2Mode.parse(""));
        assertEquals(ProductionV2Mode.OFF, ProductionV2Mode.parse("false"));
        assertEquals(ProductionV2Mode.OFF, ProductionV2Mode.parse("off"));
        assertEquals(ProductionV2Mode.OFF, ProductionV2Mode.parse("DRY RUN"),
                "a space is not a valid mode - OFF, not a guess");
        assertEquals(ProductionV2Mode.OFF, ProductionV2Mode.parse("drry_run"));
    }

    @Test
    public void dryRunIsRecognisedCaseInsensitively() {
        assertEquals(ProductionV2Mode.DRY_RUN, ProductionV2Mode.parse("DRY_RUN"));
        assertEquals(ProductionV2Mode.DRY_RUN, ProductionV2Mode.parse("dry_run"));
        assertEquals(ProductionV2Mode.DRY_RUN, ProductionV2Mode.parse("DRYRUN"));
        assertEquals(ProductionV2Mode.DRY_RUN, ProductionV2Mode.parse("  dry_run  "));
    }

    @Test
    public void liveIsRecognisedByItsSpellings() {
        assertEquals(ProductionV2Mode.LIVE, ProductionV2Mode.parse("LIVE"));
        assertEquals(ProductionV2Mode.LIVE, ProductionV2Mode.parse("true"));
        assertEquals(ProductionV2Mode.LIVE, ProductionV2Mode.parse("ON"));
    }

    @Test
    public void modesAnswerTheirOwnQuestions() {
        assertFalse(ProductionV2Mode.OFF.isEnabled());
        assertFalse(ProductionV2Mode.OFF.isDryRun());
        assertFalse(ProductionV2Mode.OFF.isLive());

        assertTrue(ProductionV2Mode.DRY_RUN.isEnabled());
        assertTrue(ProductionV2Mode.DRY_RUN.isDryRun());
        assertFalse(ProductionV2Mode.DRY_RUN.isLive());

        assertTrue(ProductionV2Mode.LIVE.isEnabled());
        assertFalse(ProductionV2Mode.LIVE.isDryRun());
        assertTrue(ProductionV2Mode.LIVE.isLive());
    }
}
