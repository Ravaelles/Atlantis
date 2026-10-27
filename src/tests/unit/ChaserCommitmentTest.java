package tests.unit;

import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * NEXT #186: the stop-point rule waits only for a COMMITTED chaser - an
 * enemy that has us as its order target, is moving, and faces us. These
 * pins cover the reads the rule will consume, not the rule itself:
 * the order-target stub (production reads BWAPI, the stub used to
 * answer null always), the facing window, and the fog-equivalent
 * (no info reads as no commitment, never as commitment).
 */
@Tag("combat")
public class ChaserCommitmentTest extends WorldStubForTests {

    @Test
    public void orderTargetReadExposesTheChasersCommitment() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 32, 20);
        zealot.setEnemy();

        world(1, fakeOurs(dragoon), fakeEnemies(zealot), () -> {
            zealot.orderTarget = dragoon;

            assertSame(dragoon, zealot.orderTarget(),
                "a chaser ordered onto us reads back as committed");
        });
    }

    @Test
    public void unsetOrderTargetReadsAsNoCommitment() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 32, 20);
        zealot.setEnemy();

        world(1, fakeOurs(dragoon), fakeEnemies(zealot), () -> {
            assertNull(zealot.orderTarget(),
                "no info (fog, unset) reads as no commitment - the future rule must not wait on it");
        });
    }

    @Test
    public void committedChaserFacesUsAndMoves() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 32, 20);
        zealot.setEnemy();

        world(1, fakeOurs(dragoon), fakeEnemies(zealot), () -> {
            zealot.orderTarget = dragoon;
            zealot.setAngle(Math.PI);
            zealot.lastCommand = "Move";

            assertSame(dragoon, zealot.orderTarget(), "ordered onto us");
            assertTrue(zealot.isMoving(), "closing, not holding");
            assertTrue(dragoon.isOtherFacingThisUnit(zealot),
                "facing us inside the 1.1 rad window - the full commitment triple reads committed");
        });
    }
}
