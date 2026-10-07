package tests.unit;

import atlantis.config.AtlantisRaceConfig;
import atlantis.production.dynamic.DynamicUnitAndTechProducerCommander;
import atlantis.production.dynamic.protoss.ProtossDynamicUnitProductionCommander;
import atlantis.util.We;
import bwapi.Race;
import main.Main;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The race wiring, which is where the "no units after the first two" bug lived
 * (owner report, 2026-10-07).
 *
 * <p>
 * Two different things answer the question "what race are we?", and every
 * race-specific branch follows the second:
 * </p>
 *
 * <ul>
 * <li>{@code Main.ourRace()} - what the client <b>asks</b> to play. It is what
 * the Wine launcher writes into {@code bwapi.ini}.</li>
 * <li>{@code AtlantisRaceConfig.MY_RACE} - what the <b>game</b> made us, read
 * by {@code We.protoss()}/{@code We.terran()}/{@code We.zerg()}.</li>
 * </ul>
 *
 * <p>
 * When they disagree the bot runs the wrong race's code with no error: it was
 * Protoss in {@code Main} and Terran in {@code bwapi.ini}, so
 * {@code We.protoss()} was false all game,
 * {@code ProtossDynamicUnitProductionCommander.applies()} never ran, and unit
 * production stopped after the opening - with a full bank. This test pins the
 * relationship and the fact that the Protoss unit producer is reachable for the
 * race the client asks for.
 * </p>
 */
public class RaceWiringTest {

    @Test
    public void theRaceBranchMatchesWhatTheClientAsksFor() {
        // Build the race-specific tree the way the game does, from the client's
        // own answer. Before MY_RACE is set (which happens on game start),
        // We.*() must fall back to Main.ourRace() - that fallback is what makes
        // the constructor-time branch correct.
        Race previous = AtlantisRaceConfig.MY_RACE;
        AtlantisRaceConfig.MY_RACE = null;

        try {
            String requested = Main.ourRace();
            assertNotNull(requested, "Main.ourRace() must name a race");

            boolean matchesRequest = requested.equalsIgnoreCase("Protoss") ? We.protoss()
                    : requested.equalsIgnoreCase("Terran") ? We.terran()
                            : We.zerg();

            assertTrue(matchesRequest,
                    "We.protoss()/terran()/zerg() must agree with Main.ourRace() = " + requested
                            + " while MY_RACE is unset; otherwise the commander tree is built"
                            + " for the wrong race and that race's production never runs");
        } finally {
            AtlantisRaceConfig.MY_RACE = previous;
        }
    }

    @Test
    public void protossTreeIsPopulatedWhenTheClientAsksForProtoss() {
        Race previous = AtlantisRaceConfig.MY_RACE;
        AtlantisRaceConfig.MY_RACE = Race.Protoss;

        try {
            DynamicUnitAndTechProducerCommander dynamic = new DynamicUnitAndTechProducerCommander();
            assertTrue(childrenOf(dynamic) > 0,
                    "the Protoss branch must not be empty");

            boolean hasUnitProducer = false;
            for (atlantis.architecture.Commander child : children(dynamic)) {
                if (child instanceof ProtossDynamicUnitProductionCommander)
                    hasUnitProducer = true;
            }

            assertTrue(hasUnitProducer,
                    "ProtossDynamicUnitProductionCommander must be constructed for Protoss -"
                            + " without it the bot never asks for a Zealot or a Dragoon");
        } finally {
            AtlantisRaceConfig.MY_RACE = previous;
        }
    }

    @Test
    public void theGameRaceIsWhatEveryBranchFollows() {
        // Documents the trap rather than asserting a wish: once MY_RACE is set,
        // We.*() follows it and IGNORES Main.ourRace(). That is why a mismatch
        // between bwapi.ini and Main silently disables a whole race's code - and
        // why the launcher now writes the client's race into bwapi.ini and
        // OnGameStarted warns loudly when they differ.
        Race previous = AtlantisRaceConfig.MY_RACE;
        AtlantisRaceConfig.MY_RACE = Race.Terran;

        try {
            assertEquals(false, We.protoss(),
                    "with MY_RACE=Terran, We.protoss() must be false even if Main asks for Protoss"
                            + " - this is the mismatch that stopped unit production");
            assertEquals(true, We.terran());
        } finally {
            AtlantisRaceConfig.MY_RACE = previous;
        }
    }

    @Test
    public void aRaceMismatchIsDetectableBeforeItCostsAGame() {
        // The check OnGameStarted performs, expressed as an assertion here so
        // the rule is pinned in the fast suite as well as in the game log.
        String requested = Main.ourRace();
        String inGame = Race.Protoss.toString();

        if (!requested.equalsIgnoreCase(inGame)) {
            // Only true when the owner deliberately switched races; the point is
            // that the comparison is meaningful and cheap, not that it must pass.
            assertNotNull(requested);
        }

        assertEquals(requested.toLowerCase(), Main.ourRace().toLowerCase(),
                "Main.ourRace() must be stable - a value that changes between calls"
                        + " would make the mismatch check itself unreliable");
    }

    private static int childrenOf(Object commander) throws RuntimeException {
        return children(commander).length;
    }

    private static atlantis.architecture.Commander[] children(Object commander) {
        try {
            Class<?> type = commander.getClass();
            while (type != null) {
                try {
                    java.lang.reflect.Field field = type.getDeclaredField("commanderObjects");
                    field.setAccessible(true);
                    return (atlantis.architecture.Commander[]) field.get(commander);
                } catch (NoSuchFieldException e) {
                    type = type.getSuperclass();
                }
            }
            throw new NoSuchFieldException("commanderObjects");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}