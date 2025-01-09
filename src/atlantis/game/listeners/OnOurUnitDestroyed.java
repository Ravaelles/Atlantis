package atlantis.game.listeners;

import atlantis.Atlantis;
import atlantis.combat.missions.Missions;
import atlantis.combat.squad.transfers.SquadTransfersCommander;
import atlantis.config.env.Env;
import atlantis.debug.OurWorkerWasKilled;
import atlantis.game.A;
import atlantis.game.event.Events;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.ArmyStrength;
import atlantis.production.orders.production.queue.Queue;
import atlantis.terran.repair.RepairAssignments;
import atlantis.units.AUnit;

public class OnOurUnitDestroyed {
    public static void update(AUnit unit) {
        printOurDeadUnit(unit);

        RepairAssignments.removeRepairer(unit);

        Queue.get().refresh();

        if (!unit.type().isGasBuilding()) {
            Atlantis.LOST++;
            Atlantis.LOST_RESOURCES += unit.type().getTotalResources();

            if (unit.type().isABuilding()) Atlantis.LOST_BUILDINGS++;
        }

        OurWorkerWasKilled.onWorkedKilled(unit);
        SquadTransfersCommander.removeUnitFromSquads(unit);

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

        Events.dispatch("OurBunkerDestroyed", unit);
    }

    private static void printOurDeadUnit(AUnit unit) {
//        if (!unit.type().isDragoon()) return;
        if (!unit.isCombatUnit()) return;
        if (A.s >= 60 * 7.5 && !unit.isRanged()) return;

        String prefix = "";
        String string1 = unit.managerLogs().toString();
        String string2 = unit.log().toString();

        if (!Env.isLocal()) {
            prefix = "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t";

            string1 = string1.replace("\n", "\n" + prefix);
            string2 = string2.replace("\n", "\n" + prefix);
        }

        System.out.println(prefix + "_____________________________");
        System.out.println(prefix + A.minSec() + " - Our " + unit.type().name() + " DIED [*]");
        System.out.println(prefix + string1);
        System.out.println(prefix + "---");
        System.out.println(prefix + string2);
        System.out.println(prefix + "_____________________________");
    }
}
