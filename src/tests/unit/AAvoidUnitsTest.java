package tests.unit;

import atlantis.combat.micro.avoid.AAvoidUnits;
import atlantis.game.AGame;
import atlantis.information.enemy.EnemyInfo;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.BaseSelect;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Arrays;

public class AAvoidUnitsTest extends AbstractTestWithUnits {

    public MockedStatic<AGame> aGame;
    public MockedStatic<EnemyInfo> enemyInformation;

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
                ling1 = fake(AUnitType.Zerg_Zergling, 12.5),
                hydra = fake(AUnitType.Zerg_Hydralisk, 14),
                sunken = fake(AUnitType.Zerg_Sunken_Colony, 16),
                fake(AUnitType.Zerg_Creep_Colony, 11),
                ling2 = fake(AUnitType.Zerg_Zergling, 12.9),
                fake(AUnitType.Zerg_Zergling, 13),
                fake(AUnitType.Zerg_Zergling, 14),
                fake(AUnitType.Zerg_Zergling, 15),
                fake(AUnitType.Zerg_Zergling, 16),
                fake(AUnitType.Zerg_Zergling, 17),
                fake(AUnitType.Zerg_Hydralisk, 18),
                fake(AUnitType.Zerg_Sunken_Colony, 28)
        );

        usingFakeOurAndFakeEnemies(our, enemies, () -> {
            assertContainsAll(
                    new FakeUnit[] { drone, ling1, hydra, sunken, ling2 },
                    AAvoidUnits.unitsToAvoid(our).array()
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
        int outsideRange = 22;
        
        FakeUnit[] enemies = fakeEnemies(
                enemy1 = fake(AUnitType.Protoss_Photon_Cannon, inRange),
                fake(AUnitType.Protoss_Photon_Cannon, outsideRange),

                enemy2 = fake(AUnitType.Zerg_Sunken_Colony, inRange),
                fake(AUnitType.Zerg_Sunken_Colony, outsideRange),

                fake(AUnitType.Terran_Missile_Turret, inRange),

                enemy3 = fake(AUnitType.Terran_Bunker, inRange - 4),
                fake(AUnitType.Terran_Bunker, outsideRange),

                fake(AUnitType.Zerg_Spore_Colony, inRange)
        );

        usingFakeOurAndFakeEnemies(our, enemies, () -> {
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
                    new FakeUnit[] { enemy1, enemy2, enemy3, enemy4 },
                    AAvoidUnits.unitsToAvoid(our).array()
            );
        });
    }

    @Test
    public void avoidsFuckedSorryFoggedUnits() {
        int inRange = 15;
        int outsideRange = 30;
        final AUnit enemy1;

        FakeUnit our = fake(AUnitType.Terran_Siege_Tank_Siege_Mode, 10);

        FakeUnit[] enemies = fakeEnemies(
                (FakeUnit) (enemy1 = fake(AUnitType.Protoss_Photon_Cannon, inRange)),
                fake(AUnitType.Protoss_Photon_Cannon, outsideRange)
        );

        // =========================================================

        int framesNow = 1;
        try (MockedStatic<BaseSelect> baseSelect = Mockito.mockStatic(BaseSelect.class)) {
            aGame = Mockito.mockStatic(AGame.class);
            aGame.when(AGame::now).thenReturn(framesNow);

            FakeFoggedUnit enemy2, enemy3, enemy4, enemy5;

            FakeFoggedUnit[] fogged = new FakeFoggedUnit[] {
                    enemy2 = fogged(AUnitType.Protoss_Photon_Cannon, inRange),
                    fogged(AUnitType.Protoss_Photon_Cannon, outsideRange),
                    enemy3 = fogged(AUnitType.Zerg_Sunken_Colony, inRange),
                    fogged(AUnitType.Zerg_Sunken_Colony, outsideRange),
                    fogged(AUnitType.Protoss_Zealot, inRange),
                    fogged(AUnitType.Zerg_Mutalisk, inRange),
                    enemy4 = fogged(AUnitType.Terran_Siege_Tank_Siege_Mode, inRange),
                    fogged(AUnitType.Terran_Siege_Tank_Siege_Mode, outsideRange),
                    fogged(AUnitType.Terran_Siege_Tank_Tank_Mode, inRange),
                    fogged(AUnitType.Terran_Siege_Tank_Tank_Mode, outsideRange),
                    enemy5 = fogged(AUnitType.Zerg_Lurker, inRange),
                    fogged(AUnitType.Zerg_Lurker, outsideRange)
            };

            enemyInformation = Mockito.mockStatic(EnemyInfo.class);
            enemyInformation.when(EnemyInfo::discoveredAndAliveUnits).thenReturn(Arrays.asList(fogged));

            baseSelect.when(BaseSelect::ourUnits).thenReturn(Arrays.asList(our));
            baseSelect.when(BaseSelect::enemyUnits).thenReturn(Arrays.asList(enemies));

            assertContainsAll(
                    new AUnit[] { enemy1, enemy2, enemy3, enemy4, enemy5 },
                    AAvoidUnits.unitsToAvoid(our).array()
            );
        }
    }

}