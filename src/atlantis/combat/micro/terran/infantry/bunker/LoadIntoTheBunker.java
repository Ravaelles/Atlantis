package atlantis.combat.micro.terran.infantry.bunker;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.information.strategy.GamePhase;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;

public class LoadIntoTheBunker extends Manager {
    public LoadIntoTheBunker(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.isLoaded()) return false;
        if (wouldOverstack()) return false;

        if (Enemy.terran()) {
            if (unit.isMissionDefend() && unit.idIsOdd()) return true;
            if (unit.hp() <= 25 && unit.isMarine()) return true;
        }
        if (unit.isMissionDefend() && unit.distToFocusPoint() <= (5 + unit.id() % 5)) return true;

        int meleeEnemiesNearCount = unit.meleeEnemiesNearCount(2.2);

        if (GamePhase.isEarlyGame() && meleeEnemiesNearCount == 0) return false;
        if (A.seconds() <= 360 && unit.enemiesThatCanAttackMe(3).empty()) return false;

        // Without enemies around, don't do anything
        Selection enemiesNear = unit.enemiesNear().havingWeapon().inRadius(9, unit).canAttack(unit, 10);
        if (enemiesNear.excludeMedics().empty()) {
            return false;
        }

        return true;
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            ContinueLoadingIntoBunker.class
        };
    }

    @Override
    protected Manager handle() {
        AUnit bunker = bunkerToLoadTo();
//        double maxDistanceToLoad = Missions.isGlobalMissionDefend() ? 5.2 : 8.2;

        if (
            unit.hp() >= (Enemy.protoss() ? 18 : 6)
                && unit.lastActionLessThanAgo(15, Actions.LOAD)
        ) return usedManager(this);

        if (bunker != null && hasSpaceForThisUnit(unit, bunker)) {
            double unitDistToBunker = bunker.distTo(unit);
            double maxDistanceToLoad = maxDistanceToLoad();

            if (unitDistToBunker > maxDistanceToLoad) {
                return null;
            }

            boolean isItSafeToLoadIntoBunker = isItSafeToLoadIntoBunker(bunker, unitDistToBunker);

            if (isItSafeToLoadIntoBunker) {
                PreventMaginotLine preventMaginotLine = new PreventMaginotLine(unit);
                if (preventMaginotLine.invoke(this) != null) {
                    return usedManager(preventMaginotLine);
                }

                unit.load(bunker);

                String t = "GetToDaChoppa";
                unit.setTooltipTactical(t);
                unit.addLog(t);
                return usedManager(this);
            }
        }

        return (new MoveToBunkerWhenCantLoadIntoIt(unit)).invoke(this);
    }

    private boolean hasSpaceForThisUnit(AUnit unit, AUnit bunker) {
        return bunker.hasFreeSpaceFor(unit)
            && (unit.id() % 2 == 0 || bunker.loadedUnits().size() <= 2);
    }

    private double maxDistanceToLoad() {
        double base = Enemy.terran() ? 5.5 : 2.9;
        return base + unit.id() % 4 + (A.seconds() <= 400 ? 20 : 0);
    }

    private boolean isItSafeToLoadIntoBunker(AUnit bunker, double unitDistToBunker) {
        AUnit nearestEnemy = unit.nearestEnemy();
        if (nearestEnemy == null) return true;
        else {
            Selection enemiesNear = bunker.enemiesNear();

            if (enemiesNear.groundUnits().inRadius(1, bunker).atLeast(5)) return false;

            double enemyDist = unit.distTo(nearestEnemy);
            double enemyDistToBunker = nearestEnemy.distTo(bunker);

            if (enemyDist <= 2.8 && enemyDistToBunker + 1.9 < unitDistToBunker) return false;

            return enemyDist < 2.4 || !enemiesNear.onlyMelee() || unitDistToBunker <= 3.6;
        }
    }

    private AUnit bunkerToLoadTo() {
        return Select.ourOfType(AUnitType.Terran_Bunker)
            .inRadius(15, unit)
            .havingSpaceFree(unit.spaceRequired())
            .nearestTo(unit);
    }


    private boolean wouldOverstack() {
        if (unit.hp() <= 18) return false;
        if (Count.marines() <= 5) return false;
        if (unit.squad().isLeader(unit)) return false;

        return unit.id() % 3 != 0 && unit.friendsNear().marines().count() >= 6;
    }
}
