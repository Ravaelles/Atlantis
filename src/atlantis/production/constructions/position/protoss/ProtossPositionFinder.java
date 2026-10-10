package atlantis.production.constructions.position.protoss;

import atlantis.game.A;
import atlantis.map.AMap;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructions.position.AbstractPositionFinder;
import atlantis.production.constructions.position.PositionFulfillsAllConditions;
import atlantis.production.constructions.position.conditions.can_build_here.IsPoweredByAPylon;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;

public class ProtossPositionFinder extends AbstractPositionFinder {

    /**
     * Returns best position for given <b>building</b>, maximum <b>maxDistance</b> build tiles from
     * <b>nearTo</b>
     * position.<br />
     * It checks if buildings aren't too close one to another and things like that.
     */
    /**
     * Finds a standard position for a Protoss building.
     *
     * <p>
     * <b>Pylon power is checked here, in the Protoss layer, on purpose</b> (owner's
     * ruling, 2026-10-10). It is a race rule - Terran and Zerg have no equivalent -
     * so putting it in the shared {@code PositionFulfillsAllConditions} would make a
     * race-specific constraint every race has to carry and would hide which race it
     * belongs to. This is the one place the Protoss standard search decides, so the
     * rule lives with the race that has it.
     * </p>
     *
     * <p>
     * <b>Why this is not the same as the check that already existed:</b>
     * {@code ProtossAllowHereEarlyEvenWithoutRequirements} does test power, but it is
     * only reached for the buildings {@code AllowToProduceEarlyWithoutRequirements}
     * admits and it is restricted to gateways, and {@code PsiGating} belongs to the
     * rewritten planner that only runs with {@code PLACEMENT=catalogue}. The
     * condition chain below, {@code PositionFulfillsAllConditions}, never checked
     * power at all - so with the default planner a Cybernetics Core could be sent to
     * a tile no Pylon covered (owner report, IDE game, 2026-10-10).
     * </p>
     *
     * <p>
     * A building that <b>provides</b> power (the Pylon itself) is not constrained by
     * it; neither is a building whose whole job is outside power (a Cannon may
     * legitimately sit unpwered, and the game allows a Pylon to be placed to power
     * it later), so those are left to the other conditions.
     * </p>
     */
    public static APosition findStandardPositionFor(AUnit builder, AUnitType building, HasPosition nearTo, double maxDistance) {
        _STATUS = "None";

        // =========================================================

        if (builder == null) {
            AbstractPositionFinder._STATUS = "NO BUILDER ASSIGNED";
            return null;
        }

        // =========================================================

//        int searchRadius = (building.isBase() || building.isCombatBuilding()) ? 0 : 1;
        int searchRadius = 0;

//        boolean logToFile = building.isGateway();
//        if (logToFile) LogToFile.info("------------\n");

        int xMapMax = AMap.getMapWidthInTiles() - 1;
        int yMapMax = AMap.getMapHeightInTiles() - 1;

//        System.err.println("maxDistance = " + maxDistance);

        while (searchRadius < maxDistance) {
            int xMin = Math.max(0, nearTo.tx() - searchRadius);
            int xMax = Math.min(xMapMax, nearTo.tx() + searchRadius);
            int yMin = Math.max(0, nearTo.ty() - searchRadius);
            int yMax = Math.min(yMapMax, nearTo.ty() + searchRadius);

            for (int tileX = xMin; tileX <= xMax; tileX++) {
                for (int tileY = yMin; tileY <= yMax; tileY++) {
                    if (tileX == xMin || tileY == yMin || tileX == xMax || tileY == yMax) {
//                        if (logToFile) LogToFile.info("tx,ty: [" + tileX + "," + tileY + "]\n");

                        APosition constructionPosition = APosition.create(tileX, tileY);
//                        System.err.println("constructionPosition = " + constructionPosition + " / " + _CONDITION_THAT_FAILED);

                        // Race rule, checked before the shared chain (see the javadoc):
                        // a Protoss building that needs power must stand inside a
                        // finished Pylon's field. A Pylon powers its own tile and does
                        // not depend on one, and a Cannon may legitimately sit
                        // unpowered until a Pylon is placed for it.
                        if (
                            building.needsPower()
                                && !building.isPylon()
                                && !building.isCannon()
                                && !IsPoweredByAPylon.check(constructionPosition)
                        ) {
                            AbstractPositionFinder._STATUS = "Not powered by a Pylon";
                            continue;
                        }

                        if (PositionFulfillsAllConditions.doesPositionFulfillAllConditions(
                            builder, building, constructionPosition, nearTo
                        )) {
                            if (building.isCombatBuilding()) {
                                // Turret fix - make sure to build in the same region
                                if (constructionPosition.groundDistanceTo(nearTo) > 1.6 * searchRadius) {
                                    continue;
                                }
                            }

                            AbstractPositionFinder._STATUS = "OK";
                            return constructionPosition;
                        }

//                        if (building.isPylon() && A.supplyUsed() <= 18) {
//                            System.out.println("Fail: " + _STATUS);
//                        }
                    }
                }
            }

            searchRadius++;
        }

        return null;
    }
}
