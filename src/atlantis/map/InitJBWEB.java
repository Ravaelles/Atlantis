package atlantis.map;

import atlantis.Atlantis;
import jbweb.Blocks;
import jbweb.JBWEB;
import jbweb.Stations;

public class InitJBWEB {

    public static void init() {
        //        if (!A.isUms() && Select.main() != null && Count.workers() == 4) {
        try {
            JBWEB.onStart(Atlantis.game(), AMap.bwem);
            Blocks.findBlocks();
            Stations.findStations();
//            Wall wall = Walls.createTWall();
//            System.out.println("Walls.getWalls() = " + Walls.getWalls());
//            System.out.println("Wall = " + wall);

//            wall = Walls.getWall(Chokes.mainChoke().rawChoke());
//            System.out.println("Walls.getWalls() = " + Walls.getWalls());
//            System.out.println("Wall = " + wall);
        } catch (Exception e) {
            System.err.println("JBWEB exception: " + e.getMessage());
//            AGame.setUmsMode();
        }
//        }
    }

}
