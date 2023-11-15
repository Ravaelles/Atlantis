package atlantis.map.wall;

import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import bwem.ChokePoint;
import bwem.Tile;

import java.util.ArrayList;
import java.util.List;

public class GetWallIn {
    public static void get() {
        Structure structure = new Structure(3, 3);
        AChoke chokePoint = Chokes.mainChoke();
        APosition startPosition = APosition.create(chokePoint.rawChoke().getAreas().getFirst());
        APosition endPosition = APosition.create(chokePoint.rawChoke().getAreas().getSecond());

        WallInAlgorithm.wallIn(
            structures(), placedStructures(),
            chokePoint.center(), startPosition, endPosition,
            enemySize()
        );

//        // Access the result from structureSet
//        for (Structure placedStructure : structureSet) {
//            System.out.println("Placed structure at (" + placedStructure.x + ", " + placedStructure.y + ")");
//        }
    }

    private static List<Structure> structures() {
        ArrayList<Structure> list = new ArrayList<>();

        list.add(new Structure(2, 2));
        list.add(new Structure(3, 3));
        list.add(new Structure(2, 2));

        return list;
    }

    private static EnemyUnitToWallFrom enemySize() {
        return new EnemyUnitToWallFrom();
    }

    private static List<Structure> placedStructures() {
        return new ArrayList<>();
    }
}
