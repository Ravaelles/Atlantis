package atlantis.combat.retreating.protoss.should;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.AttackNearbyEnemies;
import atlantis.game.A;
import atlantis.information.strategy.OurStrategy;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Have;
import atlantis.units.select.Select;

public class ProtossShouldNotRetreat extends Manager {
    public ProtossShouldNotRetreat(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.enemiesNear().visibleOnMap().notEmpty()
            && shouldNotRetreat();
    }

    @Override
    protected Manager handle() {
        if ((new AttackNearbyEnemies(unit)).invoke(this) != null) return usedManager(this);

        return null;
    }

    public boolean shouldNotRetreat() {
        if (shouldNotRunInMissionSparta(unit)) {
            unit.addLog("NoRunInSparta");
            return false;
        }

        if (shouldNotRunInMissionDefend(unit)) {
            unit.addLog("NoRunInDefend");
            return false;
        }

        if (shouldNotRunInMissionAttack(unit)) {
            unit.addLog("NoRunInAttack");
            return false;
        }

        return false;
    }

    private static boolean shouldNotRunInMissionSparta(AUnit unit) {
        return unit.isMissionSparta();
    }

    private static boolean shouldNotRunInMissionDefend(AUnit unit) {
        return unit.isMissionDefend()
            && unit.hp() >= 41
            && unit.combatEvalRelative() >= 0.7
            && (
            unit.distToBase() <= 6
                ||
                closeToCombatBuilding(unit)
        );
    }

    private static boolean shouldNotRunInMissionAttack(AUnit unit) {
        if (!unit.isMissionAttack()) return false;

        if (unit.isZealot()) {
            AUnit nearestGoon = unit.friendsNear().dragoons().inRadius(4, unit).nearestTo(unit);
            if (nearestGoon == null) return false;

            return !nearestGoon.isRetreating();
        }

        return false;
    }

    private static boolean closeToCombatBuilding(AUnit unit) {
        return unit.hp() > 20
            && unit.friendsNear().combatBuildings(false).inRadius(3, unit).notEmpty();
    }
}
