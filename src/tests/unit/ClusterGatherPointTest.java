package tests.unit;

import atlantis.combat.cluster.ClusterRally;
import atlantis.combat.cluster.FightOrFlight;
import atlantis.map.position.APosition;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertSame;
import static tests.unit.AbstractTestWithUnits.fake;

/**
 * The cluster marches and retreats as one (owner 2026-10-19: both strung
 * out in a line on Wine). With no enemy to engage, a straggler turns back
 * to the leader instead of racing the anchor; the leader (or a lone unit)
 * takes the anchor. Public seam on BattleCommander, pinned without
 * running the frame.
 */
@Tag("combat")
public class ClusterGatherPointTest extends AbstractTestWithWorld {

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @Test
    public void stragglerRejoinsLeader() {
        FakeUnit leader = fake(AUnitType.Protoss_Zealot, 30, 20);
        FakeUnit straggler = fake(AUnitType.Protoss_Zealot, 40, 20);
        APosition anchor = APosition.create(90, 20);

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            leader, straggler
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            assertSame(leader,
                ClusterRally.gatherPointFor(straggler, leader, anchor, FightOrFlight.Tactic.FIGHT),
                "10 tiles behind: turn back to the leader, not the anchor");
        });
    }

    @Test
    public void leaderAndCloseUnitsTakeAnchor() {
        FakeUnit leader = fake(AUnitType.Protoss_Zealot, 30, 20);
        FakeUnit close = fake(AUnitType.Protoss_Zealot, 31, 20);
        APosition anchor = APosition.create(90, 20);

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            leader, close
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            assertSame(anchor,
                ClusterRally.gatherPointFor(leader, leader, anchor, FightOrFlight.Tactic.FIGHT),
                "the leader sets the pace to the anchor");
            assertSame(anchor,
                ClusterRally.gatherPointFor(close, leader, anchor, FightOrFlight.Tactic.FIGHT),
                "inside 2 tiles: keep marching, no jitter");
        });
    }

    @Test
    public void noLeaderMeansAnchor() {
        FakeUnit lone = fake(AUnitType.Protoss_Zealot, 30, 20);
        APosition anchor = APosition.create(90, 20);

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            lone
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            assertSame(anchor,
                ClusterRally.gatherPointFor(lone, null, anchor, FightOrFlight.Tactic.FIGHT),
                "no leader: the anchor is the only answer");
        });
    }

    @Test
    public void fleeSendsEveryoneHomeNeverToTheLeader() {
        FakeUnit leader = fake(AUnitType.Protoss_Zealot, 30, 20);
        FakeUnit straggler = fake(AUnitType.Protoss_Zealot, 40, 20);
        APosition anchor = APosition.create(90, 20);

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            leader, straggler
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            assertSame(anchor,
                ClusterRally.gatherPointFor(straggler, leader, anchor, FightOrFlight.Tactic.FLEE),
                "owner 2026-10-23: rallying stragglers onto a forward-kiting leader marches them back into the fight");
        });
    }
}
