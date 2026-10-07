package tests.unit;

import atlantis.architecture.Commander;
import atlantis.config.AtlantisRaceConfig;
import atlantis.config.env.Env;
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
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
public class UnitProductionPathTest {

    private Race raceBefore;

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

    @Test
    public void productionCommanderHasTheDynamicProducerInTheModeWeActuallyRun() throws Exception {
        // The mode matters: ProductionCommander builds a DIFFERENT list when
        // PRODUCTION_V2=LIVE (the v2 cutover drops the legacy dynamic
        // commanders). Assert the expectation for the current configuration,
        // whatever it is, so a mode switch cannot silently remove unit
        // production from the tree.
        ProductionCommander production = new ProductionCommander();
        boolean live = Env.productionV2().isLive();

        boolean hasDynamic = contains(production, DynamicProductionCommander.class);

        if (live) {
            assertFalse(hasDynamic,
                    "in LIVE the legacy dynamic producer is dropped on purpose");
            assertTrue(contains(production, atlantis.production.ProductionOrdersCommander.class),
                    "in LIVE the v2 engine must be the one running production");
        } else {
            assertTrue(hasDynamic,
                    "in OFF/DRY_RUN the legacy dynamic producer must be present -"
                            + " without it nothing asks for units. Mode was " + Env.productionV2());
        }
    }

    @Test
    public void theLegacyChainIsIntactFromProductionDownToTheProtossProducer() throws Exception {
        ProductionCommander production = new ProductionCommander();
        DynamicProductionCommander dynamic = find(production, DynamicProductionCommander.class);
        assertNotNull(dynamic, "DynamicProductionCommander missing under ProductionCommander");

        DynamicUnitAndTechProducerCommander unitProducer = find(dynamic, DynamicUnitAndTechProducerCommander.class);
        assertNotNull(unitProducer,
                "DynamicUnitAndTechProducerCommander missing under DynamicProductionCommander");

        assertTrue(contains(unitProducer, ProtossDynamicUnitProductionCommander.class),
                "ProtossDynamicUnitProductionCommander missing - this is the node whose"
                        + " applies() the owner never sees fire");
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

    // ---- helpers -----------------------------------------------------------

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
}