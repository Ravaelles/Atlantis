package atlantis.combat.advance.focus_choke;

import atlantis.combat.missions.Missions;
import atlantis.game.A;
import atlantis.map.choke.AChoke;
import atlantis.map.path.PathToEnemyBase;

import java.util.ArrayList;

public class CurrentFocusChoke {
    private static int currentIndex = -1;
    private static AChoke currentChoke = null;

    public static AChoke get() {
//        System.out.println("PathToEnemyBase.chokesLeadingToEnemyBase() = " + PathToEnemyBase.chokesLeadingToEnemyBase().size());

        if (!Missions.isGlobalMissionAttack()) return null;
        if (A.minerals() >= 1500 || A.supplyUsed() >= 185) return null;

        if (currentChoke != null) {
            return currentChoke;
        }

//        System.err.println("currentChoke = " + currentChoke);
        return currentChoke = defineWhenNull();
    }

    private static AChoke defineWhenNull() {
        if (currentIndex == -1) currentIndex = 1;
        ArrayList<AChoke> chokes = PathToEnemyBase.chokesLeadingToEnemyBase();

        if (currentIndex < chokes.size()) return chokes.get(currentIndex);
        else return null;
    }

    public static boolean switchToNextIfPossible() {
        if (true) return false;

        AChoke next = CurrentFocusChoke.next();

        if (next != null) {
            CurrentFocusChoke.set(next);
            return true;
        }

        return false;
    }

    private static AChoke next() {
        ArrayList<AChoke> chokes = PathToEnemyBase.chokesLeadingToEnemyBase();

        if (currentIndex + 1 < chokes.size()) {
            currentIndex++;

            return chokes.get(currentIndex);
        }

        return null;
    }

    public static void set(AChoke newTarget) {
        currentChoke = newTarget;
    }

    public static boolean exists() {
        return currentChoke != null;
    }

    public static void resetChoke() {
        currentIndex = -1;
    }
}
