package tests.unit;

import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pinning the repair-pair filters before their verbatim move into
 * {@code SelectionMembership} (NEXT #10): repairing vs notRepairing
 * partition the same selection.
 */
@Tag("infra")
public class SelectionRepairTest extends WorldStubForTests {

    @Test
    public void repairPairPartitions() {
        FakeUnit w1 = fake(AUnitType.Protoss_Probe, 10, 10);
        FakeUnit w2 = fake(AUnitType.Protoss_Probe, 12, 10);

        world(1, fakeOurs(w1, w2), fakeEnemies(), () -> {
            Selection ours = Select.our();
            assertEquals(2, ours.size());

            int repairing = ours.repairing().size();
            int notRepairing = ours.notRepairing().size();
            assertEquals(2, repairing + notRepairing, "every unit lands on exactly one side");
            assertEquals(0, repairing, "nobody repairs by default");
            assertEquals(2, notRepairing);
        });
    }
}
