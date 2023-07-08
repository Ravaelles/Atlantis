package atlantis.combat.micro.terran.bunker;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.managers.Manager;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class LoadIntoBunkers extends Manager {

    public LoadIntoBunkers(AUnit unit) {
        super(unit);
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
            return usingManager(this);
        }

        return null;
    }

    private boolean tryLoadingInfantryIntoBunkerIfNeeded() {
        if (unit.isLoaded()) {
            return false;
        }

        if (unit.lastActionLessThanAgo(20, Actions.LOAD)) {
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

            boolean canLoad;

            AUnit nearestEnemy = unit.nearestEnemy();
            if (nearestEnemy == null) {
                canLoad = true;
            }
            else {
                double enemyDist = unit.distTo(nearestEnemy);
                double enemyDistToBunker = nearestEnemy.distTo(bunker);

                if (enemyDistToBunker + 1.5 < unitDistToBunker) {
                    return false;
                }

                canLoad = enemyDist < 2.4 || !enemiesNear.onlyMelee() || unitDistToBunker <= 3.6;
            }

            if (canLoad) {
                unit.load(bunker);

                String t = "GetToDaChoppa";
                unit.setTooltipTactical(t);
                unit.addLog(t);
                return true;
            }
        }

        return false;
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
