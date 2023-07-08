package atlantis.combat.micro.terran;

import atlantis.config.env.Env;
import atlantis.debug.painter.AAdvancedPainter;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.map.ABaseLocation;
import atlantis.map.Bases;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.managers.Manager;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.log.ErrorLog;
import bwapi.Color;

import java.util.List;

public class TerranCommandCenter extends Manager {

    public TerranCommandCenter(AUnit unit) {
        super(unit);
    }

    public  boolean update() {
        if (AGame.notNthGameFrame(46)) {
            return false;
        }

        boolean baseMinedOut = baseMinedOut();
        if (baseMinedOut && unit.isLifted()) {
            return flyToNewMineralPatches();
        }
        else if (baseMinedOut) {
            if (unit.lastActionMoreThanAgo(3)) {
                unit.lift();
            }
            return true;
        }

        return false;
    }

    // =========================================================

    private  boolean baseMinedOut() {
        return Select.minerals().inRadius(12, unit).isEmpty();
    }

    private  boolean flyToNewMineralPatches() {
        if (Env.isTesting()) {
            return false;
        }

        List<AUnit> minerals = Select.minerals().sortDataByDistanceTo(unit, true);
        Selection bases = Select.ourBuildingsWithUnfinished().ofType(AUnitType.Terran_Command_Center);
        ABaseLocation baseLocation = Bases.expansionFreeBaseLocationNearestTo(unit);

        if (baseLocation == null && !Env.isTesting()) {
            ErrorLog.printErrorOnce("No expansionFreeBaseLocationNearestTo for rebasing");
            return false;
        }

        APosition rebaseTo = baseLocation.isPositionVisible()
            ? baseLocation.makeLandableFor(unit)
            : baseLocation.position();

        if (rebaseTo == null && minerals.size() > 0) {
            rebaseTo = minerals.get(0) != null ? minerals.get(0).position() : null;
        }

        if (rebaseTo == null) {
            ErrorLog.printErrorOnce("Null rebaseTo");
            return false;
        }

//        System.out.println(rebaseTo + " // " + A.dist(Select.main(), rebaseTo));

        AAdvancedPainter.paintBase(rebaseTo, "REBASE HERE", Color.Green, -0.5);

//        System.out.println("baseLocation.isExplored() = " + baseLocation.isExplored());
//        System.out.println("minerals = " + Select.minerals().inRadius(10, rebaseTo).notEmpty());

            if (
                !rebaseTo.isExplored()
                || Select.minerals().inRadius(10, rebaseTo).notEmpty()
            ) {
                if (!unit.isLifted() && rebaseTo.distToMoreThan(unit, 3)) {
//                    System.err.println("# Lift");
                    unit.lift();
                    return true;
                }
                else {
//                    if (A.everyNthGameFrame(31)) {
                    double dist = rebaseTo.distTo(unit);
                    unit.setTooltip("Rebase" + A.dist(dist), true);
                    if (dist <= 5) {
                        rebaseTo = baseLocation.makeLandableFor(unit);
//                        System.err.println("# Land at " + rebaseTo.toTilePosition());
                        if (rebaseTo != null) {
                            unit.land(rebaseTo.toTilePosition());
                            return true;
                        }
                    } else {
//                        System.err.println("# Fly to " + rebaseTo + " // " + dist);
                        if (unit.move(rebaseTo, Actions.MOVE_SPECIAL, "FlyToRebase", true)) {
                            return true;
                        }
                    }
                }
            }
//        }

        return false;
    }

}
