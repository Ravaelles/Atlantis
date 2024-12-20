package atlantis.combat.missions.attack.focus;

import atlantis.combat.advance.focus.AFocusPoint;
import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.select.Select;
import atlantis.util.log.ErrorLog;

public class AttackEnemyExpansion {
    public boolean shouldFocusIt() {
        APosition expansion = expansion();

        if (expansion == null) return false;
        if (A.s <= 300) return false;

        boolean isVisible = expansion.isPositionVisible();

        return !isVisible || Select.enemyRealUnits().groundUnits().inRadius(10, expansion).notEmpty();
    }

    public AFocusPoint expansion() {
        HasPosition expansion = EnemyExistingExpansion.get();
        if (expansion == null) return null;

        if (!expansion.isWalkable()) {
            ErrorLog.printMaxOncePerMinute("Enemy expansion is not walkable: " + expansion);
            return null;
        }

//        A.errPrintln(A.s + "s:  ENEMY EXPANSION FOUND: " + expansion);

        if (
            expansion.isPositionVisible()
                && (
                Select.enemy().buildings().inRadius(15, expansion).empty()
            )
        ) return null;

        return new AFocusPoint(
            expansion,
            Select.mainOrAnyBuilding(),
            "AttackExpansion"
        );
    }
}
