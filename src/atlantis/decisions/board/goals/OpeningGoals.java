package atlantis.decisions.board.goals;

import atlantis.decisions.board.BoardState;
import atlantis.decisions.board.Goal;
import atlantis.decisions.board.GoalRanks;

/**
 * Phase-1 goal vocabulary (Protoss opening). Each goal re-derives its
 * condition from game meaning with zero copy-paste from the legacy
 * producers (owner ruling 1A/3A): the legacy files stay readable for
 * reference, nothing is lifted.
 */
public final class OpeningGoals {

    private OpeningGoals() {
    }

    /** Blocked supply: nothing trains without it. */
    public static Goal supplyPylon() {
        return new Goal() {
            @Override
            public String id() {
                return "supply-pylon";
            }

            @Override
            public boolean active(BoardState state) {
                return state.baseCount > 0 && state.supplyFree() <= 2;
            }

            @Override
            public int rank() {
                return GoalRanks.SUPPLY;
            }
        };
    }

    /** Unseen enemy with no answer in place: preempts everything urgent. */
    public static Goal detection() {
        return new Goal() {
            @Override
            public String id() {
                return "detection";
            }

            @Override
            public boolean active(BoardState state) {
                return state.enemyHasHiddenUnits && !state.hasDetection();
            }

            @Override
            public int rank() {
                return GoalRanks.DETECTION;
            }
        };
    }

    /** Continuous economy until saturation. */
    public static Goal workers() {
        return new Goal() {
            @Override
            public String id() {
                return "workers";
            }

            @Override
            public boolean active(BoardState state) {
                return state.workerCount < workersWanted(state);
            }

            @Override
            public int rank() {
                return GoalRanks.WORKERS;
            }
        };
    }

    public static final int WORKERS_PER_BASE = 22;

    public static int workersWanted(BoardState state) {
        return WORKERS_PER_BASE * Math.max(1, state.baseCount);
    }

    /** Production capacity for the opening. */
    public static Goal gateway() {
        return new Goal() {
            @Override
            public String id() {
                return "gateway";
            }

            @Override
            public boolean active(BoardState state) {
                return state.gatewayCount < WANTED_GATEWAYS;
            }

            @Override
            public int rank() {
                return GoalRanks.GATEWAY;
            }
        };
    }

    public static final int WANTED_GATEWAYS = 2;

    /** Tech unlock for Dragoons, needs a Gateway first. */
    public static Goal core() {
        return new Goal() {
            @Override
            public String id() {
                return "core";
            }

            @Override
            public boolean active(BoardState state) {
                return !state.hasCyberneticsCore && state.gatewayCount >= 1;
            }

            @Override
            public int rank() {
                return GoalRanks.CORE;
            }
        };
    }

    /** Gas income, Core-first. */
    public static Goal assimilator() {
        return new Goal() {
            @Override
            public String id() {
                return "assimilator";
            }

            @Override
            public boolean active(BoardState state) {
                return !state.hasAssimilator && state.hasCyberneticsCore;
            }

            @Override
            public int rank() {
                return GoalRanks.ASSIMILATOR;
            }
        };
    }

    /** Opening fighting force. */
    public static Goal army() {
        return new Goal() {
            @Override
            public String id() {
                return "army";
            }

            @Override
            public boolean active(BoardState state) {
                return state.combatUnits < OPENING_ARMY_MIN;
            }

            @Override
            public int rank() {
                return GoalRanks.ARMY;
            }
        };
    }

    public static final int OPENING_ARMY_MIN = 6;

    /**
     * Second base once the economy can carry it and nothing presses.
     * Affordability is the wallet's job, not this predicate's.
     */
    public static Goal expansion() {
        return new Goal() {
            @Override
            public String id() {
                return "expansion";
            }

            @Override
            public boolean active(BoardState state) {
                return state.baseCount < 2
                    && state.workerCount >= EXPANSION_MIN_WORKERS
                    && !state.baseUnderAttack;
            }

            @Override
            public int rank() {
                return GoalRanks.EXPANSION;
            }
        };
    }

    public static final int EXPANSION_MIN_WORKERS = 12;
}
