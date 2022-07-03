package jfap;

import java.util.Set;

public abstract class AJFAP {

    /**
     * Adds the unit to the simulator for player 1
     *
     * @param fu The FAPUnit to add
     */
    abstract void addUnitPlayer1(JFAPUnit fu);

    /**
     * Adds the unit to the simulator for player 1, only if it is a combat unit
     *
     * @param fu The FAPUnit to add
     */
    abstract void addIfCombatUnitPlayer1(JFAPUnit fu);

    /**
     * Adds the unit to the simulator for player 2
     *
     * @param fu The FAPUnit to add
     */
    abstract void addUnitPlayer2(JFAPUnit fu);

    /**
     * Adds the unit to the simulator for player 2, only if it is a combat unit
     *
     * @param fu The FAPUnit to add
     */
    abstract void addIfCombatUnitPlayer2(JFAPUnit fu);

    /**
     * Starts the simulation. You can run this function multiple times. Feel free to run once, get the state and keep running.
     *
     * @param nFrames the number of frames to simulate. A negative number runs the sim until combat is over.
     */
    abstract void simulate(int nFrames); // = 24*4, 4 seconds on fastest

    /**
     * Starts the simulation. You can run this function multiple times. Feel free to run once, get the state and keep running.
     * Uses default number of frames, nFrames = 96
     */
    abstract public void simulate();

    /**
     * Default score calculation function
     *
     * @return Returns the score for alive units, for each player
     */
    abstract MutablePair<Integer, Integer> playerScores();

    /**
     * Default score calculation, only counts non-buildings.
     *
     * @return Returns the score for alive non-buildings, for each player
     */
    abstract MutablePair<Integer, Integer> playerScoresUnits();

    /**
     * Default score calculation, only counts buildings.
     *
     * @return Returns the score for alive buildings, for each player
     */
    abstract MutablePair<Integer, Integer> playerScoresBuildings();

    /**
     * Gets the internal state of the simulator. You can use this to get any info about the unit participating in the simulation or edit the state.
     *
     * @return Returns a pair of pointers, where each pointer points to a vector containing that player's units.
     */
    abstract MutablePair<Set<JFAPUnit>, Set<JFAPUnit>> getState();

    /**
     * Clears the simulation. All units are removed for both players. Equivalent to reconstructing.
     */
    abstract void clear();
}
