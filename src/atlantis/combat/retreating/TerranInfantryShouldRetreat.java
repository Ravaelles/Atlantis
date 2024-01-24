package atlantis.combat.retreating;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.information.generic.OurArmyStrength;
import atlantis.units.AUnit;

public class TerranInfantryShouldRetreat extends Manager {
    public TerranInfantryShouldRetreat(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isTerranInfantry();
    }

    public Manager shouldRetreat() {
        if (!applies()) return null;

        if (shouldEarlyGameRetreat()) return usedManager(this);

        if (shouldRetreatFromCombatBuildings()) return usedManager(this);

        if (!unit.mission().isMissionDefend()) {
            if (
                unit.enemiesNear().ranged().notEmpty()
                    && unit.friendsNear().atMost(4) && unit.combatEvalRelative() <= 2.7
            ) {
                unit.setTooltipTactical("BewareRanged");
                return usedManager(this);
            }
        }

        return null;
    }

    private boolean shouldEarlyGameRetreat() {
        return unit.isMissionAttack()
            && A.seconds() <= 350
            && OurArmyStrength.relative() <= 94;
    }

    private boolean shouldRetreatFromCombatBuildings() {
        if (!unit.isMissionAttack()) return false;
        if (unit.hp() >= 125 && unit.groundWeaponRange() > 6.5) return false;

        return unit.enemiesNear().combatBuildings(false).canAttack(unit, 8.5).notEmpty();
    }
}
