package tests.acceptance;

import atlantis.combat.micro.terran.TerranGhost;
import atlantis.game.A;
import org.junit.Test;
import tests.unit.FakeUnit;

import static atlantis.units.AUnitType.*;
import static org.junit.Assert.assertEquals;

public class UnitsToTargetsWithCacheTest  extends AbstractTestFakingGame {

    private FakeUnit ghost1;
    private FakeUnit ghost2;
    private FakeUnit ghost3;
    private FakeUnit ghost4;
    private FakeUnit ghost5;
    private FakeUnit dragoon;
    private FakeUnit shuttle1;
    private FakeUnit shuttle2;

    @Test
    public void tetsItReturnsTargetsAcquiredInLastNFrames() {
        createWorld(2, () -> {
//            System.out.println("\n### now = " + A.now());

            // === 1 ===========================================

            if (A.now() == 1) {
                assertEquals(0, TerranGhost.lockdownTargets.targetsAcquiredInLast(1).size());

                TerranGhost.lockdownTargets.addTarget(shuttle1, ghost1);
                TerranGhost.lockdownTargets.addTarget(shuttle2, ghost2);
                TerranGhost.lockdownTargets.addTarget(shuttle2, ghost3);
            }

            // === 2 ===========================================

            else if (A.now() == 2) {
                assertEquals(0, TerranGhost.lockdownTargets.targetsAcquiredInLast(0).size());
                assertEquals(3, TerranGhost.lockdownTargets.targetsAcquiredInLast(1).size());

                TerranGhost.lockdownTargets.clear();
            }
        });
    }

    // =========================================================

    protected FakeUnit[] generateOur() {
        return fakeOurs(
            ghost1 = fake(Terran_Ghost, 10),
            ghost2 = fake(Terran_Ghost, 11),
            ghost3 = fake(Terran_Ghost, 12),
            ghost4 = fake(Terran_Ghost, 13),
            ghost5 = fake(Terran_Ghost, 14)
        );
    }

    protected FakeUnit[] generateEnemies() {
        return fakeEnemies(
            fakeEnemy(Protoss_Zealot, 11),
            dragoon = fakeEnemy(Protoss_Dragoon, 12),
            shuttle1 = fakeEnemy(Protoss_Shuttle, 13),
            shuttle2 = fakeEnemy(Protoss_Shuttle, 14)
        );
    }

}

