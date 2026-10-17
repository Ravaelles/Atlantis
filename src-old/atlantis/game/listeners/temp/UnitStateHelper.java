package atlantis.game.listeners.temp;

import atlantis.game.A;
import atlantis.units.AUnit;

public class UnitStateHelper {
    public static void identifyUnitBrakingDistance(AUnit unit) {
//        if (A.now <= 1) System.out.println("       maxSpeed = " + unit.maxSpeed());

        System.out.println(A.now() + " - " + unit.typeWithUnitId() + " / dist: " + A.digit(unit.distToTarget()));
//        if (unit.isMoving()) System.out.println("         moving = " + unit.isMoving());
//        if (unit.isAccelerating()) System.out.println("         accelerating = " + unit.isAccelerating());
//        if (unit.isBraking()) System.out.println("         braking = " + unit.isBraking());

        if (unit.isAttackFrame() && unit.lastAttackOrderLessThanAgo(1)) {
            System.out.println("         ATTACK FRAME");
        }
//        if (unit.lastAttackFrameLessThanAgo(1)) System.err.println("      ATTACK FRAME AT " + A.now());
    }
}
