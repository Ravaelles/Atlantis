package atlantis.combat.micro.terran.bunker;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class LoadIntoBunkers extends Manager {
    public LoadIntoBunkers(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return !unit.isLoaded();
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            PreventMaginotLine.class
        };
    }

    @Override
    public Manager handle() {
        if (tryLoadingInfantryIntoBunkerIfNeeded()) {
            return usedManager(this);
        }

        return null;
    }

    private boolean tryLoadingInfantryIntoBunkerIfNeeded() {
        if (unit.isLoaded()) {
            return false;
        }

        if (unit.lastActionLessThanAgo(90, Actions.LOAD)) {
            unit.addLog("Loading");
            return continueLoadingIntoBunker();
        }

        // Only Terran infantry get inside
        if (!unit.isMarine() && !unit.isGhost()) {
            return false;
        }

        // Without enemies around, don't do anything
        Selection enemiesNear = unit.enemiesNear().inRadius(9, unit).canAttack(unit, 10);
        if (enemiesNear.excludeMedics().empty()) {
            return false;
        }

//        if (UnloadFromBunkers.preventFromActingLikeFrenchOnMaginotLine()) {
//            return false;
//        }

        // =========================================================

        AUnit bunker = bunkerToLoadTo();
//        double maxDistanceToLoad = Missions.isGlobalMissionDefend() ? 5.2 : 8.2;

        if (bunker != null && bunker.hasFreeSpaceFor(unit)) {
            double unitDistToBunker = bunker.distTo(unit);
            double maxDistanceToLoad = 2.9 + unit.id() % 4;

            if (unitDistToBunker > maxDistanceToLoad) {
                return false;
            }

            boolean isItSafeToLoadIntoBunker = isItSafeToLoadIntoBunker(bunker, unitDistToBunker, enemiesNear);

            if (isItSafeToLoadIntoBunker) {
                unit.load(bunker);

                String t = "GetToDaChoppa";
                unit.setTooltipTactical(t);
                unit.addLog(t);
                return true;
            }
        }

        return false;
    }

    private boolean isItSafeToLoadIntoBunker(AUnit bunker, double unitDistToBunker, Selection enemiesNear) {
        AUnit nearestEnemy = unit.nearestEnemy();
        if (nearestEnemy == null) {
            return true;
        }
        else {
            if (enemiesNear.groundUnits().inRadius(1, bunker).atLeast(5)) {
                return false;
            }

            double enemyDist = unit.distTo(nearestEnemy);
            double enemyDistToBunker = nearestEnemy.distTo(bunker);

            if (enemyDistToBunker + 1.5 < unitDistToBunker) return false;

            return enemyDist < 2.4 || !enemiesNear.onlyMelee() || unitDistToBunker <= 3.6;
        }
    }

    private boolean continueLoadingIntoBunker() {
        AUnit target = unit.target();

        if (target != null && target.isBunker()) {
            return unit.isMoving() ? true : unit.load(target);
        }
        else {
            AUnit bunker = bunkerToLoadTo();
            if (bunker != null) {
                return unit.load(bunker);
            }

            return false;
        }
    }

    private AUnit bunkerToLoadTo() {
        return Select.ourOfType(AUnitType.Terran_Bunker)
            .inRadius(15, unit)
            .havingSpaceFree(unit.spaceRequired())
            .nearestTo(unit);
    }
}
