package tests.unit;

import atlantis.combat.targeting.AcquireTarget;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Stability pin for the laddder's top (NEXT #184): against a mixed pack
 * (wounded + healthy swarm in range) acquire() holds one target
 * instead of churning. Measured 0 switches / 119 frames. The open
 * question whether keepable should FINISH sub-35hp swarm is answered
 * the same way: there is no churn to fix, and the focus bans stay for
 * anti-overkill pack discipline - finishing wounded swarm is a DPS
 * optimization, not a defect, and needs its own efficacy proof first.
 */
@Tag("combat")
public class KeepableStabilityTest extends WorldStubForTests {

    @Test
    public void noChurnAgainstMixedPack() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit d = fake(AUnitType.Protoss_Dragoon, 30, 20);
        d.hp = 120;
        d.cooldown = 0;
        FakeUnit w = fake(AUnitType.Zerg_Zergling, 31, 20);
        w.setEnemy();
        w.hp = 10;
        FakeUnit h1 = fake(AUnitType.Zerg_Zergling, 32, 20);
        h1.setEnemy();
        FakeUnit h2 = fake(AUnitType.Zerg_Zergling, 29, 21);
        h2.setEnemy();
        final StringBuilder ids = new StringBuilder();

        world(120, fakeOurs(nexus, d), fakeEnemies(w, h1, h2), () -> {
            int now = atlantis.game.A.now();
            if (now < 1) return;

            AUnit t = AcquireTarget.acquire(d);
            if (t != null) ids.append(t.id()).append(",");
            if (now == 119) {
                String[] parts = ids.toString().split(",");
                int switches = 0;
                for (int i = 1; i < parts.length; i++) {
                    if (!parts[i].equals(parts[i - 1])) switches++;
                }
                assertTrue(switches <= 2,
                    "the ladder holds its target (rearm discipline bounds re-issues): " + switches);
            }
        });
    }
}
