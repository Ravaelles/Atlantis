package atlantis.game.listeners;


import atlantis.Atlantis;
import atlantis.battle.cluster.Missions;
import atlantis.config.env.Env;
import atlantis.debug.OurWorkerWasKilled;
import atlantis.game.A;
import atlantis.game.event.Event;
import atlantis.game.event.Events;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.ArmyStrength;
import atlantis.production.orders.production.queue.Queue;
import atlantis.terran.repair.RepairAssignments;
import atlantis.units.AUnit;

public class OnOurUnitDestroyed {
    public static void update(AUnit unit) {
        printOurDeadUnit(unit);
        atlantis.battle.twins.FightTrace.died(unit);

        RepairAssignments.removeRepairer(unit);
        OurWorkerWasKilled.onWorkedKilled(unit);
        if (unit.squad() != null) unit.squad().removeUnit(unit);

        Queue.get().refresh();

        if (!unit.type().isGasBuilding()) {
            Atlantis.LOST++;
            Atlantis.LOST_RESOURCES += unit.type().getTotalResources();

            if (unit.type().isABuilding()) Atlantis.LOST_BUILDINGS++;
        }

        // =========================================================

        if (
            unit.isMissionAttack()
                && A.s <= 60 * 7
                && ArmyStrength.ourArmyRelativeStrength() <= 85
                && !A.isUms()
                && EnemyUnits.discovered().ranged().count() >= 2
        ) {
            Missions.forceGlobalMissionDefend("Far too weak to attack!");
        }

        if (unit.isBunker()) Events.dispatch(Event.OUR_BUNKER_DESTROYED, unit);
    }

    private static void printOurDeadUnit(AUnit unit) {
        if (unit.isABuilding()) return;

        String prefix = "";
        String string1 = unit.managerLogs().toString();

        if (!Env.isLocal()) {
            prefix = "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t";

            string1 = string1.replace("\n", "\n" + prefix);
        }

        String leader = unit.isLeader() ? " LEADER!!!" : "";

        System.out.println(prefix + "_____________________________");
        System.out.println(prefix + A.minSec() + " - Our " + unit.typeWithUnitId() + " DIED [*]" + leader);
        System.out.println(prefix + "Tooltip: " + unit.tooltip());
        System.out.println(prefix + string1);
        System.out.println(prefix + "_____________________________");
    }
}
