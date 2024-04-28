package atlantis.combat.retreating.terran;

import atlantis.architecture.Manager;
import atlantis.decisions.Decision;
import atlantis.game.A;
import atlantis.information.generic.OurArmy;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;

public class TerranInfantryShouldRetreat extends Manager {
    public TerranInfantryShouldRetreat(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isTerranInfantry();
    }

    public Decision shouldRetreat() {
        if (!applies()) return null;

        Selection enemies = enemies(unit);

        if (shouldSmallScaleRetreat(unit, enemies)) return Decision.TRUE;
        if (shouldEarlyGameRetreat()) return Decision.TRUE;
        if (shouldRetreatFromCombatBuildings()) return Decision.TRUE;

        if (!unit.mission().isMissionDefend()) {
            if (
                unit.enemiesNear().ranged().notEmpty()
                    && unit.friendsNear().atMost(4) && unit.combatEvalRelative() <= 2.7
            ) {
                unit.setTooltipTactical("BewareRanged");
                return Decision.TRUE;
            }
        }

        return null;
    }

    private static Selection enemies(AUnit unit) {
        return unit.enemiesNear()
            .ranged()
            .canAttack(unit, 6);
    }

    private static boolean shouldSmallScaleRetreat(AUnit unit, Selection enemies) {
        if (Enemy.terran()) {
            if (unit.isMarine()) {
                if (unit.friendsNear().inRadius(5, unit).count() < unit.enemiesNear().inRadius(5, unit).count()) {
                    unit.addLog("MvM-SmRetreat");
                    return true;
                }
            }
        }

        return false;
    }

    private boolean shouldEarlyGameRetreat() {
        return unit.isMissionAttack()
            && A.seconds() <= 350
            && OurArmy.strength() <= 94;
    }

    private boolean shouldRetreatFromCombatBuildings() {
        if (!unit.isMissionAttack()) return false;
        if (unit.hp() >= 125 && unit.groundWeaponRange() > 6.5) return false;

        return unit.enemiesNear().combatBuildings(false).canAttack(unit, 8.5).notEmpty();
    }
}
