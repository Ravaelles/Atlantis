package atlantis.production.constructions.cancelling;

import atlantis.map.position.APosition;
import atlantis.game.A;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.units.AUnitType;
import atlantis.util.log.ErrorLog;
import atlantis.util.log.EventLog;
import atlantis.util.log.Log;
import atlantis.util.log.LogKey;

public class CancelConstruction {
    private static Log cancelLog = new Log(100, 30 * 300);

    public static void cancel(Construction construction, String reason) {
        AUnitType type = construction.buildingType();

        if (!reason.contains("already being constructed")) {
            APosition at = construction.buildPosition();
            ErrorLog.printMaxOncePerMinute("[LVL=HIGH] Cancel construction: " + type.name()
                + " / " + reason
                + " / at:" + at
                + (at != null ? " / buildable:" + at.isBuildableIncludeBuildings() : "")
            );
            EventLog.key(LogKey.CONSTRUCTION_CANCEL)
                .param("type", type.name())
                .param("cid", construction.id())
                .param("oid", construction.productionOrder() == null ? null : construction.productionOrder().id())
                .param("reason", reason)
                .param("tile", at == null ? "-" : at.toString())
                .log(A.now(), A.minSec());

//            AAdvancedPainter.paintRectangle(at, (int) (32 * type.widthInTiles()), (int) (32 * type.heightInTiles()), Color.Green);
//            PauseAndCenter.on(at, true);
        }
//        AConsole.printStackTrace("Construction.cancel() - " + this);

        if (construction.buildingUnit() != null) {
            construction.buildingUnit().cancelConstruction();
        }

        if (construction.builder() != null) {
            construction.builder().cancelConstruction();
            construction.setBuilder(null);
        }

        ConstructionRequests.removeOrder(construction);

        cancelLog.addMessage(type.name(), A.now());
    }

    public static int countRecentCancellationsOf(AUnitType type) {
        return cancelLog.countMessage(type.name());
    }
}
