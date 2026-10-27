package tests.unit;

import atlantis.information.enemy.UnitsArchive;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The "lost" half of the end-of-game report - `Zealot=3 (1 lost)`.
 *
 * <p>
 * The number has to be a count of destruction EVENTS, not `built - alive`.
 * Those differ
 * whenever a unit is lost and rebuilt, and the difference is exactly the case a
 * player cares
 * about: three Zealots built, one killed, one rebuilt to replace it ends with
 * the same three
 * on the board and two built later - so a subtraction would report "no losses"
 * while a Zealot
 * was in fact lost. {@code UnitsArchive} counts the events, which is why the
 * report asks it.
 * </p>
 */
@Tag("infra")
public class OurLostUnitsReportTest extends tests.acceptance.WorldStubForTests {

    private static final AUnitType ZEALOT = AUnitType.Protoss_Zealot;

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    /**
     * The archive is a static accumulator with no per-test reset in the shared harness, so the
     * counts leak from one test into the next - which is how the first run of this class failed
     * (`expected 0 but was 3`).
     *
     * <p>
     * The reset has to happen INSIDE `world(...)`, not in `@BeforeEach`: the surrounding test
     * fixture clears its own state while entering the world, so anything reset before that is
     * cleared again afterwards and the leak survives. That difference cost a test cycle - the
     * annotation looked right and had no effect. The archive's own `reset()` is used rather than
     * a local helper, so the test clears exactly the state the game clears between games.
     * </p>
     */
    private void resetArchive() {
        UnitsArchive.reset();
    }

    /**
     * Nothing lost yet: the report shows a plain count rather than `(0 lost)`.
     */
    @Test
    public void nothingLostIsReportedAsZero() {
        FakeUnit zealot = fake(ZEALOT, 50, 50);

        world(1, fakeOurs(zealot), fakeEnemies(), () -> {
            resetArchive();

            assertEquals(
                0, UnitsArchive.ourLost(ZEALOT),
                "a fresh archive has lost nothing - the report omits the note for this");
        });
    }

    /**
     * The counting itself, through the real entry point: {@code ourUnitDestroyed}
     * is what the
     * game listener calls, so a test that incremented anything else would not be
     * testing the
     * path the report reads.
     */
    @Test
    public void eachDestroyedUnitOfOurTypeIsCounted() {
        FakeUnit zealot = fake(ZEALOT, 50, 50);

        world(1, fakeOurs(zealot), fakeEnemies(), () -> {
            resetArchive();

            UnitsArchive.ourUnitDestroyed(zealot);
            UnitsArchive.ourUnitDestroyed(zealot);
            UnitsArchive.ourUnitDestroyed(zealot);

            // Asserted INSIDE the world: the fixture clears its own state on the way in, so a
            // count checked after it would be read from a reset archive.
            assertEquals(
                3, UnitsArchive.ourLost(ZEALOT),
                "three destruction events on the same type must count as three losses");
        });
    }

    /**
     * Losses are per TYPE: one type's deaths must not appear in another's count,
     * which is the
     * whole reason the report can print a per-type line.
     */
    @Test
    public void lossesAreCountedPerType() {
        FakeUnit zealot = fake(ZEALOT, 50, 50);
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 52, 50);

        world(1, fakeOurs(zealot, dragoon), fakeEnemies(), () -> {
            resetArchive();

            UnitsArchive.ourUnitDestroyed(zealot);
            UnitsArchive.ourUnitDestroyed(dragoon);
            UnitsArchive.ourUnitDestroyed(dragoon);

            assertEquals(1, UnitsArchive.ourLost(ZEALOT), "one Zealot destroyed");
            assertEquals(
                2, UnitsArchive.ourLost(AUnitType.Protoss_Dragoon),
                "two Dragoons destroyed");
            assertEquals(
                0, UnitsArchive.ourLost(AUnitType.Protoss_Pylon),
                "and a type with no losses reports none, rather than another type's count");
        });
    }

    /**
     * A null type is answered rather than thrown on - the report must not crash on
     * exit.
     */
    @Test
    public void aNullTypeHasNoLosses() {
        FakeUnit zealot = fake(ZEALOT, 50, 50);

        world(1, fakeOurs(zealot), fakeEnemies(), () -> {
            resetArchive();

            assertEquals(
                0, UnitsArchive.ourLost(null),
                "a null type loses nothing, and must not throw"
            );
        });
    }
}
