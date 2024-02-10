package atlantis.combat.micro.terran.tank;

import atlantis.information.tech.ATech;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.util.Enemy;
import bwapi.TechType;

public class TankDecisions {
    public static boolean siegeResearched() {
        return ATech.isResearched(TechType.Tank_Siege_Mode);
    }

    public static boolean tooLonely(AUnit unit) {
        return Select.ourCombatUnits().inRadius(6, unit).atMost(4);
    }

    public static boolean canSiegeHere(AUnit unit, boolean checkTooLonely) {
        if (unit.hasSiegedRecently() && unit.enemiesNear().inRadius(14, unit).empty()) return false;

        if (checkTooLonely && tooLonely(unit)) return false;

        if (tooManyTanksInOnePlace(unit)) return false;

        AChoke choke = Chokes.nearestChoke(unit.position(), "MAIN");
        if (choke == null) return true;

        return (unit.distTo(choke.center()) - choke.width()) >= 2.6 || (choke.width() >= 3.8);
    }

    private static boolean tooManyTanksInOnePlace(AUnit unit) {
        if (Enemy.terran()) return false;

        return unit.friendsNear().tanksSieged().inRadius(1, unit).isEmpty();
    }
}
