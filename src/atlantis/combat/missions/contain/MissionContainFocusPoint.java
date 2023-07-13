package atlantis.combat.missions.contain;

import atlantis.combat.advance.focus.AFocusPoint;
import atlantis.combat.advance.focus.MissionFocusPoint;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.map.AChoke;
import atlantis.map.Bases;
import atlantis.map.Chokes;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.util.We;
import atlantis.util.cache.Cache;

public class MissionContainFocusPoint extends MissionFocusPoint {

    private Cache<AFocusPoint> cache = new Cache<>();

    @Override
    public AFocusPoint focusPoint() {
        return cache.get(
            "focusPoint",
            30,
            () -> {
                if (We.terran() && We.haveBase()) {
//                        if (!EnemyInformation.hasDefensiveLandBuilding()) {
                    AUnit enemyBuilding = EnemyUnits.nearestEnemyBuilding();
                    if (
                        enemyBuilding != null
                            && enemyBuilding.position() != null
                            && EnemyInfo.isProxyBuilding(enemyBuilding)
                    ) {
                        return new AFocusPoint(
                            enemyBuilding,
                            Select.main(),
                            "NearestEnemyBuilding"
                        );
                    }
//                        }
                }

//                if (EnemyInfo.hasDefensiveLandBuilding(true) && We.haveBase()) {
//                    AUnit nearestCombatBuilding = EnemyUnits.foggedUnits()
//                        .combatBuildings(false)
//                        .nearestTo(Select.main());
//                    if (nearestCombatBuilding != null && nearestCombatBuilding.hasPosition()) {
//                        AChoke choke = Chokes.nearestChoke(nearestCombatBuilding);
//                        if (choke != null) {
//                            return new AFocusPoint(
//                                choke.position(),
//                                Select.main(),
//                                "DefensiveBuilding"
//                            );
//                        }
//                    }
//                }

                AChoke naturalChoke = Chokes.enemyNaturalChoke();
                if (naturalChoke != null && naturalChoke.width() <= 4) {
                    return new AFocusPoint(
                        naturalChoke,
                        Select.main(),
                        "EnemyNaturalChoke"
                    );
                }

//                AChoke enemyMainChoke = Chokes.enemyMainChoke();
                APosition enemyNatural = Bases.enemyNatural();
                if (enemyNatural != null) {
//                    AChoke enemyNaturalChoke = Chokes.enemyNaturalChoke();
//                    if (enemyNaturalChoke != null) {
//                        return new AFocusPoint(
//                            enemyNatural.translatePercentTowards(enemyNaturalChoke, 40),
//                            enemyNatural,
//                            "EnemyNaturalChoke"
//                        );
//                    }
                    return new AFocusPoint(
                        enemyNatural,
                        Select.main(),
                        "EnemyNatural"
                    );
                }


//                    if (enemyMainChoke != null && enemyMainChoke.getWidth() <= 4) {
//                        return enemyMainChoke.position();
//                    }

//                    FoggedUnit enemyBuilding = EnemyUnits.nearestEnemyBuilding();
//                    if (enemyBuilding != null) {
//                        return enemyBuilding.position();
//                    }
////
//        AUnit nearestEnemy = Select.enemy().nearestTo(Select.our().first());
//        if (nearestEnemy != null) {
//            return nearestEnemy.position();
//        }

                AUnit enemyBase = EnemyUnits.enemyBase();
                if (enemyBase != null && enemyBase.hasPosition()) {
                    return new AFocusPoint(
                        enemyBase,
                        Select.main(),
                        "EnemyBase"
                    );
//                        return containPointIfEnemyBaseIsKnown(enemyBase);
                }

//                    AChoke enemyMainChoke = Chokes.enemyMainChoke();
//        if (enemyMainChoke != null) {
//            return enemyMainChoke.position();
//        }

                // Try to go to some starting location, hoping to find enemy there.
                if (Select.main() != null) {
                    APosition unexploredBase = Bases.nearestUnexploredStartingLocation(Select.main().position());

                    if (unexploredBase != null) {
                        return new AFocusPoint(
                            unexploredBase,
                            Select.main(),
                            "UnexplStartLocat"
                        );
                    }

//                    AChoke choke = Chokes.nearestChoke(
//                        Bases.nearestUnexploredStartingLocation(Select.main().position())
//                    );
//
//                    if (choke == null) {
//                        return null;
//                    }
//
//                    return new AFocusPoint(
//                        choke,
//                        Select.main(),
//                        "UnexploredStartLocation"
//                    );
                }

                return null;
            }
        );
    }

    // =========================================================

//    private APosition containPointIfEnemyBaseIsKnown(APosition enemyBase) {
//        AChoke chokepoint = Chokes.natural(enemyBase);
//        if (chokepoint != null) {
////            CameraCommander.centerCameraOn(chokepoint.getCenter());
//            return chokepoint.center();
//        }
//
//        ABaseLocation natural = Bases.natural(enemyBase.position());
//        if (natural != null) {
////            CameraCommander.centerCameraOn(natural);
//            return natural.position();
//        }
//
//        System.err.println("Shouldnt be here mate?");
//        return null;
//    }

//    private APosition containPointIfEnemyBaseNotKnown() {
//        AUnit nearestEnemy = Select.enemy().nearestTo(Select.our().first());
//        if (nearestEnemy != null) {
//            return nearestEnemy.getPosition();
//        }
//
//        return null;
//
////        AUnit mainBase = Select.mainBase();
////        if (mainBase == null) {
////            return null;
////        }
////
////        AChoke choke = AMap.getChokepointForNatural(mainBase.getPosition());
////        return choke == null ? null : choke.getCenter();
//    }

}
