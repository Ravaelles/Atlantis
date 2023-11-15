package atlantis.map;

import atlantis.Atlantis;
import atlantis.game.A;
import jbweb.*;

public class InitJBWEB {

    /**
     * Walls are completely broken. I tried fixing it twice, but failed.
     * Still JBWEB is used for ground dist calculation.
     */
    public static void init() {
//        try {
        JBWEB.onStart(Atlantis.game(), AMap.bwem);
        Blocks.findBlocks();
        Stations.findStations();

//        Wall wall = Walls.createTWall();
//        System.out.println("-------------------");
//        System.out.println("wall = " + wall);
//        System.err.println("wall = " + wall.getRawBuildings());
//        System.err.println("wall = " + wall.getDefenses().size());


//            if (!wall.getDefenses().isEmpty()) {
//                wall = Walls.getWall(Chokes.mainChoke().rawChoke());

//            }
//            else {
//                System.err.println("Not able to init wall");
//            }
//        } catch (Exception e) {
//            if (!A.isUms()) {
//                System.err.println("JBWEB exception: " + e.getMessage());
//                e.printStackTrace();
//                Atlantis.getInstance().exitGame();
//            }
//            else {
//                A.errPrintln("InitJBWEB failed, but continuing to play.");
//            }
//        }

//        Atlantis.getInstance().exitGame();
    }
}
