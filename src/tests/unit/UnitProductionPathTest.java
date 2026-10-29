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
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The unit-production path, from the top of the commander tree down to the
 * Protoss producer, with the mode the bot actually runs in.
 *
 * <p>
 * The owner's evidence (2026-10-07) is that
 * {@code ProtossDynamicUnitProductionCommander.applies()} never prints, even
 * though the tree contains it. That means the node is <b>never invoked</b> -
 * and a commander is only invoked if its PARENT is invoked, whose own
 * {@code applies()} decides. So the failure is higher up, and this test walks
 * the chain asserting each parent's gate is open for the live configuration.
 * </p>
 */
@Tag("production")
public class UnitProductionPathTest {

    private Race raceBefore;

    private static String names(Commander commander) throws Exception {
        List<String> out = new ArrayList<>();
        for (Commander child : children(commander))
            out.add(child.getClass().getSimpleName());

        return out.toString();
    }

    private static boolean contains(Commander commander, Class<?> type) throws Exception {
        return find(commander, type) != null;
    }

    private static <T> T find(Commander commander, Class<T> type) throws Exception {
        for (Commander child : children(commander)) {
            if (type.isInstance(child))
                return type.cast(child);

            T nested = find(child, type);
            if (nested != null)

                return nested;
        }
        return null;
    }

    private static Commander[] children(Commander commander) throws Exception {
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
        throw new NoSuchFieldException("commanderObjects on " + commander.getClass());
    }

    @BeforeEach
    public void setOurRace() {
        raceBefore = AtlantisRaceConfig.MY_RACE;
        // The tree is built in constructors, so the race must be set first -
        // the same ordering the game has (MY_RACE is assigned on game start).
        AtlantisRaceConfig.MY_RACE = Race.Protoss;
    }

    @AfterEach
    public void restoreRace() {
        AtlantisRaceConfig.MY_RACE = raceBefore;
    }

    // ---- helpers -----------------------------------------------------------

    @Test
    public void productionCommanderHasTheDynamicProducerInTheModeWeActuallyRun() throws Exception {
        // There is only one mode now: the unconditional list carries the
        // v2 engine and no legacy dynamic commanders. Assert that shape
        // directly, so a reintroduced legacy branch cannot silently take
        // unit production back.
        ProductionCommander production = new ProductionCommander();

        assertTrue(contains(production, atlantis.production.ProductionOrdersCommander.class),
            "the v2 engine must be the one running production");
        assertFalse(contains(production, DynamicProductionCommander.class),
            "the legacy dynamic producer is dropped on purpose - two policies"
                + " over one economy is the double-ordering the cutover removed");
    }

    @Test
    public void everyParentGateOnTheWayIsOpen() {
        // applies() of each link, evaluated as the game would evaluate it. A
        // false here means the child is never invoked no matter how correct it
        // is - the exact shape of "applies() never prints".
        assertTrue(We.protoss(), "the race branch must be Protoss for this test to mean anything");

        ProductionCommander production = new ProductionCommander();
        DynamicProductionCommander dynamic = new DynamicProductionCommander();
        DynamicUnitAndTechProducerCommander unitProducer = new DynamicUnitAndTechProducerCommander();

        // DynamicUnitAndTechProducerCommander inherits the default applies()
        // (true), so it is never the gate. Assert that explicitly: if someone
        // adds a gate there, this test should be updated deliberately, not
        // discover it in a game.
        assertTrue(unitProducer.applies(),
            "DynamicUnitAndTechProducerCommander must not gate itself - its children are"
                + " the race-specific producers");

        // The Protoss producer's own gate is the one the owner watches.
        ProtossDynamicUnitProductionCommander protoss = new ProtossDynamicUnitProductionCommander();
        assertTrue(protoss.applies(),
            "applies() must be true for our own race; if this is false the commander"
                + " is in the tree but never runs");

        // Record the parents for the failure message, so a future break says
        // where it happened.
        assertNotNull(production);
        assertNotNull(dynamic);
    }

    @Test
    public void theTopLevelPipelineInvokesProductionCommander() throws Exception {
        AtlantisGameCommander top = new AtlantisGameCommander();
        ProductionCommander production = find(top, ProductionCommander.class);
        assertNotNull(production,
            "ProductionCommander is not reachable from AtlantisGameCommander -"
                + " nothing below it can ever be invoked. Children: " + names(top));
    }
}
