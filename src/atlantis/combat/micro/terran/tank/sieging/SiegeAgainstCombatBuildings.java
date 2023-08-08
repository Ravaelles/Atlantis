package atlantis.combat.micro.terran.tank.sieging;

import atlantis.architecture.Manager;
import atlantis.combat.micro.terran.tank.TankDecisions;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

public class SiegeAgainstCombatBuildings extends Manager {
    public static final double COMBAT_BUILDING_DIST_SIEGE = 11.95;
    private AUnit combatBuilding;

    public SiegeAgainstCombatBuildings(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        combatBuilding = Select.enemy()
            .combatBuildings(false)
            .inRadius(COMBAT_BUILDING_DIST_SIEGE, unit)
            .nearestTo(unit);

        return combatBuilding != null;
    }

    public Manager handle() {
        if (
            (
                unit.distToLessThan(combatBuilding, COMBAT_BUILDING_DIST_SIEGE)
                    && TankDecisions.canSiegeHere(unit, false)
            )
                || unit.distToLessThan(combatBuilding, 7.8)
        ) {
            return usedManager(ForceSiege.forceSiegeNow(this, "SiegeBuilding" + A.dist(unit, combatBuilding)));
        }

        return null;
    }
}
