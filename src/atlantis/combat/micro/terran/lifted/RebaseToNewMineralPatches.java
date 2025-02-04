package atlantis.combat.micro.terran.lifted;

import atlantis.architecture.Manager;
import atlantis.config.env.Env;
import atlantis.game.AGame;
import atlantis.map.base.ABaseLocation;
import atlantis.map.base.BaseLocations;
import atlantis.map.path.OurClosestBaseToEnemy;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.util.log.ErrorLog;

public class RebaseToNewMineralPatches extends Manager {
    private static ABaseLocation rebaseTo = null;

    public RebaseToNewMineralPatches(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (AGame.notNthGameFrame(47)) return false;

        return unit.isCommandCenter() && (unit.isLifted() || isBaseMinedOut(unit));
    }

    @Override
    protected Manager handle() {
        if (flyToNewMineralPatches()) {
            return usedManager(this);
        }

        return null;
    }

    private boolean flyToNewMineralPatches() {
        if (Env.isTesting()) return false;

        if (newBaseIsDefinedAndFree() && unit.isMoving() && unit.lastActionLessThanAgo(30 * 15)) return true;
        rebaseTo = defineNewBaseLocation();

        if (rebaseTo == null) {
            ErrorLog.printErrorOnce("Null newBase");
            return false;
        }

        if (flyAndRebase(rebaseTo)) return true;

        return false;
    }

    private boolean newBaseIsDefinedAndFree() {
        return rebaseTo != null && Select.all().buildings().countInRadius(5, rebaseTo) == 0;
    }

    private ABaseLocation defineNewBaseLocation() {
        AUnit oldestUnit = Select.ourWorkers().first();

        ABaseLocation baseLocation = BaseLocations.expansionFreeBaseLocationNearestTo(oldestUnit);

        if (baseLocation == null && !Env.isTesting()) {
            ErrorLog.printErrorOnce("No expansionFreeBaseLocationNearestTo for rebasing");
            return null;
        }

        return baseLocation;
    }

    private boolean flyAndRebase(ABaseLocation baseLocation) {
        if (liftToFlyFirst(baseLocation)) return true;

        double dist = baseLocation.distTo(unit);
        if (dist >= 6) {
            return moveToRebase(baseLocation);
        }
        else {
            issueLandOrder(baseLocation);
            return true;
        }
    }

    private boolean moveToRebase(ABaseLocation rebaseTo) {
        unit.setTooltipAndLog("MoveToRebase");
        unit.move(rebaseTo, Actions.SPECIAL, "FlyToRebase", true);
        return true;
    }

    private boolean issueLandOrder(ABaseLocation baseLocation) {
        APosition rebaseExactLocation = baseLocation.makeLandableFor(unit);
        if (rebaseExactLocation != null) {
            unit.setTooltipAndLog("LandInNewHome");
            unit.land(rebaseExactLocation.toTilePosition());
            OurClosestBaseToEnemy.clearCache();
            return true;
        }
        return false;
    }

    private boolean liftToFlyFirst(ABaseLocation rebaseTo) {
        if (!unit.isLifted() && rebaseTo.distToMoreThan(unit, 5)) {
            unit.setTooltipAndLog("LiftAndRebase");
            unit.lift();
            OurClosestBaseToEnemy.clearCache();
            return true;
        }
        return false;
    }

    public static boolean isBaseMinedOut(AUnit unit) {
        return Select.minerals().inRadius(10, unit).isEmpty();
    }
}
