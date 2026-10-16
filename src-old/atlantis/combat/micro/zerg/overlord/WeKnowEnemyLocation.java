package atlantis.combat.micro.zerg.overlord;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
import atlantis.information.enemy.EnemyInfo;
import atlantis.units.AUnit;

public class WeKnowEnemyLocation extends Manager {

    public WeKnowEnemyLocation(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return EnemyInfo.hasDiscoveredAnyBuilding();
    }

    @Override
    protected ManagerFactory[] managers() {
        return new ManagerFactory[]{
            FollowArmy::new,
            StayAtHome::new,
        };
    }

//    protected Manager handle() {
//        if (applies()) {
//            update();
//            return usingManager(this);
//        }
//
//        return null;
//    }

//    private boolean update() {
//        Position goTo = AtlantisMap.getMainBaseChokepoint();
//        if (goTo == null) {
//            goTo = Select.mainBase();
//        }
//
//        unit.setTooltip("Retreat");
//        if (goTo != null && goTo.distanceTo() > 3) {
//            unit.setTooltip("--> Retreat");
//            unit.move(goTo, false);
//        }
//
//        if (unit.id() % 3 == 0) {
//            return followArmy(unit);
//        } else {
//            return stayAtHome(unit);
//        }
//    }
}
