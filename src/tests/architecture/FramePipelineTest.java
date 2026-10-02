package tests.architecture;

import atlantis.architecture.Commander;
import atlantis.application.FramePipeline;
import atlantis.game.AtlantisGameCommander;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Stage C (see _AI/REVIEW.md §16): pins the top-level frame pipeline order.
 *
 * <p>Order matters: it is the execution order of every game frame. If this test
 * fails, the order was changed — make sure that is intentional and reviewed.</p>
 */
public class FramePipelineTest {

    private static final List<String> EXPECTED_ORDER = List.of(
        "BulletsCommander",
        "UnitStateCommander",
        "SpecialActionsCommander",
        "ScoutCommander",
        "WorkerCommander",
        "CombatCommander",
        "ProductionCommander",
        "BuildingsCommander",
        "ConstructionsCommander",
        "SpecialCommander",
        "StrategyCommander",
        "EnemyUnitsCommander",
        "CameraCommander",
        "MapSpecificCommander",
        "PainterCommander",
        "DebugCommander"
    );

    @Test
    void pipelineOrderIsPinned() {
        List<String> actual = Arrays.stream(FramePipeline.steps())
            .map(Class::getSimpleName)
            .collect(Collectors.toList());

        assertEquals(EXPECTED_ORDER, actual,
            "Top-level frame pipeline order changed. This changes bot behaviour; "
                + "update intentionally and review (REVIEW §16 Stage C).");
    }

    @Test
    void everyStepIsACommander() {
        for (Class<? extends Commander> step : FramePipeline.steps()) {
            assertTrue(Commander.class.isAssignableFrom(step),
                step.getName() + " is not a Commander");
        }
    }

    @Test
    void gameCommanderDelegatesToPipeline() {
        assertArrayEquals(FramePipeline.steps(), AtlantisGameCommander.topLevelSubcommanders(),
            "AtlantisGameCommander must not keep its own copy of the pipeline order.");
    }
}
