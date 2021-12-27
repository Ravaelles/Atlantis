package atlantis;

import atlantis.combat.squad.ASquadManager;
import atlantis.combat.squad.NewUnitsToSquadsAssigner;
import atlantis.enemy.EnemyInformation;
import atlantis.production.constructing.ConstructionOrder;
import atlantis.production.constructing.ConstructionOrderStatus;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.orders.ProductionQueueRebuilder;
import atlantis.ums.UmsSpecialActionsManager;
import atlantis.units.AUnit;
import bwapi.Unit;

public class OnUnitRenegade {

    /**
     * Called when a unit changes its AUnitType.
     * <p>
     * For example, when a Drone transforms into a Hatchery, a Siege Tank uses Siege Mode, or a Vespene Geyser
     * receives a Refinery.
     */
    public static void update(AUnit unit) {
        Unit u = unit.u();

        AUnit.forgetUnitEntirely(unit);
//        if (newUnit.type().isGasBuilding() || newUnit.type().isGeyser() || newUnit.isLarvaOrEgg()) {
//            return;
//        }

        // New unit taken from us
        if (unit.isOur()) {
            Atlantis.ourNewUnit(unit);
            System.out.println("NEW RENEGADE FOR US " + unit.name());
            UmsSpecialActionsManager.NEW_NEUTRAL_THAT_WILL_RENEGADE_TO_US = unit;
        }

        // Neutral means Refinery / Extractor
        else if (unit.isNeutral()) {
            Atlantis.ourNewUnit(unit);
            System.out.println("NEW RENEGADE FOR US " + unit.name());
            UmsSpecialActionsManager.NEW_NEUTRAL_THAT_WILL_RENEGADE_TO_US = unit;
        }

        // New unit for us e.g. some UMS maps give units
        else {
            if (unit.isOverlord()) {
                return;
            }

            Atlantis.enemyNewUnit(unit);
            System.out.println("NEW RENEGADE FOR ENEMY " + unit.name());
        }
    }
}