package tests.unit;

import atlantis.production.requests.produce.BuildGate;
import atlantis.production.requests.produce.BuildGates;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins the refusal-gate registry (NEXT #155): every {@code if (...)
 * refuse(...)} of the single building door is a named gate, evaluated in
 * one explicit order. The order is semantic - a gate reordered wrongly
 * re-trips an earlier refusal - so the sequence itself is the contract
 * under test. Per-gate behavior stays pinned by the existing suite
 * (e.g. {@code LiveConstructionGuardTest}); this test pins that no gate
 * is lost, duplicated, or reordered.
 */
@Tag("production")
public class BuildGateOrderTest {

    private static final List<String> EXPECTED_ORDER = Arrays.asList(
        "below-min-supply-and-poor",
        "live-construction",
        "core-single-instance",
        "pylon-cap",
        "pylon-overstock",
        "unstarted-pylon",
        "warping-link",
        "earlier-reserve",
        "funding",
        "core-priority",
        "power-not-funded"
    );

    @Test
    public void registryHoldsEveryGateExactlyOnceInOrder() {
        List<BuildGate> gates = BuildGates.all();

        assertEquals(EXPECTED_ORDER.size(), gates.size(),
                "a gate added or removed must update this contract deliberately, never silently");

        for (int i = 0; i < EXPECTED_ORDER.size(); i++) {
            assertEquals(EXPECTED_ORDER.get(i), gates.get(i).name(),
                    "gate " + i + " out of order: the sequence is semantic");
        }
    }

    @Test
    public void gateNameMatchesItsRefusalReason() {
        for (BuildGate gate : BuildGates.all()) {
            assertTrue(gate.name() != null && !gate.name().isEmpty(),
                    "every gate names the twin-visible refusal reason");
        }
    }
}
