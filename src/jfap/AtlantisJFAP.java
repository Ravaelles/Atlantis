package jfap;

import atlantis.Atlantis;
import atlantis.units.AUnit;

public class AtlantisJFAP {

    public static boolean wouldLose(AUnit unit) {
        if (unit.enemiesNear().empty()) {
            return false;
        }

        Integer[] eval = fullEval(unit);
        int myScoreDiff = eval[0];
        int enemyScoreDiff = eval[1];

        return myScoreDiff * 1.06 <= enemyScoreDiff;
    }

    /**
     * Relative:
     * 1.2 - 20% better outcome than for enemy
     * 1.0 - equally strong
     * 0.9 - 10% worse outcome than for enemy
     *
     * Absolute:
     * E.g. 121 (of arbitrary "score points")
     */
    public static double eval(AUnit unit, boolean relativeToEnemy) {
        if (unit.enemiesNear().empty()) {
            return 9876;
        }

        Integer[] eval = fullEval(unit);
        int myScoreDiff = eval[0];
        int enemyScoreDiff = eval[1];

        if (relativeToEnemy) {
            return 1 + (double) (myScoreDiff / enemyScoreDiff);
        }

        return myScoreDiff;
    }

    private static Integer[] fullEval(AUnit unit) {
        JFAP simulator = new JFAP(Atlantis.game()); // Requires a 'BW' object to be passed by parameter
        simulator.clear(); // Before starting the simulations we need to clear the simulator

        addFriends(unit, simulator);
        addEnemies(unit, simulator);

        MutablePair<Integer, Integer> preSimScores = simulator.playerScores(); // We can get each player scores before the simulation starts
        int preSimFriendlyUnitCount = simulator.getState().first.size(); // Friendly unit count introduced to JFAP before the simulation starts
        simulator.simulate(50); // Starts simulating the combat, number of frames to simulate is passed by parameters, default is 96 frames

        // After the simulation we can get the post battle score and the number of units that died for each player
        // You can use this info to know if the combat will be favourable for you or not
        MutablePair<Integer, Integer> postSimScores = simulator.playerScores();
        int postSimFriendlyUnitCount = simulator.getState().first.size();
        int myLosses = preSimFriendlyUnitCount - postSimFriendlyUnitCount;
        int myScoreDiff = preSimScores.first - postSimScores.first;
        int enemyScoreDiff = preSimScores.second - postSimScores.second;
        int scoreDiff = myScoreDiff - enemyScoreDiff;

//        System.out.println(unit + " score = " + scoreDiff + ", myScoreDiff = " + myScoreDiff +
//            ", myLosses=" + myLosses + ", enemyScoreDiff=" + enemyScoreDiff);

        return new Integer[] { scoreDiff, myScoreDiff, enemyScoreDiff, myLosses, postSimFriendlyUnitCount };
    }

    // =========================================================

    private static void addFriends(AUnit unit, JFAP simulator) {
        simulator.addUnitPlayer1(new JFAPUnit(unit.u())); // Adds a friendly unit to the simulator
        for (AUnit friend : unit.friendsNear().list()){
            if (friend.u() != null) {
                simulator.addUnitPlayer1(new JFAPUnit(friend.u()));
            }
        }
    }

    private static void addEnemies(AUnit unit, JFAP simulator) {
        for (AUnit enemy : unit.enemiesNear().list()){
            if (enemy.u() != null) {
                simulator.addUnitPlayer2(new JFAPUnit(enemy.u()));
            }
        }
    }

}
