package atlantis.production.constructions.builders;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.production.constructions.position.conditions.can_build_here.CanPhysicallyBuildHere;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.HasUnit;
import atlantis.units.select.Select;
import atlantis.util.AConsole;
import atlantis.util.log.ErrorLog;
import atlantis.util.log.EventLog;
import atlantis.util.log.LogKey;

public class GasBuildingFix extends HasUnit {
    public GasBuildingFix(AUnit unit) {
        super(unit);
    }

    /**
     * From reasons impossible to explain sometimes it happens that we need to build Extractor
     * one tile left from the top left position. Sometimes not. This method takes care of
     * these cases and ensures the position is valid.
     *
     * <p>Public as the gas-tile seam: a refinery is only ever built on its geyser's
     * own tile, and tests pin that contract here rather than through a game.</p>
     */
    public APosition applyGasBuildingFixIfNeeded(APosition position, AUnitType building) {
        if (position == null) {
            AConsole.errPrintln("Gas building FIX got NULL");

            return null;
        }

        // The fix path is measured as one structured row, not console
        // lines: which tile came in, geyser proximity, what came out.
        // (The temporary GAS_FIX prints named the 3:44 Assimilator loop;
        // the twin below carries the same numbers.)

        // Fix geyser position problem: snap to the geyser's OWN tile (see
        // GasBuildingPositionFinder - the engine only accepts exactly it).
        if (building.isGasBuilding() && Select.geysers().inRadius(2, position).isEmpty()) {
            AUnit geyser = Select.geysers().inRadius(12, position).nearestTo(position);
            if (geyser != null && geyser.tilePosition() != null) {
                position = APosition.create(geyser.tilePosition());
            }
        }

        if (building.isGasBuilding() && Select.geysers().inRadius(3, position).isEmpty()) {
            AUnit geyser;

            ErrorLog.printMaxOncePerMinute(
                "There are no geysers in radius 3 of " + position + " for " + building
                    + "\nThis indicates a problem with initial position of the gas building."
            );

            geyser = Select.geysers().nearestTo(position);
            if (geyser != null) {
                ErrorLog.printErrorOnce("Nearest geyser dist = " + geyser.distTo(position)
                    + " / gey:" + geyser
                    + " / pos:" + position
                );
            }
            else {
                ErrorLog.printErrorOnce("No geyser is known!");
            }

            AConsole.errPrintln("Gas building FIX got position too far from geyser");
            geyser = Select.geysers().inRadius(10, position).nearestTo(position);

            if (geyser == null || geyser.tilePosition() == null) {
                logFix(position, building, "no-geyser");

                return null;
            }

            position = APosition.create(geyser.tilePosition());
        }

        // =========================================================

        if (CanPhysicallyBuildHere.check(unit, building, position)) {
            logFix(position, building, "ok");

            return position;
        }

        // === Loop ================================================

        int radius = 1;
        while (radius <= 4) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    if (dx == 0 && dy == 0) continue;

                    if (CanPhysicallyBuildHere.check(
                        unit, building, position.translateByTiles(dx, dy))
                    ) {
                        logFix(position, building, "shifted");

                        return position.translateByTiles(dx, dy);
                    }
                }
            }

            radius++;
        }

        logFix(position, building, "unshifted");

        return position;
    }

    private void logFix(APosition position, AUnitType building, String result) {
        if (!building.isGasBuilding()) return;

        AUnit geyserNear = Select.geysers().inRadius(6, position).nearestTo(position);
        EventLog.key(LogKey.GAS_FIX)
            .param("pos", position.toString())
            .param("geysersIn6", Select.geysers().inRadius(6, position).count())
            .param("nearest", geyserNear == null ? "none" : geyserNear.position().toString())
            .param("nearestDist", geyserNear == null ? "-" : A.digit(geyserNear.distTo(position)))
            .param("result", result)
            .log(A.now(), A.minSec());
    }
}
