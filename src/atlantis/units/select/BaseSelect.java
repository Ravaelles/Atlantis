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

//    private static final Cache<List<AUnit>> cacheList = new Cache<>();

    protected static List<AUnit> ourUnits() {
//        return cacheList.get(
//                "ourUnits",
//                1,
//                () -> {
                    List<AUnit> data = new ArrayList<>();

                    for (Unit u : AGame.getPlayerUs().getUnits()) {
                        data.add(AUnit.createFrom(u));
                    }

                    //                    System.out.println("------------");
                    //                    for (AUnit unit : data) {
                    //                        System.out.println(unit);
                    //                    }

                    return data;
//                }
//        );
    }

    protected static List<AUnit> enemyUnits() {
//        return cacheList.get(
//                "enemyUnits",
//                0,
//                () -> {
                    List<AUnit> data = new ArrayList<>();

                    // === Handle UMS ==========================================

                    if (AGame.isUms()) {
                        Player playerUs = AGame.getPlayerUs();
                        for (Player player : AGame.getPlayers()) {
                            if (player.isEnemy(playerUs)) {
                                for (Unit u : player.getUnits()) {
                                    AUnit unit = AUnit.createFrom(u);
                                    if (unit.isAlive()) {
                                        data.add(unit);
                                    }
                                    //                                    else {
                                    //                                        System.err.println("Enemy unit not alive? Seems a terrible problem.");
                                    //                                        System.err.println(unit);
                                    //                                        System.err.println(unit.hp() + " // " + unit.isVisibleOnMap() + " // " + unit.effVisible());
                                    //                                    }
                                }
                            }
                        }
                    }

                    // =========================================================

                    else {
                        for (Unit u : AGame.getEnemy().getUnits()) {
                            AUnit unit = AUnit.createFrom(u);
                            if (unit.isAlive()) {
                                data.add(unit);
                            }
                        }
                    }

                    return data;
//                }
//        );
    }

    protected static List<AUnit> neutralUnits() {
//        return cacheList.get(
//                "neutralUnits",
//                3,
//                () -> {
                    List<AUnit> data = new ArrayList<>();

                    for (Unit u : Atlantis.game().neutral().getUnits()) {
                        AUnit unit = AUnit.createFrom(u);
                        if (unit.isAlive()) {
                            data.add(unit);
                        }
                    }

                    return data;
//                }
//        );
    }

    protected static List<AUnit> allUnits() {
//        return cacheList.get(
//                "allUnits",
//                0,
//                () -> {
                    List<AUnit> data = new ArrayList<>();

                    for (Unit u : Atlantis.game().getAllUnits()) {
                        AUnit unit = AUnit.createFrom(u);
                        if (unit.isAlive()) {
                            data.add(unit);
                        }
                    }

                    return data;
//                }
//        );
    }

}
