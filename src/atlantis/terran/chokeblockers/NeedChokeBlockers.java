package atlantis.terran.chokeblockers;

import atlantis.Atlantis;
import atlantis.combat.missions.Missions;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.enemy.EnemyWhoBreachedBase;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.generic.OurArmy;
import atlantis.information.strategy.OurStrategy;
import atlantis.map.choke.AChoke;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.util.Enemy;
import atlantis.util.Vector;
import atlantis.util.We;

public class NeedChokeBlockers {
    private static AChoke choke;
    public static Vector translationVectorInRelationToChoke = null;

    public static boolean check() {
        if (We.protoss() && Enemy.zerg()) return false;
        if (We.zerg()) return false;
        if (Enemy.terran()) return false;

        if (We.protoss()) return forProtoss();
        if (We.terran()) return forTerran();

        return false;
    }

    private static boolean forProtoss() {
        if (Enemy.zerg()) return false;
        if (A.s >= 400) return false;
        if (Alpha.count() >= 5) return false;
        if (We.protoss() && !Missions.isGlobalMissionSparta()) return false;

//        if (A.supplyUsed() >= 45) return false;
        if (Missions.isGlobalMissionAttack()) return false;
        if (Count.basesWithUnfinished() >= 2) return false;
        if (EnemyWhoBreachedBase.notNull()) return false;

        if (Enemy.protoss()) {
            if (OurArmy.strength() >= 210) return false;
        }

        if (Missions.isGlobalMissionDefendOrSparta()) {
            if (choke == null) {
                choke = ChokeToBlock.get();
                translationVectorInRelationToChoke = ChokeToBlock.defineTranslationVector(choke);
            }
            return choke != null;
        }

        return false;
    }

    private static boolean forTerran() {
        if (OurStrategy.get().isRushOrCheese()) return false;

        if (AGame.notNthGameFrame(5)) return false;
        if (Missions.isGlobalMissionAttack()) return false;

        if (A.supplyUsed() >= 45) return false;

        choke = ChokeToBlock.get();
        if (choke == null) return false;

        if (AGame.killsLossesResourceBalance() >= 1800) return false;

        int bunkers = Count.ourWithUnfinished(AUnitType.Terran_Bunker);
//        if (bunkers <= 0) return false;
        if (bunkers >= 3 || (A.seconds() >= 450 && Count.bases() != 1)) return false;

        if (Count.tanks() >= 2) return false;

        int combatUnits = Count.ourCombatUnits();

        if (combatUnits >= 14 && ArmyStrength.ourArmyRelativeStrength() >= 270) return false;

        return Atlantis.KILLED <= 8 || combatUnits <= 25;
    }
}
