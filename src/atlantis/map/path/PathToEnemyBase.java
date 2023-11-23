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
import java.util.Iterator;

public class PathToEnemyBase {
    private static Cache<Object> cache = new Cache<>();
    private static Cache<ArrayList<AChoke>> cacheChokes = new Cache<>();

    public static ArrayList<AChoke> chokesLeadingToEnemyBase() {
        AUnit enemy = enemy();

        if (enemy == null) return null;

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
}
