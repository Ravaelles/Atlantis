package atlantis.game;

import atlantis.Atlantis;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

public class OnEveryFrame {

//    private static CappedList<Integer> frames = new CappedList<>(4);

    public static void update() {
//        AUnit marine = Select.ourCombatUnits().first();
//        AUnit enemy = marine.enemiesNear().first();
//
//        if (enemy != null) {
//            String dist = A.dist(marine, enemy);
//
//            System.out.println("@ " + A.now() + " - " + dist + ", " + marine.action());
//        }

//        if (Atlantis.KILLED >= 3) {
//            if (A.everyNthGameFrame(120)) {
//                System.out.println("----------------- @ " + A.now());
//                Select.ourCombatUnits().print("Our combat");
//                System.out.println("----");
//                Select.enemy().print("ENEMY");
//            }
//        }

//        GameSpeed.checkIfNeedToSlowDown();

//        AAdvancedPainter.paintBuildingPosition(PylonPosition.positionForFirstPylon(), "First");
//        AAdvancedPainter.paintBuildingPosition(PylonPosition.positionForSecondPylon(), "Second");

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

//        if (A.everyNthGameFrame(200)) {
//            System.out.println("Main choke = " + Chokes.mainChoke());
//            System.out.println("Natural choke = " + Chokes.natural());
//        }

//        if (Select.main() != null) {
//            Position rawBaseP = Select.main().position().rawP();
//            Position rawNaturalP = Bases.natural().position().rawP();
//            CPPath path = AMap.getMap().getPath(rawBaseP, rawNaturalP);
//
//            int index = 0;
//            for (Iterator<ChokePoint> iterator = path.iterator(); iterator.hasNext(); ) {
//                AChoke choke = AChoke.create(iterator.next());
//                AAdvancedPainter.paintChoke(choke, Color.Orange, "Index:" + index, 3);
//                index++;
//            }
//        }

//        AUnit ourCombat = Select.ourCombatUnits().first();
//        if (ourCombat != null) {
//            System.out.println("wouldWin = " + AtlantisJfap.wouldWin(ourCombat));
//        }
    }

}
