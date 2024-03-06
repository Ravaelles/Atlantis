package atlantis.combat.advance.contain.protoss;

import atlantis.combat.advance.contain.DontAdvanceButHoldAndContainWhenEnemyBuildingsClose;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.game.A;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.OurArmyStrength;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.units.AUnit;
import atlantis.util.We;

public class AppliesContainForProtoss {
    private final ContainAsProtoss manager;
    private final AUnit unit;
    public AChoke enemyMainChoke;
    public AChoke enemyNaturalChoke;

    public AppliesContainForProtoss(ContainAsProtoss manager) {
        this.manager = manager;
        this.unit = this.manager.unit();
    }

    public boolean applies() {
        if (!We.protoss()) return false;
        if (weAreTooStrongToJustContain()) return false;

        return closeToEnemyBuildingsOrChoke()
            && (noPlentyOfFriendsNearby() || enemyHasDefences());
    }

    public boolean closeToEnemyBuildingsOrChoke() {
        return EnemyUnits.discovered().buildings().inRadius(minDistToEnemyBuilding(), unit).notEmpty()
            && closeToEnemyChokes();
    }

    public boolean closeToEnemyChokes() {
        enemyMainChoke = Chokes.enemyMainChoke();
        int allowedDistToChoke = 8;

        return
            (
                enemyMainChoke != null && unit.distTo(enemyMainChoke) < allowedDistToChoke
            )
                ||
                (
                    (enemyNaturalChoke = Chokes.enemyNaturalChoke()) != null
                        && unit.distTo(enemyNaturalChoke) < allowedDistToChoke
                );
    }

    public double minDistToEnemyBuilding() {
        return 16;
    }

    protected boolean enemyHasDefences() {
        boolean isNotVeryStrong = A.supplyUsed() <= 185 && unit.friendsNear().atMost(25);

        return isNotVeryStrong && unit.enemiesNear().combatBuildingsAntiLand().notEmpty();
    }

    protected boolean noPlentyOfFriendsNearby() {
        return unit.friendsNear().inRadius(8, unit).atMost(10);
    }

    protected boolean weAreTooStrongToJustContain() {
        if (A.supplyUsed() > 185) return true;
        if (A.minerals() > 2500) return true;

        if (
            (OurArmyStrength.relative() >= 800 || Alpha.count() >= 32)
                && unit.friendsNear().combatUnits().atLeast(18)
        ) return true;

//        if (unit.friendsNear().combatUnits().inRadius(10, unit).atMost(8)) return false;

        return false;
    }
}
