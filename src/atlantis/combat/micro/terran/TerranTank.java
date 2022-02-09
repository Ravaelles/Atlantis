package atlantis.combat.micro.terran;

import atlantis.debug.painter.APainter;
import atlantis.game.A;
import atlantis.information.tech.ATech;
import atlantis.map.AChoke;
import atlantis.map.Chokes;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.util.Enemy;
import bwapi.Color;
import bwapi.TechType;


public class TerranTank {

    public static boolean update(AUnit unit) {
        return unit.isSieged() ? TerranTankWhenSieged.updateSieged(unit) : TerranTankWhenNotSieged.updateWhenNotSieged(unit);
    }

    // =========================================================

    protected static boolean hasJustSiegedRecently(AUnit unit) {
        return unit.lastActionLessThanAgo(30 * 10, Actions.SIEGE) && unit.cooldownRemaining() == 0;
    }

    protected static boolean tooLonely(AUnit unit) {
        return Select.ourCombatUnits().inRadius(6, unit).atMost(4);
    }

    protected static boolean siegeResearched() {
        return ATech.isResearched(TechType.Tank_Siege_Mode);
    }

    protected static boolean canSiegeHere(AUnit unit, boolean checkTooLonely) {
        if (checkTooLonely && tooLonely(unit)) {
            return false;
        }
        
        if (tooManyTanksInOnePlace(unit)) {
            return false;
        }

        AChoke choke = Chokes.nearestChoke(unit.position());
        if (choke == null) {
            return true;
        }

        return (unit.distTo(choke.center()) - choke.width()) >= 1.6 || (choke.width() >= 3.8);
    }

    private static boolean tooManyTanksInOnePlace(AUnit unit) {
        if (Enemy.terran()) {
            return false;
        }

        return unit.friendsNearby().tanksSieged().inRadius(1, unit).isEmpty();
    }

}
