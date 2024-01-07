package tests.acceptance;

import atlantis.combat.micro.avoid.AvoidEnemies;
import atlantis.combat.micro.avoid.EnemyUnitsToAvoid;
import atlantis.game.AGame;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.enemy.EnemyUnitsUpdater;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Units;
import atlantis.units.fogged.FakeFoggedUnit;
import atlantis.units.select.BaseSelect;
import atlantis.units.select.Select;
import atlantis.util.Angle;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import tests.unit.FakeUnit;

import java.util.Arrays;

public class AvoidEnemiesTest extends NonAbstractTestFakingGame {
    public MockedStatic<AGame> aGame;

    @Test
    public void zergUnits() {
        FakeUnit our = fake(AUnitType.Terran_Marine, 10);
        FakeUnit drone, ling1, hydra, sunken, ling2;

        FakeUnit[] enemies = fakeEnemies(
            drone = fake(AUnitType.Zerg_Drone, 12),
            fake(AUnitType.Zerg_Larva, 11),
            fake(AUnitType.Zerg_Egg, 11),
            fake(AUnitType.Zerg_Hatchery, 11),
            fake(AUnitType.Zerg_Lurker_Egg, 11),
            fake(AUnitType.Zerg_Cocoon, 11),
            fake(AUnitType.Zerg_Creep_Colony, 12),
            fake(AUnitType.Zerg_Spore_Colony, 12),
            fake(AUnitType.Zerg_Drone, 13),
            fake(AUnitType.Zerg_Drone, 14),
            ling1 = fake(AUnitType.Zerg_Zergling, 11.8),
            hydra = fake(AUnitType.Zerg_Hydralisk, 14),
            sunken = fake(AUnitType.Zerg_Sunken_Colony, 16),
            fake(AUnitType.Zerg_Creep_Colony, 11),
            ling2 = fake(AUnitType.Zerg_Zergling, 12.9).setAngle(Angle.degreesToRadians(180)),
            fake(AUnitType.Zerg_Zergling, 13).setAngle(Angle.degreesToRadians(180)),
            fake(AUnitType.Zerg_Zergling, 14).setAngle(Angle.degreesToRadians(180)),
            fake(AUnitType.Zerg_Zergling, 15).setAngle(Angle.degreesToRadians(180)),
            fake(AUnitType.Zerg_Zergling, 16).setAngle(Angle.degreesToRadians(180)),
            fake(AUnitType.Zerg_Zergling, 17).setAngle(Angle.degreesToRadians(180)),
            fake(AUnitType.Zerg_Hydralisk, 19),
            fake(AUnitType.Zerg_Sunken_Colony, 28)
        );

        usingFakeOurAndFakeEnemies(our, enemies, () -> {
            assertContainsAll(
                new FakeUnit[]{drone, ling1, hydra, sunken},
                (new EnemyUnitsToAvoid(our)).enemiesDangerouslyClose().array()
            );
        });
    }

    @Test
    public void unfinishedUnits() {
        FakeUnit our = fake(AUnitType.Terran_Wraith, 10);
        FakeUnit enemy1, enemy2, enemy3, enemy4;

        FakeUnit[] enemies = fakeEnemies(
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

        usingFakeOurAndFakeEnemies(our, enemies, () -> {
            assertContainsAll(
                new FakeUnit[]{enemy1, enemy2, enemy3, enemy4},
                (new EnemyUnitsToAvoid(our)).enemiesDangerouslyClose().array()
            );
        });
    }

    @Test
    public void combatBuildingsAgainstGround() {
        FakeUnit our = fake(AUnitType.Terran_Marine, 10);
        FakeUnit cannon, sunken, bunker, enemy4;

        double inRange = 16.9; // Range is 7, but we need some safety margin which varies depending on the unit
        int outsideRange = 22;

        FakeUnit[] enemies = fakeEnemies(
            cannon = fake(AUnitType.Protoss_Photon_Cannon, inRange),
            fake(AUnitType.Protoss_Photon_Cannon, outsideRange),

            sunken = fake(AUnitType.Zerg_Sunken_Colony, inRange),
            fake(AUnitType.Zerg_Sunken_Colony, outsideRange),

            fake(AUnitType.Terran_Missile_Turret, inRange),

            bunker = fake(AUnitType.Terran_Bunker, inRange - 1),
            fake(AUnitType.Terran_Bunker, outsideRange),

            fake(AUnitType.Zerg_Spore_Colony, inRange)
        );

        usingFakeOurAndFakeEnemies(our, enemies, () -> {
            assertContainsAll(
                new FakeUnit[]{cannon, sunken, bunker},
                (new EnemyUnitsToAvoid(our)).enemiesDangerouslyClose().array()
            );
        });
    }

    @Test
    public void combatBuildingsAgainstAir() {
        FakeUnit our = fake(AUnitType.Terran_Wraith, 10);
        FakeUnit enemy1, enemy2, enemy3, enemy4;

        int inRange = 19; // Range is 7, but we need some safety margin which varies depending on the unit
        int outsideRange = 24;

        FakeUnit[] enemies = fakeEnemies(
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

        usingFakeOurAndFakeEnemies(our, enemies, () -> {
            assertContainsAll(
                new FakeUnit[]{enemy1, enemy2, enemy3, enemy4},
                (new EnemyUnitsToAvoid(our)).enemiesDangerouslyClose().array()
            );
        });
    }

    @Test
    public void wraithAvoidsEnemies() {
        FakeUnit our = fake(AUnitType.Terran_Wraith, 10);
        FakeUnit enemy1, enemy2, enemy3, enemy4, enemy5;

        int inRange = 19; // Range is 7, but we need some safety margin which varies depending on the unit
        int outsideRange = 24;

        FakeUnit[] enemies = fakeEnemies(
            enemy1 = fake(AUnitType.Protoss_Photon_Cannon, inRange),
            fake(AUnitType.Protoss_Photon_Cannon, outsideRange),

            fake(AUnitType.Zerg_Sunken_Colony, inRange),
            fake(AUnitType.Zerg_Sunken_Colony, outsideRange),

            enemy2 = fake(AUnitType.Terran_Missile_Turret, inRange),
            fake(AUnitType.Terran_Missile_Turret, outsideRange),

            enemy3 = fake(AUnitType.Zerg_Spore_Colony, inRange),
            fake(AUnitType.Zerg_Spore_Colony, outsideRange),

            enemy4 = fake(AUnitType.Terran_Bunker, inRange - 5),
            fake(AUnitType.Terran_Bunker, outsideRange),

            enemy5 = fake(AUnitType.Protoss_Dragoon, inRange - 5),
            fake(AUnitType.Protoss_Dragoon, outsideRange)
        );

        usingFakeOurAndFakeEnemies(our, enemies, () -> {
            assertContainsAll(
                new FakeUnit[]{enemy1, enemy2, enemy3, enemy4, enemy5},
                (new EnemyUnitsToAvoid(our)).enemiesDangerouslyClose().array()
            );
        });
    }

    @Test
    public void avoidsFuckedSorryFoggedUnits() {
        AUnit enemy1;
        final int inRange = 12;
        final int outsideRange = 50;

        FakeUnit[] ours = fakeOurs(
            fake(AUnitType.Terran_Siege_Tank_Siege_Mode, 10)
        );
        FakeUnit[] enemies = fakeEnemies(
            (FakeUnit) (enemy1 = fake(AUnitType.Protoss_Photon_Cannon, inRange)),
            fake(AUnitType.Protoss_Photon_Cannon, outsideRange)
        );

        AUnit ourUnit = ours[0];

        createWorld(1, () ->
            {
                FakeFoggedUnit enemy2, enemy3, enemy4, enemy5, enemy6, enemy7;
                FakeFoggedUnit skippedTank1, skippedTank2;

                FakeFoggedUnit[] fogged = new FakeFoggedUnit[]{
                    enemy2 = fogged(AUnitType.Protoss_Photon_Cannon, inRange + 1.1),
                    fogged(AUnitType.Protoss_Photon_Cannon, outsideRange),
                    enemy3 = fogged(AUnitType.Zerg_Sunken_Colony, inRange + 1.2),
                    fogged(AUnitType.Zerg_Sunken_Colony, outsideRange + 1),
                    enemy4 = fogged(AUnitType.Protoss_Zealot, inRange + 1.3),
                    fogged(AUnitType.Zerg_Mutalisk, outsideRange + 2),
                    enemy5 = fogged(AUnitType.Terran_Siege_Tank_Siege_Mode, inRange + 1.4),
                    skippedTank1 = fogged(AUnitType.Terran_Siege_Tank_Siege_Mode, outsideRange + 3),
                    enemy6 = fogged(AUnitType.Terran_Siege_Tank_Tank_Mode, inRange + 2),
                    skippedTank2 = fogged(AUnitType.Terran_Siege_Tank_Tank_Mode, outsideRange + 4),
                    enemy7 = fogged(AUnitType.Zerg_Lurker, inRange + 3),
                    fogged(AUnitType.Zerg_Lurker, outsideRange + 5)
                };

                for (FakeFoggedUnit unit : fogged) {
                    EnemyUnitsUpdater.weDiscoveredEnemyUnit(unit);
                }

//                Select.enemy().print("SELECT Enemy units");
//                Select.from(
//                    (new Units()).addUnits(EnemyUnits.discovered().sortDataByDistanceTo(ourUnit, true))
//                ).print("Enemy DISCOVERED units");

                assertContainsAll(
                    new AUnit[]{enemy1, enemy2, enemy3, enemy4, enemy5, enemy6, enemy7},
                    (new EnemyUnitsToAvoid(ourUnit)).enemiesDangerouslyClose().array()
                );
            },
            () -> ours,
            () -> enemies
        );
    }

}
