package tests.unit;

import atlantis.battle.cluster.Alpha;
import atlantis.battle.BattleCommander;
import atlantis.battle.cluster.ClusterTactic;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Under two tiles from the main base the cluster stands instead of
 * fleeing (owner: retreating into the mineral line donates the base).
 * Individual kiting inside act() is untouched - only the cluster
 * tactic is gated.
 */
@Tag("combat")
public class ClusterHoldsHomeGroundTest extends AbstractTestWithWorld {

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }

    @AfterEach
    public void cleanCluster() {
        for (AUnit u : Alpha.get().selection().list().toArray(new AUnit[0])) {
            Alpha.get().removeUnit(u);
        }
    }

    @Test
    public void clusterOnTheBaseDoesNotFlee() {
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 21, 21);

        world(1, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            zealot
        ), fakeEnemies(), () -> {
            Alpha.get().addUnit(zealot);

            assertTrue(ClusterTactic.holdsHomeGround(),
                "cluster ~1.4 tiles from the main base holds: " + Alpha.get().center());

            Alpha.get().removeUnit(zealot);
            assertFalse(ClusterTactic.holdsHomeGround(),
                "no cluster, no gate");
        });
    }

    @Test
    public void clusterFarFromBaseMayFlee() {
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 40, 21);

        world(1, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            zealot
        ), fakeEnemies(), () -> {
            Alpha.get().addUnit(zealot);

            assertFalse(ClusterTactic.holdsHomeGround(),
                "20 tiles out the gate stays open: " + Alpha.get().center());

            Alpha.get().removeUnit(zealot);
        });
    }
}
