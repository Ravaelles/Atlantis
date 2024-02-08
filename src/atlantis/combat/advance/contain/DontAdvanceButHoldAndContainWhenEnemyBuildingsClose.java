package atlantis.combat.advance.contain;

import atlantis.architecture.Manager;
import atlantis.combat.advance.contain.protoss.AppliesForProtoss;
import atlantis.combat.advance.contain.protoss.ContainAsProtoss;
import atlantis.combat.advance.contain.terran.AppliesForTerran;
import atlantis.combat.advance.contain.terran.ContainAsTerran;
import atlantis.combat.micro.attack.AttackNearbyEnemies;
import atlantis.combat.micro.terran.tank.sieging.ForceSiege;
import atlantis.combat.missions.MissionManager;
import atlantis.information.enemy.EnemyUnits;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.util.We;

public class DontAdvanceButHoldAndContainWhenEnemyBuildingsClose extends MissionManager {
    private static final int DIST_TO_ENEMY_MAIN_CHOKE = 9;
    
    private final AppliesForTerran appliesForTerran = new AppliesForTerran(this);
    private final AppliesForProtoss appliesForProtoss = new AppliesForProtoss(this);

    public AChoke enemyMainChoke;
    public AChoke enemyNaturalChoke;
    public int tanks;

    public DontAdvanceButHoldAndContainWhenEnemyBuildingsClose(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return appliesForTerran.applies() || appliesForProtoss.applies();
    }


    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            ContainAsProtoss.class,
            ContainAsTerran.class,
        };
    }

    // =========================================================

    public boolean closeToEnemyBuildingsOrChoke() {
        return EnemyUnits.discovered().buildings().inRadius(minDistToEnemyBuilding(), unit).notEmpty()
            && closeToEnemyChokes();
    }

    public boolean closeToEnemyChokes() {
        return
            (
                (enemyMainChoke = Chokes.enemyMainChoke()) != null
                    && enemyMainChoke.distTo(unit) < DIST_TO_ENEMY_MAIN_CHOKE
            )
//                ||
//                (
//                    (enemyNaturalChoke = Chokes.enemyNaturalChoke()) != null
//                        && enemyNaturalChoke.distTo(unit) < 5
//                )
            ;
    }

    public double minDistToEnemyBuilding() {
        if (We.protoss()) return 15;

        return unit.isTank() ? 11.8 : 13.3;
    }
}
