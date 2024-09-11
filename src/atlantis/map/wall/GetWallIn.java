package atlantis.map.wall;

import atlantis.debug.painter.AAdvancedPainter;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import bwapi.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GetWallIn {
    public static Set<Structure> get() {
        Structure structure = new Structure(3, 3);
        AChoke chokePoint = Chokes.mainChoke();

        if (
            chokePoint == null
                || chokePoint.rawChoke().getAreas().getFirst() == null
        ) return null;

        APosition startPosition = APosition.create(chokePoint.rawChoke()).position();
        APosition endPosition = APosition.create(chokePoint.rawChoke()).position();
//        APosition startPosition = APosition.create(chokePoint.rawChoke().getAreas().getFirst().);
//        APosition endPosition = APosition.create(chokePoint.rawChoke().getAreas().getSecond());

        Set<Structure> structures = WallInAlgorithm.wallIn(
            structures(), placedStructures(),
            chokePoint.center(), startPosition, endPosition,
            enemySize()
        );

        return structures;


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

    public static void paint(Set<Structure> structures) {
        if (structures == null) return;

        for (Structure structure : structures) {
            paint(structure);
        }
    }

    private static void paint(Structure structure) {
        APosition position = APosition.create(structure.tx(), structure.ty());
        AAdvancedPainter.paintRectangle(
            position,
            32 * structure.getWidth(), 32 * structure.getHeight(),
            Color.Orange
        );
        AAdvancedPainter.paintRectangle(
            position.translateByPixels(1, 1),
            32 * structure.getWidth() - 2, 32 * structure.getHeight() - 2,
            Color.Orange
        );
    }
}
