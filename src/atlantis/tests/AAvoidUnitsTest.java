package atlantis.tests;

import atlantis.combat.micro.avoid.AAvoidUnits;
import atlantis.units.AUnitType;
import org.junit.Test;

public class AAvoidUnitsTest extends AbstractTestWithUnits {

    @Test
    public void zergUnits() {
        FakeUnit our = fake(AUnitType.Terran_Marine, 10);
        FakeUnit enemy1, enemy2, enemy3, enemy4;

        FakeUnit[] enemies = fakeUnits(
                enemy1 = fake(AUnitType.Zerg_Drone, 12),
                fake(AUnitType.Zerg_Larva, 11),
                fake(AUnitType.Zerg_Egg, 11),
                fake(AUnitType.Zerg_Lurker_Egg, 11),
                fake(AUnitType.Zerg_Cocoon, 11),
                fake(AUnitType.Zerg_Creep_Colony, 12),
                fake(AUnitType.Zerg_Spore_Colony, 12),
                fake(AUnitType.Zerg_Drone, 13),
                enemy2 = fake(AUnitType.Zerg_Zergling, 13),
                enemy3 = fake(AUnitType.Zerg_Hydralisk, 14),
                enemy4 = fake(AUnitType.Zerg_Sunken_Colony, 16),
                fake(AUnitType.Zerg_Creep_Colony, 11),
                fake(AUnitType.Zerg_Hatchery, 11),
                fake(AUnitType.Zerg_Sunken_Colony, 28)
        );

        usingMockedOurAndEnemies(our, enemies, () -> {
            assertContainsAll(
                    new FakeUnit[] { enemy1, enemy2, enemy3, enemy4 },
                    AAvoidUnits.unitsToAvoid(our).array()
            );
        });
    }

    @Test
    public void unfinishedUnits() {
        FakeUnit our = fake(AUnitType.Terran_Wraith, 10);
        FakeUnit enemy1, enemy2, enemy3, enemy4;

        FakeUnit[] enemies = fakeUnits(
                enemy1 = fake(AUnitType.Protoss_Photon_Cannon, 12),
                fake(AUnitType.Protoss_Photon_Cannon, 11).setCompleted(false),

                enemy2 = fake(AUnitType.Terran_Goliath, 16),

                fake(AUnitType.Zerg_Sunken_Colony, 12),
                fake(AUnitType.Zerg_Sunken_Colony, 13).setCompleted(false),

                enemy3 = fake(AUnitType.Terran_Missile_Turret, 12),
                fake(AUnitType.Terran_Missile_Turret, 13).setCompleted(false),

                enemy4 = fake(AUnitType.Zerg_Spore_Colony, 12),
                fake(AUnitType.Zerg_Spore_Colony, 13).setCompleted(false)
        );

        usingMockedOurAndEnemies(our, enemies, () -> {
            assertContainsAll(
                    new FakeUnit[] { enemy1, enemy2, enemy3, enemy4 },
                    AAvoidUnits.unitsToAvoid(our).array()
            );
        });
    }

    @Test
    public void combatBuildingsAgainstGround() {
        FakeUnit our = fake(AUnitType.Terran_Marine, 10);
        FakeUnit enemy1, enemy2, enemy3, enemy4;

        int inRange = 19; // Range is 7, but we need some safety margin which varies depending on the unit
        int outsideRange = 21;
        
        FakeUnit[] enemies = fakeUnits(
                enemy1 = fake(AUnitType.Protoss_Photon_Cannon, inRange),
                fake(AUnitType.Protoss_Photon_Cannon, outsideRange),

                enemy2 = fake(AUnitType.Zerg_Sunken_Colony, inRange),
                fake(AUnitType.Zerg_Sunken_Colony, outsideRange),

                fake(AUnitType.Terran_Missile_Turret, inRange),

                enemy3 = fake(AUnitType.Terran_Bunker, inRange - 4),
                fake(AUnitType.Terran_Bunker, outsideRange),

                fake(AUnitType.Zerg_Spore_Colony, inRange)
        );

        usingMockedOurAndEnemies(our, enemies, () -> {
            assertContainsAll(
                    new FakeUnit[] { enemy1, enemy2, enemy3 },
                    AAvoidUnits.unitsToAvoid(our).array()
            );
        });
    }

    @Test
    public void combatBuildingsAgainstAir() {
        FakeUnit our = fake(AUnitType.Terran_Wraith, 10);
        FakeUnit enemy1, enemy2, enemy3, enemy4;

        int inRange = 19; // Range is 7, but we need some safety margin which varies depending on the unit
        int outsideRange = 24;

        FakeUnit[] enemies = fakeUnits(
                enemy1 = fake(AUnitType.Protoss_Photon_Cannon, inRange),
                fake(AUnitType.Protoss_Photon_Cannon, outsideRange),

                fake(AUnitType.Zerg_Sunken_Colony, inRange),
                fake(AUnitType.Zerg_Sunken_Colony, outsideRange),

                enemy2 = fake(AUnitType.Terran_Missile_Turret, inRange),
                fake(AUnitType.Terran_Missile_Turret, outsideRange),

                enemy3 = fake(AUnitType.Zerg_Spore_Colony, inRange),
                fake(AUnitType.Zerg_Spore_Colony, outsideRange),

                enemy4 = fake(AUnitType.Terran_Bunker, inRange - 5),
                fake(AUnitType.Terran_Bunker, outsideRange)
        );

        usingMockedOurAndEnemies(our, enemies, () -> {
            assertContainsAll(
                    new FakeUnit[] { enemy1, enemy2, enemy3, enemy4 },
                    AAvoidUnits.unitsToAvoid(our).array()
            );
        });
    }

}