package atlantis.combat.squad.mission;

import atlantis.combat.missions.Missions;
import atlantis.combat.squad.Squad;
import atlantis.game.A;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.strategy.GamePhase;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Selection;
import atlantis.util.We;

public class ChangeSquadToDefend extends SquadMissionChanger {
    public static boolean shouldChangeToDefend(Squad squad) {
        if (true) return false;

        if (squad.leader().eval() >= 1.15) return false;
        if (ArmyStrength.ourArmyRelativeStrength() >= 170) return false;

        boolean offensiveRole = squad.hasMostlyOffensiveRole();
        if (!offensiveRole) {
            if (
                medicsExhausted(squad)
                    || tooFewUnitsAndNotEarlyGame(squad)
            ) {
                return true;
            }
        }

        return weakerThanEnemy(squad)
            || asTerranFacingHugeEnemySquad(squad)
            || tooManyUnitsWoundedAsTerran(squad)
            || backOffFromLurkers(squad);
    }

    protected static boolean changeMissionToDefend(Squad squad, String reason) {
//        System.err.println("Change SQUAD to DEFEND - " + reason);
        squad.setMission(Missions.DEFEND);
        return true;
    }

    // =========================================================


    private static boolean tooFewUnitsAndNotEarlyGame(Squad squad) {
        if ((units.size() <= 11) && !GamePhase.isEarlyGame()) {
            return changeMissionToDefend(squad, "Too little squad (" + units.size() + ")");
        }

        return false;
    }

    private static boolean asTerranFacingHugeEnemySquad(Squad squad) {
        if (!We.terran()) return false;

        int tanks = units.tanks().count();
        int infantry = units.terranInfantryWithoutMedics().count();

        // Hydralisk fix
        int hydras = leader.enemiesNear().ofType(AUnitType.Zerg_Hydralisk).count();

        if (infantry * 0.4 < hydras && tanks <= 5) {
            changeMissionToDefend(squad, "Mass hydras (" + hydras + " vs " + units.count() + ")");
        }

        // Dragoon fix
        int dragoons = leader.enemiesNear().ofType(AUnitType.Protoss_Dragoon).count();
        if (infantry <= 2 && infantry * 0.6 < dragoons && tanks <= 5) {
            return changeMissionToDefend(squad, "Mass goons (" + hydras + " vs " + units.count() + ")");
        }

        return false;
    }

    private static boolean backOffFromLurkers(Squad squad) {
        AUnit unit = squad.leader();
        if (unit == null) return false;

        if (
            unit.enemiesNear().lurkers().inRadius(7, unit).notEmpty()
                && unit.friendsInRadiusCount(5) <= 20
        ) {
            return changeMissionToDefend(squad, "Back off from lurkers");
        }

        return false;
    }

    private static boolean tooManyUnitsWoundedAsTerran(Squad squad) {
        if (!We.terran()) return false;

        Selection units = squad.selection();
        double injuredRatio = (double) units.wounded().count() / units.count();
        if (injuredRatio >= 0.3) {
            return changeMissionToDefend(squad, "Too many injured (" + injuredRatio + ")");
        }

        return false;
    }

    private static boolean weakerThanEnemy(Squad squad) {
        AUnit unit = squad.leader();
        if (unit == null) return false;

        if (unit.eval() < 0.7) {
            return changeMissionToDefend(squad, "Weaker than enemy (" + unit.eval() + ")");
        }

        return false;
    }

    private static boolean medicsExhausted(Squad squad) {
        if (!We.terran()) return false;

        if (A.supplyUsed() >= 150) return false;

        if (ArmyStrength.ourArmyRelativeStrength() >= 250) return false;

        Selection medics = squad.selection().medics();
        if (medics.atLeast(2) && medics.havingEnergy(30).isEmpty()) {
            return changeMissionToDefend(squad, "Medics exhausted");
        }

        return false;
    }
}
