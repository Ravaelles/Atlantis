package atlantis.map.path;

import atlantis.information.enemy.EnemyUnits;
import atlantis.map.AMap;
import atlantis.map.choke.AChoke;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.util.cache.Cache;
import atlantis.util.cache.CacheKey;
import bwem.CPPath;
import bwem.ChokePoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class PathToEnemyBase {
    private static Cache<Object> cache = new Cache<>();
    private static Cache<ArrayList<AChoke>> cacheChokes = new Cache<>();

    public static ArrayList<AChoke> chokesLeadingToEnemyBase() {
        AUnit enemy = enemy();

        if (enemy == null) return new ArrayList<>();

        return cacheChokes.get(
            "chokesLeadingToEnemyBase:" + CacheKey.toKey(enemy),
            -1,
            () -> {
                CPPath path = definePathToEnemy(Select.mainOrAnyBuilding());

                if (path == null) {
                    return new ArrayList<>();
                }

                return iteratorToChokes(path);
            }
        );
    }

    private static AUnit enemy() {
        return EnemyUnits.nearestEnemyBuilding();
    }

    protected static CPPath definePathToEnemy(HasPosition from) {
        AUnit enemy = enemy();

        if (enemy == null || !enemy.hasPosition() || from == null) {
            return null;
        }

        CPPath path = AMap.getMap().getPath(from.position().p(), enemy.position().p());
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

    public static AChoke oneChokeCloserToMain(HasPosition closerThanPosition) {
        ArrayList<AChoke> chokes = chokesLeadingToEnemyBase();
        if (chokes == null || chokes.isEmpty()) {
            return null;
        }

        APosition main = Select.mainOrAnyBuildingPosition();
        if (main == null) return null;

        double givenDistToMain = main.distTo(closerThanPosition);

        Collections.reverse(chokes);

        for (AChoke choke : chokes) {
            if (choke.distTo(closerThanPosition) >= 8 && main.distTo(choke) <= givenDistToMain) {
                return choke;
            }
        }

        return null;
    }

    public static boolean isKnown() {
        return chokesLeadingToEnemyBase().size() >= 2;
    }
}
