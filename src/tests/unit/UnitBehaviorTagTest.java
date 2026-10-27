package tests.unit;

import atlantis.cherryvis.ACherryVis;
import atlantis.cherryvis.simple.ACherryVisLogger;
import atlantis.config.env.Env;
import atlantis.units.AUnitType;
import atlantis.units.workers.gather.GatherResources;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * NEXT #214c: the unit log names the active behavior, not just the
 * manager class. A lone Zealot marching 15 tiles ahead of the line is
 * indistinguishable from a scout without it - the "ClusterFlee" half
 * of the "ClusterFlee/RetreatFromLostFight" row is the evidence.
 */
@Tag("infra")
public class UnitBehaviorTagTest extends WorldStubForTests {

    @Override
    public bwapi.Race initRace() {
        return bwapi.Race.Protoss;
    }

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }

    @BeforeEach
    public void enableTrace() {
        Env.setCherryVisEnabledForTest(true);
        unitLogger().clearForTests();
    }

    @AfterEach
    public void cleanTrace() {
        unitLogger().clearForTests();
        Env.setCherryVisEnabledForTest(false);
    }

    private static atlantis.cherryvis.simple.ACherryVisUnitLogger unitLogger() {
        return ((ACherryVisLogger) ACherryVis.logger()).getUnitLogger();
    }

    @Test
    public void behaviorTagRidesTheManagerLine() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 30, 20);

        world(20, fakeOurs(nexus, probe), new FakeUnit[] {}, () -> {
            if (atlantis.game.A.now() != 5) return;

            probe.setManagerUsed(new GatherResources(probe), "ClusterFlee");

            String rows = atlantis.cherryvis.simple.ACherryVis_UnitLogs.buildUnitLogs(unitLogger());
            assertTrue(rows.contains("-ClusterFlee/GatherResources@"),
                "the behavior tag rides next to the manager class: " + rows);
        });
    }

    @Test
    public void managerWithoutBehaviorKeepsOldShape() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 30, 20);

        world(20, fakeOurs(nexus, probe), new FakeUnit[] {}, () -> {
            if (atlantis.game.A.now() != 5) return;

            probe.setManagerUsed(new GatherResources(probe), null);

            String rows = atlantis.cherryvis.simple.ACherryVis_UnitLogs.buildUnitLogs(unitLogger());
            assertTrue(rows.contains("-GatherResources@"),
                "no behavior tag, no prefix - the old shape stands: " + rows);
        });
    }
}
