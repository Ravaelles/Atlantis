package atlantis.map.path;

import atlantis.information.enemy.EnemyUnits;
import atlantis.map.AMap;
import atlantis.map.choke.AChoke;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import bwem.CPPath;
import bwem.ChokePoint;

import java.util.ArrayList;
import java.util.Iterator;

public class PathToEnemyBase {
    public static ArrayList<AChoke> chokesLeadingToEnemyBase() {
        CPPath path = definePath();

        if (path == null) {
            return new ArrayList<>();
        }

        return iteratorToChokes(path);
    }

    private static CPPath definePath() {
        AUnit enemy = EnemyUnits.nearestEnemyBuilding();
        AUnit ourBuilding = Select.ourBuildings().first();

        if (enemy == null || !enemy.hasPosition() || ourBuilding == null) {
            return null;
        }

        CPPath path = AMap.getMap().getPath(ourBuilding.position().p(), enemy.position().p());
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