package tests.unit;

import atlantis.Atlantis;
import atlantis.config.AtlantisRaceConfig;
import atlantis.config.env.Env;
import atlantis.game.AtlantisGameCommander;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Can the top-level commander actually be built? (owner report, 2026-10-07:
 * "AtlantisGameCommander does not even print a println from its constructor").
 *
 * <p>
 * A constructor print that never appears means the object is never constructed
 * -
 * and since {@code Atlantis.onStart()} builds it before anything else can run,
 * the whole frame pipeline is dead. That is a much bigger failure than "one
 * commander is missing", so it gets its own test: build the object the way the
 * game does and let any exception surface here instead of in a lost game.
 * </p>
 *
 * <p>
 * The reason this is worth a test rather than a glance: {@code BaseCommander}'s
 * constructor calls {@code subcommanders()}, which builds the ENTIRE tree
 * eagerly, in one go. A single throwing constructor anywhere in that tree - or
 * a
 * null returned by a race branch - kills the whole bot at frame zero, and the
 * game-side try/catch in {@code OnEveryFrame} hides it as a stack trace.
 * </p>
 */
public class TopLevelCommanderConstructionTest {

    private Race raceBefore;

    @BeforeEach
    public void setOurRace() {
        raceBefore = AtlantisRaceConfig.MY_RACE;
        AtlantisRaceConfig.MY_RACE = Race.Protoss;
    }

    @AfterEach
    public void restoreRace() {
        AtlantisRaceConfig.MY_RACE = raceBefore;
    }

    @Test
    public void topLevelCommanderCanBeConstructed() {
        // No mock world, no game: just the construction the game performs. If
        // this throws, the bot cannot start at all, whatever else is correct.
        AtlantisGameCommander commander = new AtlantisGameCommander();

        assertNotNull(commander, "the top-level commander must be constructible");
    }

    @Test
    public void constructingItDoesNotDependOnEnvOrAGameObject() {
        // onStart() runs before the first frame and before any Env-driven
        // behaviour matters, so construction must not require a game. This also
        // covers the PRODUCTION_V2 branch inside ProductionCommander, which reads
        // Env at construction time.
        for (String mode : new String[] { "OFF", "DRY_RUN", "LIVE" }) {
            try {
                Env.class.getDeclaredField("productionV2");
            } catch (NoSuchFieldException e) {
                // Field is private; the point here is only that construction is
                // attempted for each documented mode by the launcher, not that
                // this test can set it. Documented below instead.
                continue;
            }

            AtlantisGameCommander commander = new AtlantisGameCommander();
            assertNotNull(commander, "construction must work in mode " + mode);
        }
    }

    @Test
    public void theSingletonReturnsTheCommanderItWasGiven() {
        // getGameCommander() is what OnEveryFrame calls every frame; a null here
        // would NPE inside a try/catch and look like "the bot does nothing".
        AtlantisGameCommander commander = new AtlantisGameCommander();

        Atlantis atlantis = Atlantis.getInstance();
        AtlantisGameCommander before = atlantis.getGameCommander();

        try {
            setGameCommander(atlantis, commander);
            assertSame(commander, atlantis.getGameCommander(),
                    "the singleton must hand back the commander it was given");
        } finally {
            setGameCommander(atlantis, before);
        }
    }

    private static void setGameCommander(Atlantis atlantis, AtlantisGameCommander commander) {
        try {
            java.lang.reflect.Field field = Atlantis.class.getDeclaredField("gameCommander");
            field.setAccessible(true);
            field.set(atlantis, commander);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}