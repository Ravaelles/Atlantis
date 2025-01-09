package atlantis.production.constructions.cancelling;

import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.units.AUnitType;
import atlantis.util.log.ErrorLog;
import atlantis.util.log.Log;

public class CancelConstruction {
    private static Log cancelLog = new Log(100, 30 * 300);

    public static void cancel(Construction construction, String reason) {
        AUnitType type = construction.buildingType();

        if (!reason.contains("already being constructed")) {
            APosition at = construction.buildPosition();
            ErrorLog.printMaxOncePerMinute("Cancel construction: " + type.name()
                + " / " + reason
                + " / at:" + at
                + (at != null ? " / buildable:" + at.isBuildableIncludeBuildings() : "")
            );

//            AAdvancedPainter.paintRectangle(at, (int) (32 * type.widthInTiles()), (int) (32 * type.heightInTiles()), Color.Green);
//            PauseAndCenter.on(at, true);
        }
//        A.printStackTrace("Construction.cancel() - " + this);

        if (construction.buildingUnit() != null) {
            construction.buildingUnit().cancelConstruction();
        }

        if (construction.builder() != null) {
            construction.builder().cancelConstruction();
            construction.setBuilder(null);
        }

        ConstructionRequests.removeOrder(construction);

        cancelLog.addMessage(type.name(), null);
    }

    public static int countRecentCancellationsOf(AUnitType type) {
        return cancelLog.countMessage(type.name());
    }
}
