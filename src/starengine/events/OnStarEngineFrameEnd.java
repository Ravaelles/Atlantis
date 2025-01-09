package starengine.events;

import atlantis.game.A;
import starengine.sc_logic.UpdateUnits;
import tests.acceptance.AbstractWorldCreatingTest;
import tests.fakes.FakeUnit;

public class OnStarEngineFrameEnd {
    public static double UNIT_SPEED_MODIFIER_PER_FRAME = 1;

    public static void onFrameEnd(AbstractWorldCreatingTest test) {
//        for (FakeUnit unit : test.our) {
//            updatePosition(unit);
//        }

        UpdateUnits.update();

        test.engine().updateOnFrameEnd();
    }

    // =========================================================

    private static void updatePosition(FakeUnit unit) {
        int speedInPixels = (int) (unit.maxSpeed() * UNIT_SPEED_MODIFIER_PER_FRAME);

//        System.err.println("unit.targetPosition = " + unit.targetPosition);
//        System.err.println("unit.isMoving() = " + unit.isMoving());
//        System.err.println("unit.isAttacking() = " + unit.isAttacking());
        if (unit.targetPosition != null && (unit.isMoving() || unit.isAttacking())) {
//            System.err.println("PRE " + unit.position);
            unit.position = unit.position.translateByPixels(
                A.inRange(-speedInPixels, unit.targetPosition.x - unit.position.x, speedInPixels),
                A.inRange(-speedInPixels, unit.targetPosition.y - unit.position.y, speedInPixels)
            );
//            System.err.println("Post " + unit.position);
        }
    }

}
