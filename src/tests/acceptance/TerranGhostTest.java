package tests.acceptance;

import atlantis.combat.micro.terran.TerranGhost;
import org.junit.Test;
import tests.unit.FakeUnit;

import static atlantis.units.AUnitType.*;
import static junit.framework.TestCase.assertEquals;

public class TerranGhostTest extends AbstractTestFakingGame {

    protected FakeUnit ghost1;
    protected FakeUnit ghost2;
    protected FakeUnit ghost3;
    protected FakeUnit ghost4;
    protected FakeUnit dragoon;
    protected FakeUnit scout;
    protected FakeUnit reaver;
    protected FakeUnit wraith1;
    protected FakeUnit wraith2;

    @Test
    public void ghostIsTargetingMechanicalUnitsForLockdown() {
        createWorld(1, () -> {
                TerranGhost manager1 = new TerranGhost();
                TerranGhost manager2 = new TerranGhost();
                TerranGhost manager3 = new TerranGhost();
                TerranGhost manager4 = new TerranGhost();
                manager1.update(ghost1);
                manager2.update(ghost2);
                manager3.update(ghost3);
                manager4.update(ghost4);

//                System.err.println("manager1.defineLockdownTarget() = " + manager1.defineLockdownTarget());
//                System.err.println("manager2.defineLockdownTarget() = " + manager2.defineLockdownTarget());
//                System.err.println("manager3.defineLockdownTarget() = " + manager3.defineLockdownTarget());

                assertEquals(dragoon, manager1.defineLockdownTarget());
                assertEquals(dragoon, manager2.defineLockdownTarget());
                assertEquals(reaver, manager3.defineLockdownTarget());
                assertEquals(wraith1, manager4.defineLockdownTarget());
            },
            () -> this.generateOur(),
            () -> this.generateEnemies()
        );
    }

    // =========================================================

    protected FakeUnit[] generateOur() {
        return fakeOurs(
            ghost1 = fake(Terran_Ghost, 10).setEnergy(100),
            ghost2 = fake(Terran_Ghost, 10).setEnergy(150),
            ghost3 = fake(Terran_Ghost, 15).setEnergy(150),
            ghost4 = fake(Terran_Ghost, 50).setEnergy(150),
            fake(Terran_SCV, 11),
            fake(Terran_Bunker, 12)
        );
    }

    protected FakeUnit[] generateEnemies() {
        FakeUnit[] fakeEnemies = fakeEnemies(
            dragoon = fakeEnemy(Protoss_Dragoon, 16),
            fakeEnemy(Zerg_Hydralisk, 19),
            fakeEnemy(Protoss_Zealot, 20),
            scout = fakeEnemy(Protoss_Scout, 22),
            reaver = fakeEnemy(Protoss_Reaver, 26),
            wraith1 = fakeEnemy(Terran_Wraith, 40).setCloaked(false).setEffVisible(true),
            wraith2 = fakeEnemy(Terran_Wraith, 45).setCloaked(true).setEffVisible(false)
        );

//        System.out.println("wraith = " + wraith1.effVisible());
//        System.out.println("wraith = " + wraith2.effVisible());

        return fakeEnemies;
    }

}
