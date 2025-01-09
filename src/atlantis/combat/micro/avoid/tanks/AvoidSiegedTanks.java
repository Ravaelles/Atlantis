package atlantis.combat.micro.avoid.tanks;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.game.player.Enemy;

//public class AvoidSiegedTanks extends Manager {
//    public static final double MIN_SAFE_DIST = 13.3;
//
//    private AUnit siegedTank;
//    private double siegedTankDist;
//
//    public AvoidSiegedTanks(AUnit unit) {
//        super(unit);
//    }
//
//    @Override
//    public boolean applies() {
//        if (!Enemy.terran()) return false;
//        if (unit.isAir()) return false;
//        if (!unit.isMissionAttackOrGlobalAttack()) return false;
//
//        siegedTank = siegedTankClose();
//
//        return siegedTank != null
//            && siegedTankDist < MIN_SAFE_DIST
//            && !tankIsNearOurBuildings()
//            && !shouldEnageSiegedTank();
//    }
//
//    @Override
//    protected Manager handle() {
//        if (unit.moveAwayFrom(siegedTank, moveAwayDist(), Actions.MOVE_AVOID, "AvoidSieged")) return usedManager(this);
//
//        return null;
//    }
//
//    private double moveAwayDist() {
//        return siegedTankDist >= 12.5 ? 0.2 : 4;
//    }
//
//    private boolean shouldEnageSiegedTank() {
//        return unit.combatEvalRelative() >= 1.6
//            || A.supplyUsed() >= 140
//            || A.minerals() >= 1000;
//    }
//
//    private boolean tankIsNearOurBuildings() {
//        return siegedTank.enemiesNear().buildings().notEmpty();
//    }
//
//    private AUnit siegedTankClose() {
//        AUnit tank = unit.enemiesNear()
//            .tanksSieged()
//            .inRadius(13, unit)
//            .nearestTo(unit);
//
//        if (tank == null) {
//            siegedTankDist = 999;
//            return null;
//        }
//
//        siegedTankDist = unit.distTo(tank);
//        return tank;
//    }
//}
