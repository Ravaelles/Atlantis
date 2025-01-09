package atlantis.units.interrupt;

import atlantis.architecture.Manager;
import atlantis.combat.micro.avoid.AvoidEnemies;
import atlantis.units.AUnit;
import atlantis.util.We;

//public class ForceRunAfterShot extends Manager {
//    private int cooldown;
//
//    public ForceRunAfterShot(AUnit unit) {
//        super(unit);
//    }
//
//    @Override
//    public boolean applies() {
//        if (!We.terran()) return false;
//
//        cooldown = unit.cooldown();
//        if (cooldown <= 5) return false;
//
//        return applyForMarine(unit);
//    }
//
//    @Override
//    public Manager handle() {
//        if (unit.lastAttackFrameLessThanAgo(14)) {
/// /            System.err.println("Force AvoidEnemies afterShot (" + unit.cooldown() + ")");
//            if ((new AvoidEnemies(unit)).forceHandle() != null) {
////                System.err.println("          YES");
//                return usedManager(this);
//            }
//        }
//
//        return null;
//    }
//
//    private boolean applyForMarine(AUnit unit) {
//        if (!unit.isMarine()) return false;
//
//        return cooldown <= 14;
//    }
//}
