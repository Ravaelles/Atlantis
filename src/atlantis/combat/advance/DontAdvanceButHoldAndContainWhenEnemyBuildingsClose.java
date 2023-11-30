package atlantis.combat.advance;

import atlantis.architecture.Manager;
import atlantis.combat.micro.terran.tank.sieging.ForceSiege;
import atlantis.combat.micro.terran.tank.sieging.WantsToSiege;
import atlantis.combat.missions.MissionManager;
import atlantis.game.A;
import atlantis.information.enemy.EnemyUnits;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.units.AUnit;
import atlantis.units.select.Count;

public class DontAdvanceButHoldAndContainWhenEnemyBuildingsClose extends MissionManager {

    public static final int DIST_TO_ENEMY_MAIN_CHOKE = 8;
    private AChoke enemyMainChoke;

    public DontAdvanceButHoldAndContainWhenEnemyBuildingsClose(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        int tanks = Count.tanks();

        return
            (A.supplyUsed() < 185 || tanks < 9)
                && A.minerals() < 2200
                && unit.isCombatUnit()
                && closeToEnemyBuildingsOrChoke();
    }

    private boolean closeToEnemyBuildingsOrChoke() {
        return EnemyUnits.discovered().buildings().inRadius(minDistToEnemyBuilding(), unit).notEmpty()
            && ((enemyMainChoke = Chokes.enemyMainChoke()) != null && enemyMainChoke.distTo(unit) < DIST_TO_ENEMY_MAIN_CHOKE);
    }

    private double minDistToEnemyBuilding() {
        return unit.isTank() ? 8.7 : 9.3;
    }

    protected Manager handle() {
//        System.err.println("@ " + A.now() + " - DontAdvanceButHoldAndContainWhenEnemyBuildingsClose " + unit);

        if (unit.isTank()) return asTank();

        return asNonTank();
    }

    private Manager asNonTank() {
        unit.holdPosition("Steady");
        return usedManager(this);
    }

    private Manager asTank() {
        if (unit.isSieged() && unit.lastSiegedAgo() <= 30 * (17 + unit.id() % 6)) {
            return usedManager(this, "StayHere");
        }

        if (unit.isTankUnsieged()) {
            WantsToSiege.wantsToSiegeNow(unit, "RemainHere");
            return usedManager(this);
        }

        return null;
    }
}
