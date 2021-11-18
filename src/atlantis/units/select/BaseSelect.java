package atlantis.units.select;

import atlantis.AGame;
import atlantis.Atlantis;
import atlantis.units.AUnit;
import atlantis.util.Cache;
import bwapi.Player;
import bwapi.Unit;

import java.util.ArrayList;
import java.util.List;

public class BaseSelect<T extends AUnit> {

    private static final Cache<List<AUnit>> cacheList = new Cache<>();

    protected static List<AUnit> ourUnits() {
        return cacheList.get(
            "ourUnits",
            0,
            () -> {
                List<AUnit> data = new ArrayList<>();

                for (Unit u : AGame.getPlayerUs().getUnits()) {
                    AUnit unit = AUnit.createFrom(u);
                    if (unit.isAlive()) {
                        data.add(unit);
                    }
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
                    data.add(AUnit.createFrom(u));
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

                // === Handle UMS ==========================================

                if (AGame.isUms()) {
                    Player playerUs = AGame.getPlayerUs();
                    for (Player player : AGame.getPlayers()) {
                        if (player.isEnemy(playerUs)) {
                            for (Unit u : player.getUnits()) {
                                AUnit unit = AUnit.createFrom(u);
                                data.add(unit);
                            }
                        }
                    }
                }

                // =========================================================

                else {
                    for (Unit u : AGame.getEnemy().getUnits()) {
                        AUnit unit = AUnit.createFrom(u);
                        if (!unit.isLarvaOrEgg()) {
                            data.add(unit);
                        }
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
                        AUnit unit = AUnit.createFrom(u);
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
                        AUnit unit = AUnit.createFrom(u);
                        data.add(unit);
                    }

                    return data;
                }
        );
    }

}
