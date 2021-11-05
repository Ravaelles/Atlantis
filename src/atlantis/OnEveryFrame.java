package atlantis;

import atlantis.combat.squad.Squad;
import atlantis.map.Chokes;
import atlantis.units.AUnit;
import jbweb.Blocks;
import jbweb.Stations;
import jbweb.Wall;
import jbweb.Walls;

public class OnEveryFrame {

//    private static CappedList<Integer> frames = new CappedList<>(4);

    public static void update() {
        GameSpeed.checkIfNeedToSlowDown();

//        for (AUnit unit : Select.ourCombatUnits().list()) {
//            if (unit.isUnderAttack(2) && unit.hpPercent() < 48) {
//                GameSpeed.changeSpeedTo(30);
//            }
//        }

//        AEnemyUnits.printEnemyFoggedUnits();
//        System.out.println("ENEMY BASE = " + AEnemyUnits.enemyBase());

//        Select.printCache();

        // JBWEB building positions (blocks)
//        Blocks.draw();
        Stations.draw();
        Walls.draw();

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
    }

}
