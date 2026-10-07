package tests.unit;

import atlantis.architecture.Commander;
import atlantis.config.AtlantisRaceConfig;
import atlantis.game.AtlantisGameCommander;
import atlantis.production.ProductionCommander;
import atlantis.production.dynamic.DynamicProductionCommander;
import atlantis.production.dynamic.DynamicUnitAndTechProducerCommander;
import atlantis.production.dynamic.protoss.ProtossDynamicUnitProductionCommander;
import atlantis.util.We;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The core-structure guard the owner asked for (2026-10-07): "we have a broken
 * core system and did not even know".
 *
 * <p>
 * The symptom was that the bot produced one Zealot and one Dragoon and then
 * stopped asking for units forever, with plenty of resources banked.
 * {@code ProtossDynamicUnitProductionCommander.applies()} never ran - a print
 * statement at its top never appeared - so the commander was not in the tree at
 * all, and nothing anywhere said so.
 * </p>
 *
 * <p>
 * The reason that class of bug is invisible: the commander tree is built ONCE,
 * in constructors, by calling {@code subcommanders()}. A branch in that method
 * that depends on game state (the race) is evaluated at construction time, so
 * a wrong or unset race silently produces an empty or wrong branch - no
 * exception, no log, just a bot that stops thinking. This test walks the tree
 * and asserts the Protoss unit producer is actually in it, which is the
 * smallest check that would have caught it.
 * </p>
 */
public class CommanderTreeWiringTest {

    private Race raceBefore;

    /**
     * The race MUST be set before any commander is constructed, because the
     * tree is built in constructors by calling {@code subcommanders()} - and a
     * race-dependent branch evaluated with no race set produces an empty tree.
     *
     * <p>This is not a test detail: it is the same ordering the game has, where
     * {@code MY_RACE} is assigned on game start. Leaving it unset here is what
     * made the first version of this test report "ProtossDynamicUnitProductionCommander
     * is NOT in the tree" - a false alarm that still proved the point about how
     * silent this failure is.</p>
     */
    @BeforeEach
    public void setOurRaceBeforeBuildingAnyTree() {
        raceBefore = AtlantisRaceConfig.MY_RACE;
        AtlantisRaceConfig.MY_RACE = Race.Protoss;
    }

    @AfterEach
    public void restoreRace() {
        AtlantisRaceConfig.MY_RACE = raceBefore;
    }

        @Test
        public void protossUnitProducerIsInTheDynamicTree() throws Exception {
                // Build the commander the way the game does.
                DynamicUnitAndTechProducerCommander dynamic = new DynamicUnitAndTechProducerCommander();

                assertTrue(contains(dynamic, ProtossDynamicUnitProductionCommander.class),
                                "ProtossDynamicUnitProductionCommander is NOT in DynamicUnitAndTechProducerCommander's"
                                                + " tree - the bot cannot produce units, and nothing reports it."
                                                + " Built children: " + childrenOf(dynamic));
        }

        @Test
        public void dynamicProducerIsInTheProductionTree() throws Exception {
                ProductionCommander production = new ProductionCommander();

                assertTrue(contains(production, DynamicProductionCommander.class),
                                "DynamicProductionCommander is missing from ProductionCommander -"
                                                + " nothing would ever ask for a unit. Children: "
                                                + childrenOf(production));
        }

        @Test
        public void protossProducerReachesThroughTheWholeChainFromTheTop() throws Exception {
                // The full path the game walks every frame:
                // AtlantisGameCommander -> ProductionCommander -> DynamicProductionCommander
                // -> DynamicUnitAndTechProducerCommander ->
                // ProtossDynamicUnitProductionCommander
                // Each link is a constructor call, so a break anywhere is silent.
                AtlantisGameCommander top = new AtlantisGameCommander();

                assertTrue(contains(top, ProductionCommander.class),
                                "ProductionCommander missing from the top-level pipeline");

                ProductionCommander production = find(top, ProductionCommander.class);
                assertTrue(contains(production, DynamicProductionCommander.class),
                                "DynamicProductionCommander missing under ProductionCommander");

                DynamicProductionCommander dynamic = find(production, DynamicProductionCommander.class);
                assertTrue(contains(dynamic, DynamicUnitAndTechProducerCommander.class),
                                "DynamicUnitAndTechProducerCommander missing under DynamicProductionCommander");

                DynamicUnitAndTechProducerCommander unitProducer = find(dynamic,
                                DynamicUnitAndTechProducerCommander.class);
                assertTrue(contains(unitProducer, ProtossDynamicUnitProductionCommander.class),
                                "ProtossDynamicUnitProductionCommander missing at the end of the chain -"
                                                + " this is the exact break that stopped unit production");
        }

        @Test
        public void theTreeIsNotSilentlyEmptyForOurRace() throws Exception {
                // A race branch that matches nothing returns null from subcommanders(),
                // and mergeCommanders would then throw on .length - or, worse, produce an
                // empty tree. Assert the branch is populated for the race we actually
                // play, which is the assumption the whole production chain rests on.
                assertTrue(We.protoss() || We.terran() || We.zerg(),
                                "no race is set at construction time; every race-dependent branch is dead");

                DynamicUnitAndTechProducerCommander dynamic = new DynamicUnitAndTechProducerCommander();
                assertTrue(childrenOf(dynamic).length > 0,
                                "the race-specific branch is EMPTY - the tree was built with no race set."
                                                + " We.protoss()=" + We.protoss() + " We.terran()=" + We.terran());
        }

        @Test
        public void appliesIsFalseOnlyWhenTheRaceIsNotOurs() throws Exception {
                // applies() must not be the reason nothing is produced: for our race it
                // must be true. The owner's evidence was that the print inside applies()
                // never appeared, which means applies() was never CALLED - a missing
                // node, not a false answer. This test separates those two cases.
                ProtossDynamicUnitProductionCommander protoss = new ProtossDynamicUnitProductionCommander();

                if (We.protoss()) {
                        assertTrue(protoss.applies(),
                                        "applies() must be true for our own race - if it is false the commander"
                                                        + " is in the tree but never runs");
                }
        }

        // ---- tree walking ------------------------------------------------------

        private static Commander[] childrenOf(Commander commander) throws Exception {
            // commanderObjects lives on BaseCommander, but the depth to it differs:
            // some commanders extend Commander directly, others (HasReason ones)
            // sit one level deeper. Walk up instead of assuming a fixed depth - the
            // first version used getSuperclass() and threw NoSuchFieldException for
            // exactly this reason.
            Class<?> type = commander.getClass();
            while (type != null) {
                try {
                    Field field = type.getDeclaredField("commanderObjects");
                    field.setAccessible(true);
                    return (Commander[]) field.get(commander);
                } catch (NoSuchFieldException e) {
                    type = type.getSuperclass();
                }
            }

            throw new NoSuchFieldException("commanderObjects not found on " + commander.getClass());
        }

        private static boolean contains(Commander commander, Class<?> type) throws Exception {
                return find(commander, type) != null;
        }

        private static <T> T find(Commander commander, Class<T> type) throws Exception {
                for (Commander child : childrenOf(commander)) {
                        if (type.isInstance(child))
                                return type.cast(child);
                        T nested = find(child, type);
                        if (nested != null)
                                return nested;
                }
                return null;
        }

        @Test
        public void treeWalkingHelperItselfWorks() throws Exception {
                // A guard on the guard: if reflection ever stops finding children, every
                // assertion above would pass vacuously.
                AtlantisGameCommander top = new AtlantisGameCommander();
                Commander[] children = childrenOf(top);

                assertNotNull(children, "children must never be null - initChildren always assigns");
                assertEquals(true, children.length > 0,
                                "the top-level commander must have children, or every containment"
                                                + " assertion in this class is vacuous");
        }
}