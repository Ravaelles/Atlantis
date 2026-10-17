package tests.unit;

import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Pinning the membership-filter family before its extraction out of
 * {@code Selection} (NEXT #10, slice 2): one predicate shape each, so the
 * verbatim move that follows cannot silently change semantics.
 *
 * <p>Written against the current implementation; the extraction keeps
 * every assertion green without touching them.</p>
 */
public class SelectionMembershipTest extends WorldStubForTests {

    @Test
    public void typeFilters() {
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 10, 10);
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 12, 10);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 14, 10);

        world(1, fakeOurs(zealot, dragoon, probe), fakeEnemies(), () -> {
            Selection ours = Select.our();
            assertEquals(3, ours.size());

            assertEquals(1, ours.zealots().size());
            assertEquals(1, ours.dragoons().size());
            assertEquals(1, ours.workers().size());
            assertEquals(0, ours.marines().size());
            assertEquals(3, ours.groundUnits().size());
            assertEquals(3, ours.nonBuildings().size());
        });
    }

    @Test
    public void kindFilters() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit pylon = fake(AUnitType.Protoss_Pylon, 20, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 30, 30);
        FakeUnit observer = fake(AUnitType.Protoss_Observer, 40, 40);

        world(1, fakeOurs(nexus, pylon, zealot, observer), fakeEnemies(), () -> {
            Selection ours = Select.our();

            assertEquals(2, ours.buildings().size());
            assertEquals(2, ours.nonBuildings().size());
            assertEquals(1, ours.air().size());
            assertEquals(3, ours.groundUnits().size());
            assertEquals(4, ours.notScout().size());
        });
    }

    @Test
    public void stateFilters() {
        FakeUnit healthy = fake(AUnitType.Protoss_Zealot, 10, 10);
        FakeUnit wounded = fake(AUnitType.Protoss_Zealot, 12, 10).setHp(20);
        FakeUnit burrowed = fake(AUnitType.Zerg_Lurker, 20, 20).setBurrowed(true);
        FakeUnit cloaked = fake(AUnitType.Protoss_Dark_Templar, 30, 30).setCloaked(true).setDetected(true);

        world(1, fakeOurs(healthy, wounded, burrowed, cloaked), fakeEnemies(), () -> {
            Selection ours = Select.our();

            assertEquals(1, ours.wounded().size());
            assertEquals(1, ours.burrowed().size());
            assertEquals(1, ours.dts().size());
            assertEquals(1, ours.lurkers().size());
        });
    }

    @Test
    public void staticAndBuildingKindFilters() {
        FakeUnit bunker = fake(AUnitType.Terran_Bunker, 10, 10);
        FakeUnit cannon = fake(AUnitType.Protoss_Photon_Cannon, 20, 20);
        FakeUnit forge = fake(AUnitType.Protoss_Forge, 30, 30);
        FakeUnit marine = fake(AUnitType.Terran_Marine, 40, 40);

        world(1, fakeOurs(bunker, cannon, forge, marine), fakeEnemies(), () -> {
            Selection ours = Select.our();

            assertEquals(1, ours.bunkers().size());
            assertEquals(1, ours.cannons().size());
            assertEquals(1, ours.forge().size());
            assertEquals(1, ours.marines().size());
            assertEquals(4, ours.onlyCompleted().size());
            assertEquals(0, ours.notCompleted().size());
        });
    }
}
