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
import atlantis.game.player.Enemy;

public class LoadIntoTheBunker extends Manager {

    private AUnit bunker;

    public LoadIntoTheBunker(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.isLoaded()) return false;
        if (Count.bunkers() == 0) return false;

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

    private boolean leaveOneMarineOut() {
        if (unit.hp() <= 28) return false;
        if (bunker.spaceRemaining() >= 4) return false;
        if (bunker.enemiesNear().countInRadius(6, unit) > 0) return false;

        return bunker.friendsNear().marines().countInRadius(3, unit) == 0;
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            ContinueLoadingIntoBunker.class
        };
    }

    @Override
    protected Manager handle() {
        bunker = bunkerToLoadTo();
        if (bunker == null) return null;

        if (leaveOneMarineOut()) return null;

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
                if (preventMaginotLine.invokeFrom(this) != null) {
                    return usedManager(preventMaginotLine);
                }

                unit.load(bunker);

                String t = "GetToDaChoppa";
                unit.setTooltipTactical(t);
                unit.addLog(t);
                return usedManager(this);
            }
        }

        return (new MoveToBunkerWhenCantLoadIntoIt(unit)).invokeFrom(this);
    }

    private boolean hasSpaceForThisUnit(AUnit unit, AUnit bunker) {
        if (!bunker.hasFreeSpaceFor(unit)) return false;

        if (unit.distTo(bunker) <= 0.8) return true;
        if (unit.meleeEnemiesNearCount(4) == 0) return true;

        return bunker.loadedUnits().size() <= 2
            || ((unit.isWounded() || unit.hasCooldown()) && unit.enemiesNearInRadius(1.8) == 0);
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
            .inRadius(AUnit.NEAR_DIST, unit)
            .havingSpaceFree(unit.spaceRequired())
            .nearestTo(unit);
    }


    private boolean wouldOverstack() {
        if (unit.hp() <= 18) return false;
        if (Count.marines() <= 5) return false;
        if (unit.squad().isLeader(unit)) return false;

        return unit.id() % 3 != 0
            && unit.friendsNear().marines().inRadius(1, unit).count() >= 2;
    }
}
