package atlantis.map.path;

import atlantis.debug.painter.AAdvancedPainter;
import atlantis.information.enemy.EnemyUnits;
import atlantis.map.AMap;
import atlantis.map.choke.AChoke;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import bwapi.Color;
import bwem.CPPath;
import bwem.ChokePoint;

import java.util.ArrayList;
import java.util.Iterator;

public class PathToEnemyBase {
    public static ArrayList<AChoke> nodesToEnemyBase() {
        CPPath path = definePath();

        if (path == null) {
            return new ArrayList<>();
        }

        return iteratorToChokes(path);
    }

    private static CPPath definePath() {
        AUnit enemy = EnemyUnits.nearestEnemyBuilding();

        if (enemy == null || !enemy.hasPosition()) {
            return null;
        }

        CPPath path = AMap.getMap().getPath(Select.ourBuildings().first().position().p(), enemy.position().p());
        return path;
    }

    private static ArrayList<AChoke> iteratorToChokes(CPPath path) {
        ArrayList<AChoke> chokes = new ArrayList<>();

        int chokeIndex = 0;
        for (Iterator<ChokePoint> iterator = path.iterator(); iterator.hasNext(); ) {
            AChoke choke = AChoke.from(iterator.next());
            choke.setPathToEnemyBaseIndex(chokeIndex);
            chokes.add(choke);

            chokeIndex++;
        }
        return chokes;
    }
}
