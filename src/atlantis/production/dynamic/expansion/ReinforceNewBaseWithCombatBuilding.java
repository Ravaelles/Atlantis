package atlantis.production.dynamic.expansion;

import atlantis.architecture.Commander;
import atlantis.config.AtlantisConfig;
import atlantis.map.position.APosition;
import atlantis.production.constructing.Construction;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.util.We;
import atlantis.util.log.ErrorLog;

public abstract class ReinforceNewBaseWithCombatBuilding extends Commander {
    public ReinforceNewBaseWithCombatBuilding() {
    }

    public static ReinforceNewBaseWithCombatBuilding create() {
        if (We.terran()) return new TerranReinforceNewBaseWithCombatBuilding();

        return null;
    }

    @Override
    public void handle() {
        APosition basePosition = validateBasePosition();

        if (basePosition != null) makeSureIsReinfoced(basePosition);
    }

    private static APosition validateBasePosition() {
        Construction futureBase = ConstructionRequests.getNotStartedOfType(AtlantisConfig.BASE);

        if (futureBase == null) {
            ErrorLog.printMaxOncePerMinute("ReinforceNewBaseWithCombatBuilding: futureBase == null");
            return null;
        }

        APosition basePosition = futureBase.buildPosition();

        if (basePosition == null) {
            ErrorLog.printMaxOncePerMinute("ReinforceNewBaseWithCombatBuilding: futureBase.buildPosition() == null");
            return null;
        }
        return basePosition;
    }

    protected abstract void makeSureIsReinfoced(APosition basePosition);
}
