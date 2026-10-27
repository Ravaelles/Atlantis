package tests.unit;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.map.region.MainRegion;
import atlantis.production.constructions.position.conditions.IsProbablyInAnotherRegion;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Early pylons stay home (owner, lost game vs early aggression: the
 * default-sited 2nd pylon walked to the natural, died there with the
 * game). Until 60 supply a pylon from the default (null-nearTo) search -
 * which is anchored at the main - cannot land outside the main region,
 * unless the strategy is an expansion. An explicit request elsewhere
 * keeps working: the rule reads the search origin, not just the site.
 */
@Tag("placement")
public class PylonHomeRegionTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    /**
     * The stub world's base locations come from the harness (the fixed
     * TauCross-like map); only the cached main region is reset here so
     * one world never reads another's answer.
     */
    @BeforeEach
    public void clearMainRegion() {
        MainRegion.clearForTests();
    }

    @AfterEach
    public void releaseMainRegion() {
        MainRegion.clearForTests();
    }

    @Test
    public void defaultSearchRejectsNaturalBelow60() {
        FakeUnit main = fake(AUnitType.Protoss_Nexus, 9, 46);
        FakeUnit natural = fake(AUnitType.Protoss_Nexus, 16, 14);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 7, 47);
        probe.hp = 40;

        world(20, fakeOurs(main, natural, probe), fakeEnemies(), () -> {
            if (A.now() != 12) return;

            AUnit builder = Select.ourWorkers().first();
            HasPosition origin = Select.mainOrAnyBuilding();
            APosition site = APosition.create(17, 13);

            boolean verdict = IsProbablyInAnotherRegion.defaultPylonMustStayHome(
                builder, AUnitType.Protoss_Pylon, site, origin);
            assertTrue(
                verdict,
                "a default-anchored pylon search must refuse the natural "
                    + "below 60 supply - that walk lost the game");
        });
    }

    @Test
    public void defaultSearchKeepsMainSiteBelow60() {
        FakeUnit main = fake(AUnitType.Protoss_Nexus, 9, 46);
        FakeUnit natural = fake(AUnitType.Protoss_Nexus, 16, 14);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 7, 47);
        probe.hp = 40;

        world(20, fakeOurs(main, natural, probe), fakeEnemies(), () -> {
            if (A.now() != 12) return;

            AUnit builder = Select.ourWorkers().first();
            HasPosition origin = Select.mainOrAnyBuilding();
            APosition site = APosition.create(10, 45);

            assertFalse(
                IsProbablyInAnotherRegion.defaultPylonMustStayHome(
                    builder, AUnitType.Protoss_Pylon, site, origin),
                "the home half of the rule: a main-region site from a "
                    + "main-anchored search is untouched");
        });
    }

    @Test
    public void explicitNaturalRequestStillWorksBelow60() {
        FakeUnit main = fake(AUnitType.Protoss_Nexus, 9, 46);
        FakeUnit natural = fake(AUnitType.Protoss_Nexus, 16, 14);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 7, 47);
        probe.hp = 40;

        world(20, fakeOurs(main, natural, probe), fakeEnemies(), () -> {
            if (A.now() != 12) return;

            AUnit builder = Select.ourWorkers().first();
            HasPosition origin = natural;
            APosition site = APosition.create(17, 13);

            assertFalse(
                IsProbablyInAnotherRegion.defaultPylonMustStayHome(
                    builder, AUnitType.Protoss_Pylon, site, origin),
                "an explicitly requested natural pylon is not the default "
                    + "search - the rule owns null-nearTo only");
        });
    }

    @Test
    public void allowanceReturnsAt60Supply() {
        FakeUnit main = fake(AUnitType.Protoss_Nexus, 9, 46);
        FakeUnit natural = fake(AUnitType.Protoss_Nexus, 16, 14);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 7, 47);
        probe.hp = 40;

        initSupply(40, 61);
        world(20, fakeOurs(main, natural, probe), fakeEnemies(), () -> {
            if (A.now() != 12) return;

            AUnit builder = Select.ourWorkers().first();
            HasPosition origin = Select.mainOrAnyBuilding();
            APosition site = APosition.create(17, 13);

            assertFalse(
                IsProbablyInAnotherRegion.differentRegion(
                    builder, AUnitType.Protoss_Pylon, site, origin),
                "at 60+ supply the home rule lifts, like the 60 line above it");
        });
    }
}
