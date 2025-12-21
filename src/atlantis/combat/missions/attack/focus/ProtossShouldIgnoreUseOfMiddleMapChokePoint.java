package atlantis.combat.missions.attack.focus;

import atlantis.combat.squad.squads.alpha.Alpha;
import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.Army;
import atlantis.map.base.define.EnemyThirdBase;
import atlantis.production.dynamic.protoss.tech.ResearchSingularityCharge;
import atlantis.units.AUnit;
import atlantis.units.select.Count;

public class ProtossShouldIgnoreUseOfMiddleMapChokePoint {
    public static boolean ignore() {
        if (Enemy.protoss()) return ignoreVsProtoss();
        else if (Enemy.zerg()) return ignoreVsZerg();
        else if (Enemy.terran()) return ignoreVsTerran();

        return false;
    }

    private static boolean ignoreVsProtoss() {
        if (A.supplyUsed() >= 180 || A.hasMinerals(1000)) return true;

        if (
            A.resourcesBalance() <= 700 && Army.strengthWithoutCB() <= 250 && Count.ourCombatUnits() <= 20
        ) return false;

        if (
            A.supplyUsed() <= 160 && A.resourcesBalance() <= 600
                && EnemyInfo.hasRanged()
                && (EnemyUnits.dragoons() >= 2 || Army.strengthWithoutCB() <= 200)
        ) return false;

        if (A.resourcesBalance() >= 800 && Army.strengthWithoutCB() >= 200) return true;
        if (A.resourcesBalance() <= -100 && Count.ourCombatUnits() <= 15) return false;

//        if (EnemyInfo.hasRanged()) return false;
//        if (EnemyUnits.dragoons() <= 2) return false;

        return Army.strengthWithoutCB() >= 260;
    }

    private static boolean ignoreVsZerg() {
        int supplyUsed = A.supplyUsed();
        int strength = Army.strengthWithoutCB();

        if (supplyUsed >= 160 || A.hasMinerals(1000)) return true;
        if (Army.strengthWithoutCB() >= 400 || Count.darkTemplars() >= 1 || Count.reavers() >= 1) return true;

        if (useWhenZergHasCBs(supplyUsed)) return false;

        AUnit leader = Alpha.alphaLeader();
        if (Alpha.count() >= 4 && strength >= 300 && A.resourcesBalance() >= 100) {
            if (leader != null && leader.eval() >= 5) return true;
        }

        if (strength <= 450 || Count.ourCombatUnits() <= 25) return false;
        if (strength <= 160 && supplyUsed <= 160 && A.resourcesBalance() <= 500) return false;

        if (strength <= 200 && supplyUsed <= 130) {
            if (leader != null) {
                if (leader.squad().cohesionPercent() <= 75) return false;
            }
        }

        if (!EnemyExistingExpansion.found()) {
            if (
                (strength >= 150 || Alpha.count() >= 16)
                    && ResearchSingularityCharge.isResearched()
                    //                && EnemyUnits.ranged() >= 5
                    && Count.dragoons() >= 8
            ) return true;
        }

        return false;
    }

    private static boolean useWhenZergHasCBs(int supplyUsed) {
        return supplyUsed <= 150
            && EnemyInfo.combatBuildingsAntiLand() >= 2
            && Army.strength() <= 800
            && EnemyThirdBase.get() == null;
    }

    private static boolean ignoreVsTerran() {
        if (A.supplyUsed() >= 180 || A.hasMinerals(1000)) return true;

        return Army.strength() >= 80;
    }
}
