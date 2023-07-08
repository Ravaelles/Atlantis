package atlantis.combat.micro.terran.tank;

import atlantis.information.tech.ATech;
import atlantis.map.AChoke;
import atlantis.map.Chokes;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.managers.Manager;
import atlantis.units.select.Select;
import atlantis.util.Enemy;
import bwapi.TechType;

public class TerranTank extends Manager {

//    private TerranTankWhenSieged terranTankWhenSieged;
//    private TerranTankWhenNotSieged terranTankWhenNotSieged;

    public TerranTank(AUnit unit) {
        super(unit);
//        terranTankWhenSieged = new TerranTankWhenSieged(unit);
//        terranTankWhenNotSieged = new TerranTankWhenNotSieged(unit);
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            TerranTankWhenNotSieged.class,
            TerranTankWhenSieged.class,
        };
    }

//    @Override
//    public Manager handle() {
//        if (!unit.isTank()) return null;
//
//        return unit.isSieged() ? terranTankWhenSieged.handle() : terranTankWhenNotSieged.updateWhenNotSieged();
//    }

    // =========================================================

//    protected boolean hasJustSiegedRecently() {
//        return unit.lastActionLessThanAgo(30 * 9, Actions.SIEGE);
//    }

    protected boolean tooLonely() {
        return Select.ourCombatUnits().inRadius(6, unit).atMost(4);
    }

    public boolean siegeResearched() {
        return ATech.isResearched(TechType.Tank_Siege_Mode);
    }

    protected boolean canSiegeHere(boolean checkTooLonely) {
        if (checkTooLonely && tooLonely()) {
            return false;
        }
        
        if (tooManyTanksInOnePlace()) {
            return false;
        }

        AChoke choke = Chokes.nearestChoke(unit.position());
        if (choke == null) {
            return true;
        }

        return (unit.distTo(choke.center()) - choke.width()) >= 2.6 || (choke.width() >= 3.8);
    }

    private boolean tooManyTanksInOnePlace() {
        if (Enemy.terran()) {
            return false;
        }

        return unit.friendsNear().tanksSieged().inRadius(1, unit).isEmpty();
    }

}
