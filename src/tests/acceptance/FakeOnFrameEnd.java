package tests.acceptance;

import atlantis.game.A;
import tests.unit.FakeUnit;

public class FakeOnFrameEnd {

    protected static void onFrameEnd(AbstractTestFakingGame game) {
        for (FakeUnit unit : game.our) {
            updatePosition(unit);
        }
    }

    // =========================================================

    private static void updatePosition(FakeUnit unit) {
        int speedInPixels = 24;

//        System.out.println("unit.targetPosition = " + unit.targetPosition);
//        System.out.println("unit.isMoving() = " + unit.isMoving());
//        System.out.println("unit.isAttacking() = " + unit.isAttacking());
        if (unit.targetPosition != null && (unit.isMoving() || unit.isAttacking())) {
//            System.out.println("PRE " + unit.position);
            unit.position = unit.position.translateByPixels(
                    A.inRange(-speedInPixels, unit.targetPosition.x - unit.position.x, speedInPixels),
                    A.inRange(-speedInPixels, unit.targetPosition.y - unit.position.y, speedInPixels)
            );
//            System.out.println("Post " + unit.position);
        }
    }

}
