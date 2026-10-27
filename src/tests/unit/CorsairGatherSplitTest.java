package tests.unit;

import atlantis.battle.cluster.Alpha;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * NEXT #201 follow-up: the Alpha.gather split reads the CorsairCluster
 * rule - a solo Corsair (no threatening air) stays out of the main
 * cluster and hunts on its own (CorsairHunt owns it, so it never idles);
 * with threatening air it merges for Dragoon cover.
 */
@Tag("combat")
public class CorsairGatherSplitTest extends WorldStubForTests {

    @AfterEach
    public void cleanRoster() {
        for (AUnit u : Alpha.get().selection().list().toArray(new AUnit[0])) {
            Alpha.get().removeUnit(u);
        }
    }

    private void drainRoster() {
        for (AUnit u : Alpha.get().selection().list().toArray(new AUnit[0])) {
            Alpha.get().removeUnit(u);
        }
    }

    @Test
    public void soloCorsairStaysOutOfAlpha() {
        FakeUnit corsair = fake(AUnitType.Protoss_Corsair, 20, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 22, 20);
        FakeUnit ling = fake(AUnitType.Zerg_Zergling, 40, 20);
        ling.setEnemy();

        world(3, fakeOurs(corsair, zealot), fakeEnemies(ling), () -> {
            if (atlantis.game.A.now() != 2) return;

            drainRoster();

            Alpha.gather();

            assertTrue(Alpha.get().selection().ofType(AUnitType.Protoss_Zealot).notEmpty(),
                "the swarm still gathers without its hunter");
            assertTrue(Alpha.get().selection().ofType(AUnitType.Protoss_Corsair).isEmpty(),
                "no threatening air: the Corsair stays solo instead of "
                    + "trailing the army (NEXT #201)");
        });
    }

    @Test
    public void corsairMergesWhenAirThreatens() {
        FakeUnit corsair = fake(AUnitType.Protoss_Corsair, 20, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 22, 20);
        FakeUnit muta = fake(AUnitType.Zerg_Mutalisk, 40, 20);
        muta.setEnemy();

        world(3, fakeOurs(corsair, zealot), fakeEnemies(muta), () -> {
            if (atlantis.game.A.now() != 2) return;

            drainRoster();

            Alpha.gather();

            assertTrue(Alpha.get().selection().ofType(AUnitType.Protoss_Corsair).notEmpty(),
                "a Mutalisk in the sky merges the Corsair for Dragoon cover");
        });
    }
}
