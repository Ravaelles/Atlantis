package atlantis.combat.missions.contain;

import atlantis.combat.missions.AFocusPoint;
import atlantis.combat.missions.MissionFocusPoint;
import atlantis.enemy.EnemyInformation;
import atlantis.enemy.EnemyUnits;
import atlantis.information.AbstractFoggedUnit;
import atlantis.map.AChoke;
import atlantis.map.Bases;
import atlantis.map.Chokes;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.util.Cache;
import atlantis.util.We;

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
                        AbstractFoggedUnit enemyBuilding = EnemyUnits.nearestEnemyBuilding();
                        if (
                                enemyBuilding != null
                                        && enemyBuilding.position() != null
                                        && EnemyInformation.isProxyBuilding(enemyBuilding)
                        ) {
                            return new AFocusPoint(
                                    enemyBuilding,
                                    Select.main()
                            );
                        }
//                        }
                    }

                    if (EnemyInformation.hasDefensiveLandBuilding(true) && We.haveBase()) {
                        AUnit nearestCombatBuilding = EnemyUnits.foggedUnits()
                                .combatBuildings(false)
                                .nearestTo(Select.main());
                        if (nearestCombatBuilding != null) {
                            AChoke choke = Chokes.nearestChoke(nearestCombatBuilding);
                            if (choke != null) {
                                return new AFocusPoint(
                                    choke.position(),
                                    Select.main()
                                );
                            }
                        }
                    }

                    AChoke mainChoke = Chokes.enemyMainChoke();
                    APosition enemyNatural = Bases.enemyNatural();
                    if (enemyNatural != null) {
                        if (mainChoke != null) {
                            return new AFocusPoint(
                                    enemyNatural.translatePercentTowards(mainChoke, 40),
                                    enemyNatural
                            );
                        }
                        return new AFocusPoint(
                                enemyNatural,
                                Select.main()
                        );
                    }

                    AChoke naturalChoke = Chokes.enemyNaturalChoke();
                    if (naturalChoke != null && naturalChoke.width() <= 4) {
                        return new AFocusPoint(
                                naturalChoke,
                                Select.main()
                        );
                    }

//                    if (mainChoke != null && mainChoke.getWidth() <= 4) {
//                        return mainChoke.position();
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

                    APosition enemyBase = EnemyUnits.enemyBase();
                    if (enemyBase != null && enemyBase.position() != null) {
                        return new AFocusPoint(
                                enemyBase,
                                Select.main()
                        );
//                        return containPointIfEnemyBaseIsKnown(enemyBase);
                    }

//                    AChoke mainChoke = Chokes.enemyMainChoke();
//        if (mainChoke != null) {
//            return mainChoke.position();
//        }

                    // Try to go to some starting location, hoping to find enemy there.
                    if (Select.main() != null) {
                        AChoke choke = Chokes.nearestChoke(
                                Bases.nearestUnexploredStartingLocation(Select.main().position())
                        );

                        if (choke == null) {
                            return null;
                        }

                        return new AFocusPoint(
                                choke,
                                Select.main()
                        );
                    }

                    return null;
                }
        );
    }

    // =========================================================

//    private APosition containPointIfEnemyBaseIsKnown(APosition enemyBase) {
//        AChoke chokepoint = Chokes.natural(enemyBase);
//        if (chokepoint != null) {
////            CameraManager.centerCameraOn(chokepoint.getCenter());
//            return chokepoint.center();
//        }
//
//        ABaseLocation natural = Bases.natural(enemyBase.position());
//        if (natural != null) {
////            CameraManager.centerCameraOn(natural);
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
