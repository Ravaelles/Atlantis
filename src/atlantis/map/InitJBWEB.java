package atlantis.map;

import atlantis.Atlantis;
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
//            Blocks.findBlocks();
            Stations.findStations();
//            Wall wall = Walls.createTWall();

//            if (!wall.getDefenses().isEmpty()) {
//                System.out.println("Walls.getWalls() = " + Walls.getWalls());
//                System.out.println("Wall = " + wall);
//
//                wall = Walls.getWall(Chokes.mainChoke().rawChoke());
//                System.out.println("Walls.getWalls() = " + Walls.getWalls());
//                System.out.println("Wall = " + wall);
//            }
//            else {
//                System.err.println("Not able to init wall");
//            }
        } catch (Exception e) {
            System.err.println("JBWEB exception: " + e.getMessage());
            e.printStackTrace();
            Atlantis.getInstance().exitGame();
        }
    }

}
