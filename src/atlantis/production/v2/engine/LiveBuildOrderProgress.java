package atlantis.production.v2.engine;

import atlantis.game.A;
import atlantis.information.tech.ATech;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.v2.Producible;
import atlantis.production.v2.TechProducible;
import atlantis.production.v2.UnitProducible;
import atlantis.production.v2.UpgradeProducible;
import atlantis.production.v2.goals.BuildOrderProgress;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import bwapi.TechType;
import bwapi.UpgradeType;

/**
 * {@link BuildOrderProgress} answered from the live game: completed, under
 * construction, queued in a facility and requested-but-not-placed all count,
 * minus what the game starts with.
 */
public final class LiveBuildOrderProgress implements BuildOrderProgress {

    private static final int STARTING_WORKERS = 4;

    @Override
    public int supplyUsed() {
        return A.supplyUsed();
    }

    @Override
    public int produced(Producible item) {
        if (item instanceof UnitProducible)
            return producedUnits(((UnitProducible) item).type());
        if (item instanceof TechProducible)
            return producedTech(((TechProducible) item).tech());
        if (item instanceof UpgradeProducible)
            return producedUpgrade(((UpgradeProducible) item).upgrade());
        return 0;
    }

    private int producedUnits(AUnitType type) {
        int count = Select.ourOfType(type).count()
                + Select.ourUnfinished().ofType(type).count()
                + ConstructionRequests.countNotStartedOfType(type)
                + inTrainingQueues(type);
        return Math.max(0, count - startingCount(type));
    }

    private int inTrainingQueues(AUnitType type) {
        if (type.isABuilding())
            return 0;
        int total = 0;
        for (AUnit facility : Select.ourBuildings().list()) {
            for (AUnitType queued : facility.trainingQueue()) {
                if (type.equals(queued))
                    total++;
            }
        }
        return total;
    }

    /**
     * The game hands each race one base and four workers (Zerg: one Overlord too).
     */
    static int startingCount(AUnitType type) {
        if (type.isWorker())
            return STARTING_WORKERS;
        if (type.equals(AUnitType.Protoss_Nexus) || type.equals(AUnitType.Terran_Command_Center)
                || type.equals(AUnitType.Zerg_Hatchery))
            return 1;
        if (type.equals(AUnitType.Zerg_Overlord))
            return 1;
        return 0;
    }

    private int producedTech(TechType tech) {
        if (ATech.isResearched(tech))
            return 1;
        for (AUnit facility : Select.ourBuildings().list()) {
            if (facility.isResearching() && tech.equals(facility.whatIsResearching()))
                return 1;
        }
        return 0;
    }

    private int producedUpgrade(UpgradeType upgrade) {
        int level = ATech.getUpgradeLevel(upgrade);
        for (AUnit facility : Select.ourBuildings().list()) {
            if (facility.isUpgrading() && upgrade.equals(facility.whatIsUpgrading()))
                return level + 1;
        }
        return level;
    }
}
