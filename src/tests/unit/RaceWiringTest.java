package tests.unit;

import atlantis.Atlantis;
import atlantis.config.AtlantisConfigChanger;
import atlantis.config.AtlantisRaceConfig;
import atlantis.production.dynamic.DynamicUnitAndTechProducerCommander;
import atlantis.production.dynamic.protoss.ProtossDynamicUnitProductionCommander;
import atlantis.util.We;
import bwapi.Race;
import main.Main;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins the single source of truth for the race (owner's ruling, 2026-10-07:
 * "let Main.ourRace be the source of truth").
 *
 * <p>
 * The bug this closes: the race had TWO owners. {@code Main.ourRace()} said what
 * the bot wanted to play, while {@code AtlantisRaceConfig.MY_RACE} was filled
 * from the game ({@code Atlantis.game().self().getRace()}) - so {@code bwapi.ini}
 * decided, and every race branch followed {@code MY_RACE}. With the ini saying
 * Terran and Main saying Protoss, {@code We.protoss()} was false for the whole
 * game, the Protoss unit producer was never applicable, and production stopped
 * after the opening with a full bank.
 * </p>
 *
 * <p>
 * {@code We.*()} now reads {@code Main.ourRace()} directly and ignores
 * {@code MY_RACE} for the race question, so the two cannot disagree about which
 * race's code runs. {@code MY_RACE} keeps its other job: it carries the race's
 * unit types ({@code BASE}, {@code WORKER}, ...).
 * </p>
 */
public class RaceWiringTest {

    @AfterEach
    public void restoreRace() {
        AtlantisRaceConfig.MY_RACE = null;
    }

    @Test
    public void weFollowsMainNotTheGameConfig() {
        // The exact mismatch that stopped production: MY_RACE says Terran, Main
        // says Protoss. Main must win, because Main is the source of truth.
        AtlantisRaceConfig.MY_RACE = Race.Terran;

        assertEquals("Protoss", Main.ourRace(),
                "this test is only meaningful while Main asks for Protoss");
        assertEquals(true, We.protoss(),
                "We.protoss() must follow Main.ourRace(), not MY_RACE - the mismatch"
                        + " between them is what silently disabled all Protoss code");
        assertEquals(false, We.terran(),
                "and it must not report the other race either");
    }

    @Test
    public void weAgreesWithMainWhateverMainSays() {
        String requested = Main.ourRace();
        assertNotNull(requested);

        boolean matchesRequest = requested.equalsIgnoreCase("Protoss") ? We.protoss()
                : requested.equalsIgnoreCase("Terran") ? We.terran()
                : We.zerg();

        assertTrue(matchesRequest,
                "We.protoss()/terran()/zerg() must agree with Main.ourRace() = " + requested);

        // And exactly one of them is true - never zero (no race) and never two.
        int howMany = (We.protoss() ? 1 : 0) + (We.terran() ? 1 : 0) + (We.zerg() ? 1 : 0);
        assertEquals(1, howMany, "exactly one race must be selected, got " + howMany);
    }

    @Test
    public void theConfigChangerBuildsTheRaceTypesFromMain() {
        AtlantisConfigChanger.modifyRacesInConfigFileIfNeeded();

        if (We.protoss()) {
            assertEquals(Race.Protoss, AtlantisRaceConfig.MY_RACE,
                    "MY_RACE must be set from Main.ourRace(), not from the game");
            assertNotNull(AtlantisRaceConfig.BASE, "BASE must be configured for the race");
            assertNotNull(AtlantisRaceConfig.WORKER, "WORKER must be configured for the race");
        }
    }

    @Test
    public void theConfigChangerDoesNotConsultTheGame() {
        // The old implementation read Atlantis.game().self().getRace(), which
        // needs a running game and made bwapi.ini the authority. Assert the
        // method works with NO game object at all - that is what proves the game
        // is no longer consulted (and it is also why this can be a unit test
        // instead of an E2E one).
        Atlantis.getInstance().setGame(null);

        AtlantisConfigChanger.modifyRacesInConfigFileIfNeeded();

        assertNotNull(AtlantisRaceConfig.MY_RACE,
                "the race config must be settable without a game object");
    }

    @Test
    public void protossUnitProducerIsConstructedForTheRequestedRace() throws Exception {
        // The end of the chain the owner watched: with the race sourced from
        // Main, the Protoss producer must be in the tree AND applicable.
        AtlantisConfigChanger.modifyRacesInConfigFileIfNeeded();

        DynamicUnitAndTechProducerCommander dynamic = new DynamicUnitAndTechProducerCommander();
        boolean found = false;
        for (atlantis.architecture.Commander child : children(dynamic)) {
            if (child instanceof ProtossDynamicUnitProductionCommander) found = true;
        }

        assertTrue(found,
                "ProtossDynamicUnitProductionCommander must be built when Main asks for Protoss -"
                        + " its absence is the 'no units after the opening' symptom");

        if (We.protoss()) {
            ProtossDynamicUnitProductionCommander producer =
                    new ProtossDynamicUnitProductionCommander();
            assertTrue(producer.applies(),
                    "applies() must be true for our own race, or the node is in the tree"
                            + " but never runs");
        }
    }

    // ---- helpers -----------------------------------------------------------

    private static atlantis.architecture.Commander[] children(Object commander) throws Exception {
        Class<?> type = commander.getClass();
        while (type != null) {
            try {
                Field field = type.getDeclaredField("commanderObjects");
                field.setAccessible(true);
                return (atlantis.architecture.Commander[]) field.get(commander);
            } catch (NoSuchFieldException e) {
                type = type.getSuperclass();
            }
        }
        throw new NoSuchFieldException("commanderObjects on " + commander.getClass());
    }
}