package atlantis.units.select;

import atlantis.AGame;
import atlantis.Atlantis;
import atlantis.units.AUnit;
import atlantis.util.A;
import atlantis.util.Cache;
import atlantis.util.Enemy;
import bwapi.Player;
import bwapi.Unit;

import java.util.ArrayList;
import java.util.List;

public class BaseSelect<T extends AUnit> {

    protected static final Cache<List<AUnit>> cacheList = new Cache<>();

    protected static List<AUnit> ourUnits() {
        return cacheList.get(
            "ourUnits",
            0,
            () -> {
                List<AUnit> data = new ArrayList<>();

                for (Unit u : AGame.getPlayerUs().getUnits()) {
                    AUnit unit = AUnit.getById(u);
//                    if (A.seconds() >= 200) {
//                        System.out.println(u.getType().name() + " // " + u.getType().isBuilding());
//                    }
                    data.add(unit);
//                    if (unit.isAlive()) {
//                    }
                }

                return data;
            }
        );
    }

    protected static List<AUnit> ourIncludingUnfinishedUnits() {
        return cacheList.get(
            "ourIncludingUnfinishedUnits",
            0,
            () -> {
                List<AUnit> data = new ArrayList<>();

                for (Unit u : AGame.getPlayerUs().getUnits()) {
                    AUnit unit = AUnit.getById(u);
                    if (unit.isAlive()) {
                        data.add(unit);
                    }
                }

                return data;
            }
        );
    }

    protected static List<AUnit> enemyUnits() {
        return cacheList.get(
            "enemyUnits",
            0,
            () -> {
                List<AUnit> data = new ArrayList<>();

                for (Player player : Enemy.players()) {
                    for (Unit u : player.getUnits()) {
                        AUnit unit = AUnit.getById(u);
                        data.add(unit);
                    }
                }

                return data;
            }
        );
    }

    protected static List<AUnit> neutralUnits() {
        return cacheList.get(
                "neutralUnits",
                0,
                () -> {
                    List<AUnit> data = new ArrayList<>();

                    for (Unit u : Atlantis.game().neutral().getUnits()) {
                        AUnit unit = AUnit.getById(u);
                        data.add(unit);
                    }

                    return data;
                }
        );
    }

    protected static List<AUnit> allUnits() {
        return cacheList.get(
                "allUnits",
                0,
                () -> {
                    List<AUnit> data = new ArrayList<>();

                    for (Unit u : Atlantis.game().getAllUnits()) {
                        AUnit unit = AUnit.getById(u);
                        data.add(unit);
                    }

                    return data;
                }
        );
    }

    public static void clearCache() {
        cacheList.clear();
    }
}
