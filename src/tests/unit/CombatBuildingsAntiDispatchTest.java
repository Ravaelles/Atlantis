package tests.unit;

import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Pin-first for the NEXT #10 dispatch slice: combatBuildingsAnti routes
 * ground units to the land set and air units to the air set. The slice
 * moves the one-statement dispatcher verbatim afterwards.
 */
@Tag("unit")
public class CombatBuildingsAntiDispatchTest extends WorldStubForTests {

    @Test
    public void groundUnitReadsTheLandSet() {
        FakeUnit cannon = fake(AUnitType.Protoss_Photon_Cannon, 20, 20);
        FakeUnit sunken = fake(AUnitType.Zerg_Sunken_Colony, 22, 20);
        FakeUnit marine = fake(AUnitType.Terran_Marine, 24, 20);

        world(1, fakeOurs(cannon, sunken, marine), fakeEnemies(), () -> {
            assertEquals(2, atlantis.units.select.Select.ourBuildings()
                .combatBuildingsAnti(marine).count(), "ground reads land");
        });
    }

    @Test
    public void airUnitReadsTheAirSet() {
        FakeUnit cannon = fake(AUnitType.Protoss_Photon_Cannon, 20, 20);
        FakeUnit sunken = fake(AUnitType.Zerg_Sunken_Colony, 22, 20);
        FakeUnit wraith = fake(AUnitType.Terran_Wraith, 24, 20);

        world(1, fakeOurs(cannon, sunken, wraith), fakeEnemies(), () -> {
            assertEquals(1, atlantis.units.select.Select.ourBuildings()
                .combatBuildingsAnti(wraith).count(), "air reads air (cannon only)");
        });
    }
}
