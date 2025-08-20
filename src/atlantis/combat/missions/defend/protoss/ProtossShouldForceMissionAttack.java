package atlantis.combat.missions.defend.protoss;

import atlantis.combat.missions.MissionChanger;
import atlantis.combat.missions.attack.ProtossMissionChangerWhenAttack;
import atlantis.decisions.Decision;
import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.Army;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

import static atlantis.combat.missions.MissionChanger.forceMissionAttack;
import static atlantis.combat.missions.defend.protoss.ProtossMissionChangerWhenDefend.shouldEngageWithGoonsVsProtoss;

public class ProtossShouldForceMissionAttack extends ProtossMissionChangerWhenAttack {
    public static boolean shouldForce(int strength, int dragoons, int combatUnits) {
        if (A.minerals() >= 1000 && combatUnits >= 10 && strength >= 110) {
            MissionChanger.reason = "1K_minerals";
            return force();
        }

        if (combatUnits >= 25 && A.supplyUsed() >= 120 && strength >= 500) {
            MissionChanger.reason = "SuperStrongAnd" + combatUnits + "CU";
            return force();
        }

        // === vs Protoss ===========================================

        if (Enemy.protoss()) {
            if (shouldEngageWithGoonsVsProtoss(strength)) {
                MissionChanger.reason = "Engage with Goons! (" + strength + "%)";
                return forceMissionAttack(reason);
//                return Decision.TRUE;
            }
        }

        // === vs Terran ===========================================

        if (Enemy.terran()) {
            if (pressureTerranEarly()) {
                return forceMissionAttack("PressureTerranEarly");
            }

            if (tanksNearMain()) {
                return forceMissionAttack("TanksNearMain");
            }
        }

        // =========================================================

//        if (A.s >= 60 * 10 && Army.strength() >= 120 && EnemyUnitBreachedBase.noone()) {
        if (A.s >= 60 * 10 && (A.supplyUsed() >= 180 || A.minerals() >= 2000)) {
            MissionChanger.reason = "LateGame-attack";
            return force();
        }

        if (dragoons >= 20) {
            MissionChanger.reason = "Goon-attack";
            return force();
        }

        return false;
    }

    private static boolean tanksNearMain() {
        AUnit tank = EnemyUnits.discovered().tanks().groundNearestTo(Select.main());

        if (tank != null && tank.groundDistToMain() <= 60) {
            return true;
        }

        return false;
    }

    private static boolean pressureTerranEarly() {
        if (
            A.s <= 60 * 7 && Count.dragoons() >= 1
        ) {
            if (DEBUG) reason = "Pressure Terran early";
            return true;
        }

        return false;
    }

    private static boolean force() {
        return forceMissionAttack(MissionChanger.reason);
    }

    public static boolean shouldForce() {
        return shouldForce(
            Army.strengthWithoutCB(),
            Count.dragoons(),
            Count.ourCombatUnits()
        );
    }
}
