package atlantis;

import atlantis.debug.APainter;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import bwapi.Color;
import jbweb.Walls;

public class OnEveryFrame {

//    private static CappedList<Integer> frames = new CappedList<>(4);

    public static void update() {
        GameSpeed.checkIfNeedToSlowDown();

        if (Select.our().count() == 0) {
            GameSpeed.changeFrameSkipTo(100);
        }

//        for (AUnit unit : Select.ourCombatUnits().list()) {
//            if (unit.isUnderAttack(2) && unit.hpPercent() < 48) {
//                GameSpeed.changeSpeedTo(30);
//            }
//        }

//        EnemyUnits.printEnemyFoggedUnits();
//        System.out.println("ENEMY BASE = " + EnemyUnits.enemyBase());

//        Select.printCache();

        // JBWEB building positions (blocks)
//        Blocks.draw();
//        Stations.draw();
//        Walls.draw();

//        if (AGame.now() >= 5) {
//            Wall wall = Walls.createTWall();
//            Wall wall = Walls.getWall(Chokes.mainChoke().rawChoke());
//            Wall wall = Walls.createTWall();
//            System.out.println("wall = " + wall);
//        }

//        if (AGame.everyNthGameFrame(100)) {
//            for (Block block : Blocks.getBlocks()) {
//                System.out.println(block.isDefensive());
//            }
//        }

//        System.out.println("----- " + Squad.getAlphaSquad().size() );
//        for (AUnit unit : Squad.getAlphaSquad().list()) {
//            System.out.println(unit + " // " + unit.isAlive() + " // " + unit.hp());
//        }

//        AUnit scout = AScoutManager.firstScout();
//        if (scout != null) {
//            CameraManager.centerCameraOn(scout);
//        }

//        AUnit wraith = Select.ourOfType(AUnitType.Terran_Wraith).first();
//        if (wraith != null) {
//            CameraManager.centerCameraOn(wraith);
//        }

//        for (AFoggedUnit unit : EnemyUnits.discoveredAndAliveUnits()) {
//            if (unit.isBuilding()) {
//                System.out.println(unit.shortName() + " // " + unit.position() + " // " + unit.lastPositionUpdatedAgo());
//            }
//        }
//        for (AFoggedUnit unit : EnemyUnits.()) {
//            System.out.println(unit.shortName() + " // " + unit.position() + " // " + unit.lastPositionUpdatedAgo());
//        }

//        for (AUnit unit : Select.ourOfType(AUnitType.Terran_Marine).list()) {
//            System.out.println("marine = " + unit.canAttackAirUnits() + " // " + unit.getAirWeapon().damageAmount());
//        }

//        System.out.println(AGame.gas() + " // " + AGame.minerals());

//        for (AUnit unit : Select.enemies(AUnitType.Zerg_Lurker).list()) {
//            if (!unit.effVisible() || !unit.isDetected()) {
//                System.out.println(unit.shortName() + " // vis=" + unit.effVisible() + " // cloa=" + unit.effCloaked() + " // det=" + unit.isDetected());
//            }
//        }
//        for (AUnit unit : Select.enemies(AUnitType.Zerg_Hydralisk).list()) {
//            System.out.println(unit.shortName() + " // vis=" + unit.effVisible() + " // cloaked=" + unit.effCloaked());
//            break;
//        }

//        AUnit unit1 = Select.ourCombatUnits().first();
//        AUnit unit2 = Select.ourCombatUnits().last();
//
//        if (unit1 != null && (unit1.squad() == null || unit1.squad() == null)) {
//            System.out.println("unit1 = " + unit1.idWithHash() + unit1.shortName() + " // " + unit1.squad());
//            System.out.println("unit2 = " + unit2.idWithHash() + unit2.shortName() + " // " + unit2.squad());
//        }

//        for (AUnit unit : Select.all().list()) {
//            APainter.paintCircle(unit, 16, Color.Grey);
//        }
    }

}
