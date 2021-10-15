package atlantis.combat.missions;

import atlantis.AGame;
import atlantis.combat.micro.managers.DefendManager;
import atlantis.position.APosition;
import atlantis.units.AUnit;

public class MissionDefend extends Mission {

    protected MissionDefend() {
        super("Attack");
        focusPointManager = new MissionDefendFocusPointManager();
    }

    @Override
    public boolean update(AUnit unit) {

        // === Handle UMS special maps case ========================

        if (AGame.isUms()) {
            return false;
        }

        // =========================================================

        APosition focusPoint = focusPoint();
//        APainter.paintLine(unit, focusPoint, Color.Purple);

        if (focusPoint == null) {
            System.err.println("Couldn't define choke point.");
            throw new RuntimeException("Couldn't define choke point.");
        }

        return DefendManager.defendFocusPoint(unit, focusPoint);
    }

}
