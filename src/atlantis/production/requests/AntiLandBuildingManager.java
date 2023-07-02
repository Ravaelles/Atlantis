package atlantis.production.requests;

import atlantis.combat.micro.terran.TerranBunker;
import atlantis.game.A;
import atlantis.map.AChoke;
import atlantis.map.Bases;
import atlantis.map.Chokes;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.Requirements;
import atlantis.production.constructing.position.APositionFinder;
import atlantis.production.orders.build.AddToQueue;
import atlantis.production.orders.production.ProductionQueue;
import atlantis.production.requests.protoss.ProtossPhotonCannonAntiLand;
import atlantis.production.requests.zerg.ZergSunkenColony;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.Enemy;
import atlantis.util.We;

import static atlantis.units.AUnitType.Protoss_Forge;

public abstract class AntiLandBuildingManager extends DynamicBuildingManager {

    protected static AntiLandBuildingManager instance = null;

    // =========================================================

    public abstract int expected();

    // =========================================================

    @Override
    public boolean shouldBuildNew() {
//        System.err.println("Expected: " + expected()  + "(" + existingWithUnfinished() + ")");
        return existingWithUnfinished() < expected();
    }

    @Override
    public boolean handleBuildNew() {
        if (We.zerg()) {
            ((ZergSunkenColony) instance).handleExistingCreepColonyIfNeeded();
        }

        if (shouldBuildNew()) {
//            System.err.println("ENQUEUE NEW Sunken Colony");
            return requestOne(nextBuildingPosition());
        }

        return false;
    }


    @Override
    public boolean requestOne(HasPosition at) {
        AUnitType buildType = typeToBuildFirst();

        // === Zerg fix - morph existing Creep Colonies ============

        if (We.zerg()) {
            AUnit creep = Select.ourOfType(AUnitType.Zerg_Creep_Colony).first();
            if (creep != null) {
                return creep.morph(buildType);
            }
        }

        // =========================================================

        if (at == null) {
            at = nextBuildingPosition();
        }

        if (at != null) {
            AUnitType requirement = buildType.whatIsRequired();
            if (
                requirement != null
                    && !Requirements.hasRequirements(buildType)
                    && !ProductionQueue.isAtTheTopOfQueue(requirement, 6)
            ) {
                if (!buildType.isSunken() && !buildType.isSporeColony() && !buildType.isCannon()) {
                    if (A.supplyUsed() >= 6) {
                        System.err.println("--- Non critical but ugly issue ---");
                        System.err.println("Missing requirement: " + requirement + " for: " + buildType);
                    }
    //                A.printStackTrace("Missing requirement: " + requirement + " for: " + type());
                    return false;
                }

                // We're missing the requirement, so explicitly request it
                else {
                    addRequirement(buildType);
                    return true;
                }
            }

            AddToQueue.withTopPriority(buildType, at);
            return true;
        }

        return false;
    }

    private boolean addRequirement(AUnitType buildType) {
        if (buildType.isCannon()) {
            if (Count.existingOrInProductionOrInQueue(Protoss_Forge) == 0) {
                AddToQueue.withTopPriority(Protoss_Forge);
                return true;
            }
        }

        return false;
    }

    @Override
    public HasPosition nextBuildingPosition() {
        int bases = Count.bases();

        if (bases == 0 || Select.main() == null) {
            return null;
        }

        // === Main choke ===========================================

        AChoke mainChoke = Chokes.mainChoke();
        if (bases <= 1 && mainChoke != null) {
            if (We.terran() && Enemy.terran()) {
                APosition nearTo = mainChoke.translateTilesTowards(3, Select.main())
                    .makeWalkable(8);
                AUnit builder = Select.ourWorkers().nearestTo(nearTo);
                return APositionFinder.findStandardPosition(
                    builder, type(), nearTo, 15
                );
            }

            APosition nearTo = Select.main()
                .translateTilesTowards(We.terran() ? 5 : 9, mainChoke)
                .makeWalkable(8);
            AUnit builder = Select.ourWorkers().nearestTo(nearTo);
            return APositionFinder.findStandardPosition(
                builder, type(), nearTo, 15
            );

//            return PositionModifier.toPosition(
//                PositionModifier.MAIN_CHOKE, type(), null, null
//            );
        }

        // === At natural ===========================================

        if (bases >= 2) {
            APosition naturalPosition = Bases.natural();
            if (naturalPosition != null) {
                AUnit naturalBase = Select.ourBases().inRadius(8, naturalPosition).first();
                if (naturalBase != null) {
                    AChoke naturalChoke = Chokes.natural();
                    if (naturalChoke != null) {
//                        return naturalChoke.translatePercentTowards(10, naturalBase);
                        return naturalChoke.translateTilesTowards(4, naturalBase);
                    }

                    return naturalBase;
                }
            }
        }

        // =========================================================

        AUnitType building = type();
        APosition nearTo = null;

//        System.out.println(building + " // " + AGame.hasTechAndBuildingsToProduce(building));

        AUnit previousBuilding = Select.ourBuildingsWithUnfinished().ofType(building).first();
        if (previousBuilding != null) {
            nearTo = previousBuilding.position();
        }

        if (nearTo == null) {

            // Place near the base
            nearTo = Select.naturalOrMain() != null ? Select.naturalOrMain().position() : null;
//            nearTo = Select.mainBase();
        }

        // Move towards nearest choke
        if (nearTo != null) {
            AChoke choke = Chokes.nearestChoke(nearTo);
            if (choke != null) {
                nearTo = nearTo.translateTilesTowards(choke, 7);
            }
        }

        return nearTo;
    }

    // =========================================================

    public static AntiLandBuildingManager get() {
        if (instance == null) {
            if (We.zerg()) {
                return instance = new ZergSunkenColony();
            }
            else if (We.protoss()) {
                return instance = new ProtossPhotonCannonAntiLand();
            }
            else if (We.terran()) {
                return instance = new TerranBunker();
            }
        }

        return instance;
    }

}
