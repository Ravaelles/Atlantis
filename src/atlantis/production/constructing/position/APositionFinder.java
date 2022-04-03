package atlantis.production.constructing.position;

import atlantis.combat.micro.zerg.ZergCreepColony;
import atlantis.game.AGame;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructing.Construction;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.cache.Cache;
import atlantis.util.We;

public class APositionFinder {

    public static Cache<APosition> cache = new Cache<>();

    // =========================================================

    public static void clearCache() {
        cache.clear();
    }

    /**
     * Returns build position for next building of given type.
     */
    public static APosition findPositionForNew(AUnit builder, AUnitType building, Construction construction) {
        HasPosition near = construction != null ? construction.nearTo() : null;
        double maxDistance = construction != null ? construction.maxDistance() : 35;

        String modifier = construction.productionOrder().getModifier();
        if (modifier != null) {
//            AAdvancedPainter.paintCircleFilled(
//                PositionModifier.toPosition(modifier, building, builder, construction),
//                24, Color.Brown
//            );
//            GameSpeed.pauseGame();
            near = PositionModifier.toPosition(modifier, building, builder, construction);
        }


        return findPositionForNew(builder, building, construction, near, maxDistance);
    }

    /**
     * Returns build position for next building of given type. If <b>nearTo</b> is not null, it forces to find
     * position
     * <b>maxDistance</b> build tiles from given position.
     */
    public static APosition findPositionForNew(
            AUnit builder, AUnitType building,
            Construction construction,
            HasPosition nearTo, double maxDistance
    ) {
//        totalRequests++;

        if (building.isBunker() && maxDistance <= 5) {
            maxDistance = 10;
        }

        construction.setMaxDistance(maxDistance);

        // =========================================================
        // GAS extracting buildings

        if (building.isGasBuilding()) {
            return ASpecialPositionFinder.findPositionForGasBuilding(building);
        } 

        // =========================================================
        // BASE

        else if (building.isBase()) {
            if (We.zerg()) {
                if (Count.larvas() == 0 || Count.bases() >= 3) {
                    return findStandardPosition(builder, building, nearTo, 30);
                }
            }

            return ASpecialPositionFinder.findPositionForBase(building, builder, construction);
        }

        // =========================================================
        // BUNKER

        else if (building.isBunker()) {
            return TerranBunkerPositionFinder.findPosition(builder, construction);
        }

        // =========================================================
        // Creep colony

        else if (building.is(AUnitType.Zerg_Creep_Colony)) {
            return ZergCreepColony.findPosition(building, builder, construction);
        } 

        // =========================================================
        // STANDARD BUILDINGS
        else {

            // If we didn't specify location where to build, build somewhere near the main base
            if (nearTo == null) {
                if (AGame.isPlayingAsZerg()) {
                    nearTo = Select.main().position();
                }
                else {
                    if (Count.bases() >= 3) {
                        nearTo = Select.ourBases().random();
                    } else {
                        nearTo = Select.main().position();
                    }
                }
            }

            // If all of our bases have been destroyed, build somewhere near our first unit alive
            if (nearTo == null) {
                nearTo = Select.our().first().position();
            }

            // Hopeless case, all units have died, just quit.
            if (nearTo == null) {
                return null;
            }

            if (maxDistance < 0) {
                maxDistance = 50;
            }

            // =========================================================
            // Standard place
            
            return findStandardPosition(builder, building, nearTo, maxDistance);
        }
    }
    
    // =========================================================
    
    /**
     * Returns standard build position for building near given position.
     */
    public static APosition findStandardPosition(AUnit builder, AUnitType building, HasPosition nearTo, double maxDistance) {
        return cache.get(
                "findStandardPosition:" + building + "," + nearTo + "," + builder,
                41,
                () -> {
                    // ===========================================================
                    // = Handle standard building position according to the race =
                    // = as every race uses completely different approach        =
                    // ===========================================================

                    // Terran
                    if (AGame.isPlayingAsTerran()) {
                        return TerranPositionFinder.findStandardPositionFor(builder, building, nearTo, maxDistance);
                    }

                    // Protoss
                    else if (AGame.isPlayingAsProtoss()) {
                        return ProtossPositionFinder.findStandardPositionFor(builder, building, nearTo, maxDistance);
                    }

                    // Zerg
                    else if (AGame.isPlayingAsZerg()) {
                        return ZergPositionFinder.findStandardPositionFor(builder, building, nearTo, maxDistance);
                    }

                    else {
                        System.err.println("Invalid race: " + AGame.getPlayerUs().getRace());
                        System.exit(-1);
                        return null;
                    }
                }
        );
    }

}
