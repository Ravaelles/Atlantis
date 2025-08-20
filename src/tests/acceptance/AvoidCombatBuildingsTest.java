package tests.acceptance;

import atlantis.architecture.Manager;
import atlantis.combat.CombatUnitManager;
import atlantis.combat.micro.avoid.EnemyUnitsToAvoid;
import atlantis.combat.micro.avoid.buildings.AvoidCombatBuildingClose;
import atlantis.combat.squad.squads.alpha.Alpha;
import atlantis.game.A;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.util.cache.Cache;
import cherryvis.ACherryVis;
import cherryvis.ACherryVisLogger;
import cherryvis.simple.ASimpleCherryVisLogger;
import cherryvis.simple.ASimpleCherryVisUnitLogger;
import cherryvis.simple.ASimpleCherryVis_UnitLogs;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import static atlantis.units.AUnitType.Zerg_Hydralisk;
import static atlantis.units.AUnitType.Zerg_Zergling;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AvoidCombatBuildingsTest extends AbstractTestWithWorld {
    private FakeUnit sunken;

    /**
     * With buildings there is a problem - JFAP returns "no danger" status
     * if the unit is 0.1 tiles outside of range of a Sunken Colony, but the
     * eval gets drastically worse once within range.
     */
    @Test
    public void neverRunsIntoCombatBuildings() {
        createWorld(50, () -> {
//        createWorld(5, () -> {
//            Select.our().print();
//            Select.enemy().print();

            FakeUnit unit = ourFirst;
            unit.forceSetSquad(Alpha.get());
            (new CombatUnitManager(unit)).invokeFrom(this);

            double distToSunken = distToNearestEnemy(unit);
            boolean isSafe = distToSunken > 7.05;
            boolean alwaysShow = false;
//            boolean alwaysShow = true;

            if (!isSafe || alwaysShow) {
                System.err.println(A.now()
                    + " -       " + unit.tooltip()
                    + "\n   Manager : " + unit.manager()
                    + "\n   Managers: " + unit.managerLogs().toString()
                    + "\n   Command : " + unit.lastCommand()
                    + ",\n   tx     :" + unit.txWithPrecision()
                    + ",\n   dist_to_sunken:" + A.dist(distToSunken)
                    + (unit.target == null ? "" : ",\n   dist_to_target:" + A.dist(unit, unit.target))
                    + (unit.targetPosition == null ? "" : ",\n   target_position:" + unit.targetPosition)
                    + "\n   marine eval = " + unit.eval()
                    + "\n   sunken eval = " + sunken.eval()
                );
                System.err.println("_______________________________________");
            }

            assertTrue(isSafe);

//            if (A.now == 5) {
//                System.out.println("Last frame: " + A.now());
//                ACherryVisLogger logger = ACherryVis.logger();
//                ASimpleCherryVisLogger simpleLogger = (ASimpleCherryVisLogger) logger;
//                ASimpleCherryVisUnitLogger unitLogger = simpleLogger.getUnitLogger();
//
//                System.out.println(ASimpleCherryVis_UnitLogs.build(unitLogger));
//            }
        });
    }

    @Test
    public void cherryVisLogger() {
//        createWorld(50, () -> {
        createWorld(4, () -> {
//                Select.our().print();
//                Select.enemy().print();

                FakeUnit unit = ourFirst;
                unit.forceSetSquad(Alpha.get());
                (new CombatUnitManager(unit)).invokeFrom(this);

                System.out.println("@" + A.now + " - " + unit.manager());

                if (A.now == 4) {
                    ACherryVisLogger logger = ACherryVis.logger();
                    ASimpleCherryVisLogger simpleLogger = (ASimpleCherryVisLogger) logger;
                    ASimpleCherryVisUnitLogger unitLogger = simpleLogger.getUnitLogger();

                    System.out.println("######### UnitLogs #########");
                    System.out.println(ASimpleCherryVis_UnitLogs.build(unitLogger));
                }
            },
            () -> fakeOurs(fake(AUnitType.Protoss_Dragoon, 10, 10)),
            () -> fakeEnemies(fake(Zerg_Zergling, 13.5, 10))
        );
    }

    @Test
    public void dragoonAvoidsCBs() {
        FakeUnit our = fake(AUnitType.Protoss_Dragoon, 10);
        FakeUnit enemy1, enemy2, enemy3, enemy4, enemy5;

        int inRange = 19;
        int outsideRange = 22;

        FakeUnit[] enemies = fakeEnemies(

//            fake(AUnitType.Zerg_Sunken_Colony, inRange),
            fake(AUnitType.Zerg_Sunken_Colony, outsideRange),

            enemy2 = fake(AUnitType.Terran_Missile_Turret, inRange),
            fake(AUnitType.Terran_Missile_Turret, outsideRange),

            enemy3 = fake(AUnitType.Zerg_Spore_Colony, inRange),
            fake(AUnitType.Zerg_Spore_Colony, outsideRange),

            fake(AUnitType.Protoss_Photon_Cannon, outsideRange),
//            fake(AUnitType.Protoss_Photon_Cannon, 20),
            enemy4 = fake(AUnitType.Protoss_Photon_Cannon, 21.1)

//            enemy4 = fake(AUnitType.Terran_Bunker, inRange - 5),
//            fake(AUnitType.Terran_Bunker, outsideRange)
        );

        usingFakeOurAndFakeEnemies(our, enemies, () -> {
//            assertContainsAll(
//                new FakeUnit[]{enemy1, enemy2, enemy3, enemy4},
//                (new EnemyUnitsToAvoid(our)).enemiesDangerouslyClose().array()
//            );

            AvoidCombatBuildingClose manager =
                (AvoidCombatBuildingClose) (new AvoidCombatBuildingClose(our)).invokeFrom(null);

            Assertions.assertNotNull(manager);
            Assertions.assertEquals(enemy4, manager.combatBuilding());
        });
    }

    @Test
    public void combatBuildingsAgainstGround() {
        FakeUnit our = fake(AUnitType.Protoss_Zealot, 10);
        FakeUnit cannon, sunken, bunker, enemy4;

        double inRange = 17.5; // Range is 7, but we need some safety margin which varies depending on the unit
        int outsideRange = 22;

        FakeUnit[] enemies = fakeEnemies(
            sunken = fake(AUnitType.Zerg_Sunken_Colony, inRange),
            fake(AUnitType.Zerg_Sunken_Colony, outsideRange),

            cannon = fake(AUnitType.Protoss_Photon_Cannon, inRange),
            fake(AUnitType.Protoss_Photon_Cannon, outsideRange),

            fake(AUnitType.Terran_Missile_Turret, inRange),

            bunker = fake(AUnitType.Terran_Bunker, inRange),
            fake(AUnitType.Terran_Bunker, outsideRange),

            fake(AUnitType.Zerg_Spore_Colony, inRange)
        );

        usingFakeOurAndFakeEnemies(our, enemies, () -> {
            AvoidCombatBuildingClose manager =
                (AvoidCombatBuildingClose) (new AvoidCombatBuildingClose(our)).invokeFrom(null);

            Assertions.assertNotNull(manager);
            Assertions.assertEquals(sunken, manager.combatBuilding());
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

            enemy3 = fake(AUnitType.Terran_Bunker, inRange),
            fake(AUnitType.Terran_Bunker, outsideRange),

            enemy4 = fake(AUnitType.Zerg_Spore_Colony, 16.9),
            fake(AUnitType.Zerg_Spore_Colony, outsideRange)
        );

        usingFakeOurAndFakeEnemies(our, enemies, () -> {
//            assertContainsAll(
//                new FakeUnit[]{enemy1, enemy2, enemy3, enemy4},
//                (new EnemyUnitsToAvoid(our)).enemiesDangerouslyClose().array()
//            );

            AvoidCombatBuildingClose manager =
                (AvoidCombatBuildingClose) (new AvoidCombatBuildingClose(our)).invokeFrom(null);

            Assertions.assertNotNull(manager);
            Assertions.assertEquals(enemy4, manager.combatBuilding());
        });
    }

    @Test
    public void unfinishedUnits() {
        FakeUnit our = fake(AUnitType.Terran_Wraith, 10);
        FakeUnit enemy1, enemy2, enemy3, enemy4;

        FakeUnit[] enemies = fakeEnemies(
            fake(AUnitType.Protoss_Photon_Cannon, 11).setCompleted(false),

            fake(AUnitType.Zerg_Sunken_Colony, 12).setCompleted(false),
            fake(AUnitType.Zerg_Sunken_Colony, 13).setCompleted(false),

            enemy3 = fake(AUnitType.Terran_Missile_Turret, 12).setCompleted(false),
            fake(AUnitType.Terran_Missile_Turret, 13).setCompleted(false),

            enemy4 = fake(AUnitType.Zerg_Spore_Colony, 12).setCompleted(false),
            fake(AUnitType.Zerg_Spore_Colony, 13).setCompleted(false),

            enemy1 = fake(AUnitType.Protoss_Photon_Cannon, 17).setCompleted(false)
        );

        createWorld(1, () -> {
                enemy1.setCompleted(true);
//                Cache.nukeAllCaches();

                AvoidCombatBuildingClose manager =
                    (AvoidCombatBuildingClose) (new AvoidCombatBuildingClose(our)).invokeFrom(null);

                Assertions.assertNotNull(manager);
                Assertions.assertEquals(enemy1, manager.combatBuilding());
            },
            our,
            enemies
        );
    }

    // =========================================================

    protected FakeUnit[] generateOur() {
        return fakeOurs(
            fake(AUnitType.Terran_Marine, 10)
//                fake(AUnitType.Terran_Marine, 11),
//                fake(AUnitType.Terran_Marine, 11),
//                fake(AUnitType.Terran_Marine, 11),
//                fake(AUnitType.Terran_Marine, 11),
//                fake(AUnitType.Terran_Marine, 11),
//                fake(AUnitType.Terran_Marine, 11),
//                fake(AUnitType.Terran_Medic, 11)
        );
    }

    protected FakeUnit[] generateEnemies() {
        int enemyTy = 19;
        return fakeEnemies(
            sunken = fake(AUnitType.Zerg_Sunken_Colony, enemyTy),
            fake(AUnitType.Zerg_Sunken_Colony, enemyTy + 10)
        );
    }

}
