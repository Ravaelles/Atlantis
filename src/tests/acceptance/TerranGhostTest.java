package tests.acceptance;

import atlantis.combat.micro.terran.infantry.TerranGhost;
import org.junit.Test;
import tests.unit.FakeUnit;

import static atlantis.units.AUnitType.*;
import static junit.framework.TestCase.assertEquals;

public class TerranGhostTest extends AbstractTestFakingGame {

    protected FakeUnit ghost1;
    protected FakeUnit ghost2;
    protected FakeUnit ghost3;
    protected FakeUnit ghost4;
    protected FakeUnit ghost5;
    protected FakeUnit dragoon;
    protected FakeUnit scout;
    protected FakeUnit reaver;
    protected FakeUnit wraith1;
    protected FakeUnit wraith2;

    @Test
    public void ghostIsTargetingMechanicalUnitsForLockdown() {
        createWorld(1, () -> {
                TerranGhost manager1 = new TerranGhost(ghost1);
                TerranGhost manager2 = new TerranGhost(ghost2);
                TerranGhost manager3 = new TerranGhost(ghost3);
                TerranGhost manager4 = new TerranGhost(ghost4);
                TerranGhost manager5 = new TerranGhost(ghost5);
                manager1.update();
                manager2.update();
                manager3.update();
                manager4.update();
                manager5.update();

//                System.out.println("Lockdown targetFor(ghost1) = " + TerranGhost.lockdownTargets.targetFor(ghost1));
//                System.out.println("Lockdown targetFor(ghost2) = " + TerranGhost.lockdownTargets.targetFor(ghost2));
//                System.out.println("Lockdown targetFor(ghost3) = " + TerranGhost.lockdownTargets.targetFor(ghost3));
//                System.out.println("Lockdown targetFor(ghost4) = " + TerranGhost.lockdownTargets.targetFor(ghost4));
//                System.out.println("Lockdown targetFor(ghost5) = " + TerranGhost.lockdownTargets.targetFor(ghost5));

                assertEquals(reaver, TerranGhost.lockdownTargets.targetFor(ghost1)); // Reaver
                assertEquals(dragoon, TerranGhost.lockdownTargets.targetFor(ghost2)); // Dragoon
                assertEquals(scout, TerranGhost.lockdownTargets.targetFor(ghost3)); // Scout
                assertEquals(wraith1, TerranGhost.lockdownTargets.targetFor(ghost4)); // Wraith
                assertEquals(null, TerranGhost.lockdownTargets.targetFor(ghost5)); // null

                TerranGhost.lockdownTargets.clear();
            },
            () -> this.generateOur(),
            () -> this.generateEnemies()
        );
    }

    // =========================================================

    protected FakeUnit[] generateOur() {
        return fakeOurs(
            ghost1 = fake(Terran_Ghost, 15).setEnergy(100),
            ghost2 = fake(Terran_Ghost, 10).setEnergy(150),
            ghost3 = fake(Terran_Ghost, 10).setEnergy(150),
            ghost4 = fake(Terran_Ghost, 50).setEnergy(150),
            ghost5 = fake(Terran_Ghost, 50).setEnergy(150),
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

        return fakeEnemies;
    }

}
