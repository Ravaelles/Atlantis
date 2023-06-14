package jfap;

import atlantis.Atlantis;
import atlantis.units.AUnit;
import atlantis.units.fogged.AbstractFoggedUnit;
import tests.unit.FakeUnit;

/**
 * Uses JFAP (with modifications to make it comptabile with JBWAPI).
 */
public class JfapCombatEvaluator {

    public static boolean wouldLose(AUnit unit) {
        if (unit.enemiesNear().empty()) {
            return false;
        }

        Integer[] eval = fullEval(unit);
        int myScoreDiff = eval[1];
        int enemyScoreDiff = eval[2];

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
    public static double[] eval(AUnit unit, boolean relativeToEnemy) {
//        System.out.println(unit.friendsNear().print("Friends for " + unit));
//        System.out.println(unit.enemiesNear().print("Enemies for " + unit));

        if (unit.enemiesNear().empty()) {
            return new double[] { 9876, 0 };
//            return 0;
        }

        Integer[] eval = fullEval(unit);
//        int score = eval[0];
        int myScoreDiff = eval[1];
        int enemyScoreDiff = eval[2];
//        int myScoreDiff = Math.abs(eval[1]);
//        int enemyScoreDiff = Math.abs(eval[2]);

//        System.out.println("my/enemy ScoreDiff = " + myScoreDiff + " // " + enemyScoreDiff);

        if (myScoreDiff == 0) {
            return new double[] { 9878, 0 };
        }

        return new double[] { myScoreDiff, enemyScoreDiff };
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
        int myScoreDiff = postSimScores.first - preSimScores.first;
        int enemyScoreDiff = postSimScores.second - preSimScores.second;
        int scoreDiff = myScoreDiff - enemyScoreDiff;

//        System.err.println(unit + "\n   score = " + scoreDiff + ", \n   myScoreDiff = " + myScoreDiff +
//            ", \n   myLosses=" + myLosses + ", \n   enemyScoreDiff=" + enemyScoreDiff + "\n");

        return new Integer[] { scoreDiff, myScoreDiff, enemyScoreDiff, myLosses, postSimFriendlyUnitCount };
    }

    // =========================================================

    private static void addFriends(AUnit unit, JFAP simulator) {
//        if (unit.canMove()) {
            simulator.addUnitPlayer1(new JFAPUnit(unit)); // Adds a friendly unit to the simulator
//        }

        for (AUnit friend : unit.friendsNear().list()){
            if (friend.u() != null || friend instanceof FakeUnit || friend instanceof AbstractFoggedUnit) {
//                if (friend.canMove()) {
                if (friend.hasPosition()) {
                    simulator.addUnitPlayer1(new JFAPUnit(friend));
                }
//                }
            }
        }
    }

    private static void addEnemies(AUnit unit, JFAP simulator) {
//        System.err.println("unit = " + unit);
//        unit.enemiesNear().print("addEnemies enemiesNear");
        for (AUnit enemy : unit.enemiesNear().list()){
//            System.err.println("enemy = " + enemy);
            if (enemy.u() != null || enemy instanceof FakeUnit || enemy instanceof AbstractFoggedUnit) {
                if (enemy.canMove() && enemy.hasPosition()) {
//                    System.err.println("Added /" + enemy + "/ as enemy for " + unit);
                    simulator.addUnitPlayer2(new JFAPUnit(enemy));
                }
            }
        }
    }

}
