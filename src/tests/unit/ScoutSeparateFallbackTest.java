package tests.unit;

import atlantis.combat.escape.EscapeScoredStep;
import atlantis.combat.scout.ScoutSeparateFromCloseEnemies;
import atlantis.game.A;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeOrderSink;
import tests.fakes.FakeUnit;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * NEXT #187 remainder: Separate's fallback issued unscored first-success
 * steps ({@code MoveAway.from}) while the running manager and the hurt
 * branch already score (8-tile pick with building clearance). A scout
 * boxed by buildings with a zealot on its tail must not be stepped
 * onto a building's shirt - the scored pick vetoes hugging dests, the
 * first-success pick validates them.
 *
 * <p>Sim repro: healthy scout at (30,20), zealot chaser 2 tiles west,
 * enemy Gateway 4 tiles east. Straight back hugs the Gateway's edge;
 * the scored pick must clear it by 1.5 tiles.
 */
@Tag("combat")
public class ScoutSeparateFallbackTest extends WorldStubForTests {

    private static final Pattern TILE = Pattern.compile("\\[\\s*(\\d+(?:\\.\\d+)?)\\s*,\\s*(\\d+(?:\\.\\d+)?)\\s*\\]");

    private static List<double[]> moveDests(FakeUnit scout) {
        List<double[]> out = new ArrayList<>();
        for (FakeOrderSink.Record r : ((FakeOrderSink) scout.orderSink()).orders()) {
            if (!r.operation.equals("move") && !r.operation.equals("patrol")) continue;
            Matcher m = TILE.matcher(r.detail);
            if (m.find())
                out.add(new double[]{Double.parseDouble(m.group(1)) / 32, Double.parseDouble(m.group(2)) / 32});
        }
        return out;
    }

    @Test
    public void fallbackKeepsClearanceOffEnemyBuildings() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 7, 44);
        FakeUnit scout = fake(AUnitType.Protoss_Probe, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 28, 20).setEnemy();
        FakeUnit gateway = fake(AUnitType.Protoss_Gateway, 34, 20).setEnemy();

        world(60, new FakeUnit[]{nexus, scout}, new FakeUnit[]{zealot, gateway}, () -> {
            if (A.now() != 10) return;

            ScoutSeparateFromCloseEnemies separate = new ScoutSeparateFromCloseEnemies(scout);
            assertTrue(separate.applies(), "adjacent zealot chases the scout: " + scout.managerLogs());
            assertTrue(separate.separateFallback(zealot), "fallback issues a step at all");

            List<double[]> dests = moveDests(scout);
            assertFalse(dests.isEmpty(), "a move order was issued: " + ((FakeOrderSink) scout.orderSink()).orders());

            double half = gateway.type().bodyRadiusPx() / 32.0;
            for (double[] d : dests) {
                double edge = Math.hypot(d[0] - 34, d[1] - 20) - half;
                assertTrue(edge >= 1.5,
                    "no step onto the enemy Gateway's shirt: dest (" + d[0] + "," + d[1]
                        + ") edge " + edge + " logs=" + scout.managerLogs());
            }
        });
    }

    @Test
    public void scoredBlockersIncludeEnemyBuildings() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 7, 44);
        FakeUnit scout = fake(AUnitType.Protoss_Probe, 30, 20);
        FakeUnit gateway = fake(AUnitType.Protoss_Gateway, 34, 20).setEnemy();

        world(60, new FakeUnit[]{nexus, scout}, new FakeUnit[]{gateway}, () -> {
            if (A.now() != 10) return;

            List<HasPosition> blockers = EscapeScoredStep.blockers(scout);
            boolean seen = false;
            for (HasPosition h : blockers)
                if (h == gateway) {
                    seen = true;
                    break;
                }
            assertTrue(seen, "enemy buildings box the scout exactly like ours do");
        });
    }
}
