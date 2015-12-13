package atlantis.combat.micro;

import jnibwapi.Position;
import jnibwapi.Unit;

/**
 * Handles best way of running from close enemies and information about the fact if given unit is running or
 * not.
 */
public class AtlantisRunning {

    private Unit unit;
    private Position nextPositionToRunTo = null;

    // =========================================================
    public AtlantisRunning(Unit unit) {
        super();
        this.unit = unit;
    }

    // =========================================================
    // Hi-level methods
    /**
     * Indicates that this unit should be running from given enemy unit.
     */
    public void runFrom(Unit nearestEnemy) {
//        int dx = 3 * (nearestEnemy.getPX() - unit.getPX());
//        int dy = 3 * (nearestEnemy.getPY() - unit.getPY());
        Position runTo = getPositionToRunTo(unit, nearestEnemy);

        if (runTo != null && !runTo.equals((Position) unit)) {
            unit.move(runTo, false);
        }
    }

    public static Position getPositionToRunTo(Unit unit, Position runAwayFrom) {
        if (unit == null || runAwayFrom == null) {
            return null;
        }
        int howManyTiles = 1;

        while (howManyTiles <= 10) {
            int xDirectionToUnit = runAwayFrom.getPX() - unit.getPX();
            int yDirectionToUnit = runAwayFrom.getPY() - unit.getPY();

            double vectorLength = runAwayFrom.distanceTo(unit);
            double ratio = 32 * howManyTiles / vectorLength;

            // Add randomness of move if distance is big enough
            //        int xRandomness = howManyTiles > 3 ? (2 - RUtilities.rand(0, 4)) : 0;
            //        int yRandomness = howManyTiles > 3 ? (2 - RUtilities.rand(0, 4)) : 0;
            Position runTo = new Position(
                    (int) (unit.getPX() - ratio * xDirectionToUnit),
                    (int) (unit.getPY() - ratio * yDirectionToUnit)
            ).makeValid();

            if (unit.hasPathTo(runTo)) {
                return runTo;
            } else {
                howManyTiles++;
            }
        }
        return null;
    }

    // =========================================================
    // Getters & Setters
    /**
     * Returns true if given unit is currently (this frame) running from an enemy.
     */
    public boolean isRunning() {
        return nextPositionToRunTo != null;
    }

    public Unit getUnit() {
        return unit;
    }

}
