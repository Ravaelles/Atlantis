package tests.unit;

import atlantis.debug.tweaker.DuelHitsMinutesScenario;
import atlantis.debug.tweaker.ParamTweakerFactory;
import atlantis.debug.tweaker.TunerObjective;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * NEXT #160, step 6: the duel scenario contract - map, replicates,
 * fitness delegation, and a bounds table that matches the factory
 * registration (bounds live twice, here and in the lambdas, so this
 * pins the duplication instead of hoping).
 */
@Tag("infra")
public class TuningScenarioTest {

    private final DuelHitsMinutesScenario scenario = new DuelHitsMinutesScenario();

    @Test
    public void duelContract() {
        assertEquals("1Drag_v_1Zeal.scm", scenario.mapFile());
        assertEquals(3, scenario.replicates(), "K-of-N spirit: duels are noisy");
        assertTrue(scenario.frozenParams().isEmpty(), "optimize everything, hide no interaction");
    }

    @Test
    public void fitnessIsHitsTimesMinutes() {
        TunerObjective.Row row = new TunerObjective.Row("game-1", true, 14, 30);

        assertEquals(7.0, scenario.fitness(row), 0.0001);
    }

    @Test
    public void boundsMatchFactoryRegistration() {
        Map<String, double[]> bounds = scenario.paramBounds();

        assertEquals(26, bounds.size(), "one entry per registered param, no more no less");
        assertBounds(bounds, "KiteStepTiles", 1.5, 3.0);
        assertBounds(bounds, "DragoonLowHp", 30, 60);
        assertBounds(bounds, "DragoonPinchMeleeTiles", 1.5, 2.5);
        for (Map.Entry<String, double[]> bound : bounds.entrySet()) {
            assertTrue(bound.getValue().length == 2 && bound.getValue()[0] < bound.getValue()[1],
                "usable range: " + bound.getKey());
        }
        for (String intParam : ParamTweakerFactory.integerParams()) {
            assertTrue(bounds.containsKey(intParam), "int param is a real param: " + intParam);
        }
    }

    private static void assertBounds(Map<String, double[]> bounds, String name, double min, double max) {
        assertTrue(bounds.containsKey(name), "registered: " + name);
        assertEquals(min, bounds.get(name)[0], 0.0001, name);
        assertEquals(max, bounds.get(name)[1], 0.0001, name);
    }
}
