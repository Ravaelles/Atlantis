package atlantis.combat.missions.defend.protoss;

import atlantis.Atlantis;
import atlantis.combat.missions.MissionChanger;
import atlantis.combat.retreating.RetreatManager;
import atlantis.decisions.Decision;
import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.OurArmy;
import atlantis.production.dynamic.protoss.tech.ResearchSingularityCharge;
import atlantis.units.select.Count;

public class ProtossShouldPunishZergEarly {
    public static Decision shouldPunishZergEarly() {
        int zealotsAndGoons;

        if (Count.dragoons() <= 2 && OurArmy.strength() <= 140) {
            if (MissionChanger.DEBUG) MissionChanger.reason = "WeakDespiteGoons(" + OurArmy.strength() + "%)";
            return Decision.FALSE;
        }

        if (enemyLooksVeryStrongEarlyGame()) return Decision.FALSE;

        if (
            A.s <= 600
                && (zealotsAndGoons = Count.zealotsAndDragoons()) >= 8
                && (Count.dragoons() >= EnemyUnits.hydras() * 2)
        ) {
            if (
                OurArmy.strength() >= 180 && (
                    (zealotsAndGoons * 2.5 >= EnemyUnits.discovered().combatUnits().count())
                        || (Count.dragoons() >= 1 && EnemyUnits.discovered().combatUnits().atMost(18))
                )
            ) {
                if (MissionChanger.DEBUG) MissionChanger.reason = "PunishZergEarly(" + OurArmy.strength() + "%)";
                return Decision.TRUE;
            }
        }

        return Decision.INDIFFERENT;
    }

    private static boolean enemyLooksVeryStrongEarlyGame() {
        return A.s <= 60 * 9
            && OurArmy.strength() <= 190
            && (RetreatManager.GLOBAL_RETREAT_COUNTER >= 2 || Atlantis.LOST_RESOURCES >= 250)
            && (Count.dragoons() <= 9 || EnemyInfo.hasRanged())
            && !ResearchSingularityCharge.isResearched();
    }
}
