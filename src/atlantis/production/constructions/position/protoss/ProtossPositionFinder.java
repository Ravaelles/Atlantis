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
    /**
     * Does this Protoss building need to stand inside a finished Pylon's power
     * field?
     *
     * <p>
     * <b>Exactly three Protoss buildings do not</b> (owner's ruling, 2026-10-10):
     * the <b>Pylon</b> itself (it is the power source), the <b>Nexus</b> (it is the
     * base - it cannot depend on something built from itself) and the
     * <b>Assimilator</b> (gas is taken from a geyser the Nexus already touches, and
     * the game lets it be built unpowered). Everything else Protoss, including the
     * Photon Cannon, needs power.
     * </p>
     *
     * <p>
     * Written out here in full rather than delegating to
     * {@code AUnitType.needsPower()} alone. That method already excludes these
     * three - and it also excludes a Cannon, on the reasoning that a Cannon may sit
     * unpowered until a Pylon arrives for it. The placement rule the owner wants is
     * stricter for a Cannon, so the two questions are deliberately not the same
     * one, and a change to {@code needsPower()} must not silently change placement.
     * </p>
     */
    private static boolean needsPylonPower(AUnitType building) {
        if (!building.isProtoss()) return false;

        return !building.is(
            AUnitType.Protoss_Pylon,
            AUnitType.Protoss_Nexus,
            AUnitType.Protoss_Assimilator
        );
    }

    public static APosition findStandardPositionFor(AUnit builder, AUnitType building, HasPosition nearTo, double maxDistance) {
        _STATUS = "None";

        // TEMPORARY diagnostic (owner's report: the third Pylon is never built): count why
        // each candidate tile was refused, print once when the whole search fails. Remove
        // after the cause is named.
        java.util.Map<String, Integer> reasons = building.isPylon() ? new java.util.HashMap<>() : null;
        java.util.Map<String, Integer> finalStatus = reasons;

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
                        // finished Pylon's field.
                        if (needsPylonPower(building) && !IsPoweredByAPylon.check(constructionPosition)) {
                            AbstractPositionFinder._STATUS = "Not powered by a Pylon";
                            if (reasons != null) reasons.merge(AbstractPositionFinder._STATUS, 1, Integer::sum);
                            continue;
                        }

                        if (!PositionFulfillsAllConditions.doesPositionFulfillAllConditions(
                            builder, building, constructionPosition, nearTo
                        )) {
                            if (reasons != null && AbstractPositionFinder._STATUS != null) {
                                reasons.merge(AbstractPositionFinder._STATUS, 1, Integer::sum);
                            }
                            continue;
                        }

                        // Edge-margin rule: honour the relaxation ONLY when it is the Pylon
                        // fallback sweep asking (see the fallback javadoc). The condition
                        // lives here because the margin class must keep its unconditional
                        // 7-tile pin for the unit test.
                        if (ProtossTooCloseToMapBoundaries.isTooClose(building, constructionPosition)
                            && !(ProtossPositionFinder.relaxedBoundaryMargin && building.isPylon())) {
                            if (reasons != null) reasons.merge(AbstractPositionFinder._STATUS, 1, Integer::sum);
                            continue;
                        }

                        if (building.isCombatBuilding()) {
                            // Turret fix - make sure to build in the same region
                            if (constructionPosition.groundDistanceTo(nearTo) > 1.6 * searchRadius) {
                                continue;
                            }
                        }

                        AbstractPositionFinder._STATUS = "OK";
                        lastFound = constructionPosition;
                        return constructionPosition;

//                        if (building.isPylon() && A.supplyUsed() <= 18) {
//                            System.out.println("Fail: " + _STATUS);
//                        }
                    }
                }
            }

            searchRadius++;
        }

        if (reasons != null && !reasons.isEmpty()) {
            System.err.println("PYLON_FIND_FAILED near=" + nearTo + " maxDist=" + maxDistance
                + " supply=" + A.supplyUsed() + "/" + A.supplyTotal()
                + " reasons=" + reasons);
        }

        // Pylon-only fallback (measured on TauCross: a corner Nexus at [119,10] with a
        // 7-tile edge margin leaves ZERO candidates that also survive the street grid -
        // ~3000 consecutive refusals, PYLON_FIND_FAILED with supply 20/25). The relaxed
        // margin must NOT be the default (ProtossTooCloseToMapBoundariesTest pins the
        // 7-tile margin unconditionally), so it lives here as a SECOND PASS that runs only
        // when the normal search found nothing.
        if (building.isPylon()) {
            _STATUS = "Second pass (relaxed edge margin)";
            if (findWithRelaxedBoundaryMargin(builder, building, nearTo, maxDistance)) {
                return lastFoundPosition();
            }
        }

        return null;
    }

    /**
     * One full sweep of the same search with the Pylon edge margin relaxed to 4 tiles.
     * Sets _STATUS and stores the position when a tile passes.
     */
    private static boolean findWithRelaxedBoundaryMargin(
        AUnit builder, AUnitType building, HasPosition nearTo, double maxDistance
    ) {
        relaxedBoundaryMargin = true;
        try {
            APosition found = findStandardPositionFor(builder, building, nearTo, maxDistance);
            return found != null;
        }
        finally {
            relaxedBoundaryMargin = false;
        }
    }

    /**
     * True only during the Pylon fallback sweep (see the javadoc above) - it is what
     * {@code ProtossTooCloseToMapBoundaries} reads to relax its 7-tile margin to 4.
     * Thread-local not needed: the finder is single-threaded per frame.
     */
    public static boolean relaxedBoundaryMargin = false;

    private static APosition lastFoundPosition() {
        return lastFound;
    }

    private static APosition lastFound;
}
