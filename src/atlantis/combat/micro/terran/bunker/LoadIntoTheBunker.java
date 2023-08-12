package atlantis.combat.micro.terran.bunker;

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

        if (Enemy.terran() && unit.isMissionDefend()) {
            return true;
        }

        if (
            GamePhase.isEarlyGame()
                && unit.noCooldown()
                && unit.hpMoreThan(20)
                && unit.enemiesNear().ranged().empty()
                && unit.nearestEnemyDist() >= 2
        ) return false;

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

        if (unit.lastActionLessThanAgo(5, Actions.LOAD)) return usedManager(this);

        if (bunker != null && bunker.hasFreeSpaceFor(unit)) {
            double unitDistToBunker = bunker.distTo(unit);
            double maxDistanceToLoad = maxDistanceToLoad();

            if (unitDistToBunker > maxDistanceToLoad) {
                return null;
            }

            boolean isItSafeToLoadIntoBunker = isItSafeToLoadIntoBunker(bunker, unitDistToBunker);

            if (isItSafeToLoadIntoBunker) {
                PreventMaginotLine preventMaginotLine = new PreventMaginotLine(unit);
                if (preventMaginotLine.handle() != null) {
                    return usedManager(preventMaginotLine);
                }

                unit.load(bunker);

                String t = "GetToDaChoppa";
                unit.setTooltipTactical(t);
                unit.addLog(t);
                return usedManager(this);
            }
        }

        return (new MoveToBunkerWhenCantLoadIntoIt(unit)).handle();
    }

    private double maxDistanceToLoad() {
        double base = Enemy.terran() ? 5.5 : 2.9;
        return base + unit.id() % 4 + (A.seconds() <= 400 ? 20 : 0);
    }

    private boolean isItSafeToLoadIntoBunker(AUnit bunker, double unitDistToBunker) {
        AUnit nearestEnemy = unit.nearestEnemy();
        if (nearestEnemy == null) {
            return true;
        }
        else {
            Selection enemiesNear = bunker.enemiesNear();

            if (enemiesNear.groundUnits().inRadius(1, bunker).atLeast(5)) {
                return false;
            }

            double enemyDist = unit.distTo(nearestEnemy);
            double enemyDistToBunker = nearestEnemy.distTo(bunker);

            if (enemyDist <= 4 && enemyDistToBunker + 1.5 < unitDistToBunker) return false;

            return enemyDist < 2.4 || !enemiesNear.onlyMelee() || unitDistToBunker <= 3.6;
        }
    }


    private AUnit bunkerToLoadTo() {
        return Select.ourOfType(AUnitType.Terran_Bunker)
            .inRadius(15, unit)
            .havingSpaceFree(unit.spaceRequired())
            .nearestTo(unit);
    }
}
