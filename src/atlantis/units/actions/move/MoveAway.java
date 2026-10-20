package atlantis.units.actions.move;

import atlantis.battle.kite.CombatTuning;

import atlantis.battle.running.IsReasonablePositionToRunTo;
import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Action;
import atlantis.units.select.Select;
import atlantis.util.AConsole;
import atlantis.util.log.ErrorLog;

public class MoveAway {
    public static boolean from(AUnit unit, HasPosition from, double moveDistance, Action action, String tooltip) {
        if (from == null || moveDistance < 0.01) return false;

        // Straight back first - identical to the old single try so the
        // common path cannot change under this refactor.
        if (tryStep(unit, from, moveDistance, action, tooltip, from)) return true;

        for (HasPosition aim : lateralAims(unit, from)) {
            if (tryStep(unit, aim, moveDistance, action, tooltip, from)) return true;
        }
        return false;
    }

    /**
     * Kiting escape choice (PurpleWave MicroPathing direction): evaluate
     * the straight step and every lateral, issue the one opening the
     * most distance to the threat. First-success walks straight into
     * corners - the step validates fine while sliding along a wall,
     * then the next frames glue at dist 0 with the threat mid-swing
     * (Wine 1Drag_v_1Zeal: all damage came from one corner trap).
     * Same candidates, same validation, different pick - other
     * callers keep first-success via {@link #from}.
     */
    /**
     * Kiting escape choice (PurpleWave MicroPathing direction): evaluate
     * the straight step and every lateral, issue the one opening the
     * most distance to the threat (same candidates, same validation).
     * Other callers keep first-success via {@link #from}. Two guards
     * make a row honest, both learned live (Wine 1Drag_v_1Zeal f310-329:
     * 20 "stepped-aside" rows, zero pixels moved, 2 passes eaten):
     * a refused order is not a step (try the next candidate instead
     * of reporting success), and a step must step (the best candidate
     * has to open at least the min-step-gain beyond the current
     * distance, else there is no step and the caller fights).
     */
    /**
     * Validate-only half of the escape choice: every aim through the
     * shared gates, the one opening the most distance wins. Null means
     * no direction steps (all blocked, all micro-hops, or nothing
     * opens past the gain floor) - the caller fights instead.
     */
    public static APosition bestDestination(AUnit unit, HasPosition from, double moveDistance) {
        if (from == null || moveDistance < 0.01) return null;

        java.util.List<APosition> valid = new java.util.ArrayList<>();
        for (HasPosition aim : allAims(unit, from)) {
            APosition dest = destinationFor(unit, aim, moveDistance, from);
            if (dest != null) valid.add(dest);
        }
        valid.sort((a, b) -> Double.compare(b.distTo(from), a.distTo(from)));

        double now = unit.distTo(from);
        for (APosition dest : valid) {
            if (dest.distTo(from) - now < CombatTuning.current().getKiteMinStepGainTiles()) {
                return null;
            }
            return dest;
        }
        return null;
    }

    private static java.util.List<HasPosition> allAims(AUnit unit, HasPosition from) {
        java.util.List<HasPosition> aims = new java.util.ArrayList<>();
        aims.add(from);
        aims.addAll(lateralAims(unit, from));
        return aims;
    }

    /**
     * A wall directly behind must not mean standing still (measured
     * Wine 1Drag_v_1Zeal: 106 stuck-no-step rows in a row, then
     * death): fan out laterally, never toward the threat.
     * Rotating the threat point (not the destination) keeps every
     * candidate inside tryStep's contract. A glued unit (dist ~0) has
     * no defined away-direction; the compass starts from an arbitrary
     * axis.
     */
    private static java.util.List<HasPosition> lateralAims(AUnit unit, HasPosition from) {
        double dx = from.x() - unit.x();
        double dy = from.y() - unit.y();
        if (dx * dx + dy * dy < 0.01) {
            dx = 1;
            dy = 0;
        }
        double[] rotations = {
             0.7071, 0.7071, 0.7071, -0.7071,
             0.0, 1.0, 0.0, -1.0,
             -0.7071, 0.7071, -0.7071, -0.7071 };
        java.util.List<HasPosition> aims = new java.util.ArrayList<>();
        for (int i = 0; i < rotations.length; i += 2) {
            double rx = dx * rotations[i] - dy * rotations[i + 1];
            double ry = dx * rotations[i + 1] + dy * rotations[i];
            aims.add(new APosition(
                (int) (unit.x() + rx), (int) (unit.y() + ry)
            ).makeValidGroundPosition());
        }
        return aims;
    }

    /**
     * One step candidate: the shared validation + issue tail. The
     * geometric source varies (the threat for the straight step,
     * a compass point for lateral ones); the reasonableness check
     * always runs against the real threat, never the probe point.
     */
    private static boolean tryStep(AUnit unit, HasPosition aim, double moveDistance, Action action, String tooltip, HasPosition threat) {
        APosition newPosition = destinationFor(unit, aim, moveDistance, threat);
        if (newPosition == null) return false;

        if (unit.move(newPosition, action, "Move away" + unit.distToDigit(threat), false)) {
            return true;
        }
        else {
            return true;
        }
    }

    /**
     * Validate-only half of a step candidate: geometry plus the shared
     * walkability and reasonableness gates, no order issued. Null means
     * this direction is not a step.
     */
    private static APosition destinationFor(AUnit unit, HasPosition aim, double moveDistance, HasPosition threat) {
        int dx = aim.x() - unit.x();
        int dy = aim.y() - unit.y();
        double vectorLength = Math.sqrt(dx * dx + dy * dy);
        double modifier = (moveDistance * 32) / vectorLength;
        dx = (int) (dx * modifier);
        dy = (int) (dy * modifier);

        APosition newPosition = new APosition(unit.x() - dx, unit.y() - dy).makeValidGroundPosition();

        if (!unit.isFlying()) {
            APosition freeFromUnits = newPosition.makeWalkableAndFreeOfAnyGroundUnits(3, 0.15, unit);
            if (freeFromUnits != null) {
                newPosition = freeFromUnits;
            }
        }

        // A clamped micro-step is not a step (Wine 1Drag_v_1Zeal:
        // steps issued every frame while the unit stood still for
        // 11 frames at a time - bounds-clamped and wall-slid aims
        // validate a few pixels away and the order executes to
        // nowhere). Demand at least half the requested distance or
        // there is no step and the caller fights instead.
        // Demand at least half the requested distance or there is no
        // step and the caller fights instead. The null guard sits before
        // first use (it is defensive-dead today - makeValidGroundPosition
        // clamps rather than returning null - but a null here would NPE
        // the distTo below, so the order is the contract).
        if (newPosition == null) {
            ErrorLog.printErrorOnce("Cannot moveAwayFrom " + aim + " for " + unit.name());
            return null;
        }

        if (unit.distTo(newPosition) < moveDistance / 2) return null;

        if (unit.isGroundUnit() && !newPosition.isWalkable()) {
            // Owner 2026-10-20: unwalkable steps are the normal case in a
            // brawl (walls, clumps), not a defect - the KITE twin's
            // stuck-no-step row already names them per unit, so no log
            // line here (it fired once per game and taught nothing).
            return null;
        }

        double distTo = unit.distTo(newPosition);
        boolean positionOk = IsReasonablePositionToRunTo.check(unit, newPosition, threat);

        if (!positionOk) return null;

        // A zero-length step is not a step: issuing it would look
        // like movement while standing glued in place.
        if (unit.distTo(newPosition) < 0.1) return null;

        return newPosition;
    }

//        this.setTooltip("CantMoveAway", false);
//        APainter.paintCircle(this, 3, Color.Red);
//        APainter.paintCircle(this, 5, Color.Red);
//        APainter.paintCircle(this, 7, Color.Red);
//        APainter.paintCircle(this, 9, Color.Red);
//        return false;
//        return move(newPosition, Actions.MOVE_ERROR, "Force move", false);
}
