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

        // TEMPORARY measurement (Assimilator "took too long (39s) / buildable:false" on
        // OpenBW after the TooCloseToGeyser exemption landed): print the whole fix path -
        // which geyser, which tile came in, what the fix loop returned. Remove after the
        // cause is named.
        if (building.isGasBuilding()) {
            AUnit geyserNear = Select.geysers().inRadius(6, position).nearestTo(position);
            System.err.println("GAS_FIX in=" + position
                + " geysersIn6=" + Select.geysers().inRadius(6, position).count()
                + " nearest=" + (geyserNear == null ? "none" : geyserNear.position() + " d="
                    + A.digit(geyserNear.distTo(position))));
        }

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

            if (geyser == null || geyser.tilePosition() == null) return null;

            position = APosition.create(geyser.tilePosition());
        }

        // =========================================================

        if (CanPhysicallyBuildHere.check(unit, building, position)) {
            if (building.isGasBuilding()) System.err.println("GAS_FIX out(ok)=" + position);
            return position;
        }
        if (building.isGasBuilding()) System.err.println("GAS_FIX in-position NOT buildable, entering loop at " + position);

        // === Loop ================================================

        int radius = 1;
        while (radius <= 4) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    if (dx == 0 && dy == 0) continue;

                    if (CanPhysicallyBuildHere.check(
                        unit, building, position.translateByTiles(dx, dy))
                    ) {
                        return position.translateByTiles(dx, dy);
                    }
                }
            }

            radius++;
        }

        return position;
    }
}
