package atlantis.combat.missions;

import java.util.ArrayList;

public class MissionHistory {
    protected static ArrayList<Mission> missionHistory = new ArrayList<>();

    public static ArrayList<Mission> get() {
        return missionHistory;
    }

    public static int numOfChanges() {
        return Math.max(0, missionHistory.size() - 1);
    }
}
