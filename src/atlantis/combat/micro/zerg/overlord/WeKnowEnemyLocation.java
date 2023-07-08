package atlantis.combat.micro.zerg.overlord;

import atlantis.combat.squad.alpha.Alpha;
import atlantis.information.enemy.EnemyInfo;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.managers.Manager;
import atlantis.units.select.Select;

public class WeKnowEnemyLocation extends Manager {

    public WeKnowEnemyLocation(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return EnemyInfo.hasDiscoveredAnyBuilding();
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            FollowArmy.class,
            StayAtHome.class,
        };
    }

//    public Manager handle() {
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
