package starengine.sc_logic;

import atlantis.map.position.APosition;
import atlantis.util.Vector;
import atlantis.util.log.ErrorLog;
import starengine.units.state.EngineUnitState;
import tests.unit.FakeUnit;

public class ProcessMoveUnit {
    private static final double UNIT_SPEED_MODIFIER_PER_FRAME = 1;

    public static boolean update(FakeUnit unit) {
        unit.lastCommand = "Move";

        APosition target = unit.targetPosition();
        if (target == null) {
            ErrorLog.printErrorOnce("Unit " + unit + " is moving but has no target position");
            return false;
        }

        int speedInPixels = (int) (unit.maxSpeed() * UNIT_SPEED_MODIFIER_PER_FRAME);

        Vector movement = new Vector(unit.targetPosition.x - unit.position.x, unit.targetPosition.y - unit.position.y);
        movement.normalizeTo(speedInPixels);

        unit.position = unit.position.translateByPixels((int) movement.x, (int) movement.y);

//            System.err.println("PRE " + unit.position);
//        unit.position = unit.position.translateByPixels(
//            A.inRange(-speedInPixels, unit.targetPosition.x - unit.position.x, speedInPixels),
//            A.inRange(-speedInPixels, unit.targetPosition.y - unit.position.y, speedInPixels)
//        );
//            System.err.println("Post " + unit.position);

        unit.previousState = EngineUnitState.MOVING;

        return true;
    }


}
