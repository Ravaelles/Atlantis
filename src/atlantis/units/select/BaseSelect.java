package atlantis.units.select;

import atlantis.Atlantis;
import atlantis.game.AGame;
import atlantis.game.APlayer;
import atlantis.units.AUnit;
import atlantis.util.cache.Cache;
import atlantis.util.Enemy;
import bwapi.Player;
import bwapi.Unit;

import java.util.ArrayList;
import java.util.List;

public class BaseSelect<T extends AUnit> {

    protected static final Cache<List<AUnit>> cacheList = new Cache<>();

    public static List<AUnit> ourUnits() {
        return cacheList.get(
            "ourUnits",
            0,
            () -> {
                List<AUnit> data = new ArrayList<>();

                for (Unit u : AGame.getPlayerUs().getUnits()) {
                    AUnit unit = AUnit.getById(u);
                    data.add(unit);
                }

                return data;
            }
        );
    }

    public static List<AUnit> ourWithUnfinishedUnits() {
        return cacheList.get(
            "ourWithUnfinishedUnits",
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

    public static List<AUnit> enemyUnits() {
        return cacheList.get(
            "enemyUnits",
            0,
            () -> {
                List<AUnit> data = new ArrayList<>();

                for (APlayer player : Enemy.players()) {
                    for (Unit u : player.getUnits()) {
                        AUnit unit = AUnit.getById(u);
                        data.add(unit);
                    }
                }

                return data;
            }
        );
    }

    public static List<AUnit> neutralUnits() {
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

    public static List<AUnit> allUnits() {
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
