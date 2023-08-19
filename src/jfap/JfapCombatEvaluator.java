package jfap;

import atlantis.Atlantis;
import atlantis.combat.eval.AtlantisJfap;
import atlantis.units.AUnit;
import atlantis.units.fogged.AbstractFoggedUnit;
import tests.unit.FakeUnit;

/**
 * Uses JFAP (with modifications to make it comptabile with JBWAPI).
 */
public class JfapCombatEvaluator {
    private static final boolean PRINT_DEBUG = false;
//    private static final boolean PRINT_DEBUG = true;

//    public static boolean wouldLose(AUnit unit) {
//        if (unit.enemiesNear().empty()) {
//            return false;
//        }
//
//        Integer[] eval = fullEval(unit);
//        int myScoreDiff = eval[1];
//        int enemyScoreDiff = eval[2];
//
//        return myScoreDiff * 1.06 <= enemyScoreDiff;
//    }

    /**
     * Relative:
     * 1.2 - 20% better outcome than for enemy
     * 1.0 - equally strong
     * 0.9 - 10% worse outcome than for enemy
     * <p>
     * Absolute:
     * E.g. 121 (of arbitrary "score points")
     */
    public static double[] eval(AUnit unit) {
//        System.out.println(unit.friendsNear().print("Friends for " + unit));
//        System.err.println("Eval for: " + unit);
//        System.err.println(unit.enemiesNear().print("Enemies for " + unit));

        Integer[] eval = fullEval(unit);

        int myScoreDiff = eval[1];
        int enemyScoreDiff = eval[2];

        return new double[]{myScoreDiff, enemyScoreDiff};
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

        if (PRINT_DEBUG) {
            System.err.println(unit + "\n   score = " + scoreDiff + ", \n   myScoreDiff = " + myScoreDiff +
                ", \n   myLosses=" + myLosses + ", \n   enemyScoreDiff=" + enemyScoreDiff + "\n");
        }

        return new Integer[]{scoreDiff, myScoreDiff, enemyScoreDiff, myLosses, postSimFriendlyUnitCount};
    }

    // =========================================================

    protected static void addFriends(AUnit unit, JFAP simulator) {
//        if (unit.canMove()) {
        simulator.addUnitPlayer1(new JFAPUnit(unit)); // Adds a friendly unit to the simulator
//        }

        for (AUnit friend : unit.friendsNear().list()) {
            if (friend.u() != null || friend instanceof FakeUnit || friend instanceof AbstractFoggedUnit) {
                if (AtlantisJfap.isValidUnit(friend)) {
                    simulator.addUnitPlayer1(new JFAPUnit(friend));
                }
            }
        }
    }

    protected static void addEnemies(AUnit unit, JFAP simulator) {
        for (AUnit enemy : unit.enemiesNear().list()) {
            if (AtlantisJfap.isValidUnit(enemy)) {
//                System.err.println("Valid&Added /" + enemy + "/ as enemy for " + unit + " // " + enemy.notImmobilized());
                simulator.addUnitPlayer2(new JFAPUnit(enemy));
            }
        }
    }

}
