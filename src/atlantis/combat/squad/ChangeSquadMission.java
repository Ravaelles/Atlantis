package atlantis.combat.squad;

import atlantis.combat.missions.Missions;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Selection;
import atlantis.util.We;

public class ChangeSquadMission {
    private static AUnit unit;
    private static Selection units;

    protected static void changeSquadMissionifNeeded(Squad squad) {
        unit = squad.centerUnit();
        units = squad.selection();

        if (unit == null) {
            return;
        }

        if (squad.isMissionAttack()) {
            medicsExhausted(squad);
            weakerThanEnemy(squad);
            hugeEnemySquad(squad);
            tooManyUnitsWounded(squad);
            backOffFromLurkers(squad);
        }
    }

    private static void hugeEnemySquad(Squad squad) {

        // Hydralisk fix
        int hydras = unit.enemiesNear().ofType(AUnitType.Zerg_Hydralisk).count();
        if (units.terranInfantryWithoutMedics().count() * 0.8 < hydras && Count.tanks() <= 5) {
            changeMissionToDefend(squad, "Mass hydras (" + hydras + " vs " + units.count() + ")");
        }
    }

    private static void backOffFromLurkers(Squad squad) {
        AUnit unit = squad.centerUnit();
        if (unit == null) {
            return;
        }

        if (
            unit.enemiesNear().lurkers().inRadius(7, unit).notEmpty()
            && unit.friendsNearInRadius(5) <= 20
        ) {
            changeMissionToDefend(squad, "Back off from lurkers");
        }
    }

    private static void tooManyUnitsWounded(Squad squad) {
        if (We.protoss()) {
            return;
        }

        Selection units = squad.selection();
        double injuredRatio = (double) units.wounded().count() / units.count();
        if (injuredRatio >= 0.3) {
            changeMissionToDefend(squad, "Too many injured (" + injuredRatio + ")");
        }
    }

    private static void weakerThanEnemy(Squad squad) {
        AUnit unit = squad.centerUnit();
        if (unit == null) {
            return;
        }

        if (unit.combatEvalRelative() < 0.7) {
            changeMissionToDefend(squad, "Weaker than enemy (" + unit.combatEvalRelative() + ")");
        }
    }

    private static void medicsExhausted(Squad squad) {
        Selection medics = squad.selection().medics();
        if (medics.atLeast(2) && medics.havingEnergy(30).isEmpty()) {
            changeMissionToDefend(squad, "Medics exhausted");
        }
    }

    private static void changeMissionToDefend(Squad squad, String reason) {
//        System.err.println("Change SQUAD to DEFEND - " + reason);
        squad.setMission(Missions.DEFEND);
    }
}