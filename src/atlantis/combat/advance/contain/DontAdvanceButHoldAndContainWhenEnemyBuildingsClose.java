package atlantis.combat.advance.contain;

import atlantis.architecture.Manager;
import atlantis.combat.advance.contain.protoss.AppliesContainForProtoss;
import atlantis.combat.advance.contain.protoss.ContainAsProtoss;
import atlantis.combat.advance.contain.terran.AppliesContainForTerran;
import atlantis.combat.advance.contain.terran.ContainAsTerran;
import atlantis.combat.missions.MissionManager;
import atlantis.information.enemy.EnemyUnits;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.units.AUnit;
import atlantis.util.We;

public class DontAdvanceButHoldAndContainWhenEnemyBuildingsClose extends MissionManager {
    private static final int DIST_TO_ENEMY_MAIN_CHOKE = 9;

    private final AppliesContainForTerran appliesForTerran = new AppliesContainForTerran(this);
//    private final AppliesContainForProtoss appliesForProtoss = new AppliesContainForProtoss(this);

    public AChoke enemyMainChoke;
    public AChoke enemyNaturalChoke;
    public int tanks;

    public DontAdvanceButHoldAndContainWhenEnemyBuildingsClose(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return appliesForTerran.applies();
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
        enemyMainChoke = Chokes.enemyMainChoke();

        return
            (
                enemyMainChoke != null && unit.distTo(enemyMainChoke) < DIST_TO_ENEMY_MAIN_CHOKE
            )
                ||
                (
                    (enemyNaturalChoke = Chokes.enemyNaturalChoke()) != null
                        && unit.distTo(enemyNaturalChoke) < 8
                );
    }

    public double minDistToEnemyBuilding() {
        if (We.protoss()) return 15;

        return unit.isTank() ? 11.8 : 13.3;
    }
}
