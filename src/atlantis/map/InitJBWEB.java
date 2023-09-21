package atlantis.map;

import atlantis.Atlantis;
import atlantis.game.A;
import jbweb.Blocks;
import jbweb.JBWEB;
import jbweb.Stations;

public class InitJBWEB {

    /**
     * Walls are completely broken. I tried fixing it twice, but failed.
     * Still JBWEB is used for ground dist calculation.
     */
    public static void init() {
        try {
            JBWEB.onStart(Atlantis.game(), AMap.bwem);
            Blocks.findBlocks();
            Stations.findStations();
//            Wall wall = Walls.createTWall();

//            if (!wall.getDefenses().isEmpty()) {


//
//                wall = Walls.getWall(Chokes.mainChoke().rawChoke());


//            }
//            else {
//                System.err.println("Not able to init wall");
//            }
        } catch (Exception e) {
            if (!A.isUms()) {
                System.err.println("JBWEB exception: " + e.getMessage());
                e.printStackTrace();
                Atlantis.getInstance().exitGame();
            }
            else {
                A.errPrintln("InitJBWEB failed, but continuing to play.");
            }
        }
    }

}
