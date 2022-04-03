package atlantis.game;

import atlantis.debug.painter.AAdvancedPainter;
import atlantis.information.enemy.EnemyUnits;
import atlantis.map.*;
import atlantis.production.constructing.position.protoss.PylonPosition;
import atlantis.units.select.Select;
import bwapi.Color;
import bwapi.Position;
import bwem.BWEM;
import bwem.CPPath;
import bwem.ChokePoint;
import jbweb.Blocks;
import jbweb.Stations;

import java.util.Iterator;

public class OnEveryFrame {

//    private static CappedList<Integer> frames = new CappedList<>(4);

    public static void update() {
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

        if (Select.main() != null) {
            Position rawBaseP = Select.main().position().rawP();
            Position rawNaturalP = Bases.natural().position().rawP();
            CPPath path = AMap.getMap().getPath(rawBaseP, rawNaturalP);

            int index = 0;
            for (Iterator<ChokePoint> iterator = path.iterator(); iterator.hasNext(); ) {
                AChoke choke = AChoke.create(iterator.next());
                AAdvancedPainter.paintChoke(choke, Color.Orange, "Index:" + index, 3);
                index++;
            }
        }
    }

}
