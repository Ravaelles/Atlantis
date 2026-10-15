package atlantis.decisions.board;

/**
 * Immutable input snapshot for the goal board. Pure data, no engine reads:
 * tests construct it by hand, production builds it from the live game in
 * exactly one place (the adapter). The board never asks anything twice -
 * what a goal needs is a field here.
 */
public final class BoardState {

    public final int minerals;
    public final int gas;
    public final int supplyUsed;
    public final int supplyTotal;
    public final int workerCount;
    public final int baseCount;
    public final int gatewayCount;
    public final int gatewaysTraining;
    public final int combatUnits;
    public final boolean hasCyberneticsCore;
    public final boolean hasAssimilator;
    public final boolean hasForge;
    public final int cannonCount;
    public final int observerCount;
    public final boolean enemyHasHiddenUnits;
    public final boolean baseUnderAttack;
    public final int secondsElapsed;

    private BoardState(Builder builder) {
        this.minerals = builder.minerals;
        this.gas = builder.gas;
        this.supplyUsed = builder.supplyUsed;
        this.supplyTotal = builder.supplyTotal;
        this.workerCount = builder.workerCount;
        this.baseCount = builder.baseCount;
        this.gatewayCount = builder.gatewayCount;
        this.gatewaysTraining = builder.gatewaysTraining;
        this.combatUnits = builder.combatUnits;
        this.hasCyberneticsCore = builder.hasCyberneticsCore;
        this.hasAssimilator = builder.hasAssimilator;
        this.hasForge = builder.hasForge;
        this.cannonCount = builder.cannonCount;
        this.observerCount = builder.observerCount;
        this.enemyHasHiddenUnits = builder.enemyHasHiddenUnits;
        this.baseUnderAttack = builder.baseUnderAttack;
        this.secondsElapsed = builder.secondsElapsed;
    }

    public int supplyFree() {
        return supplyTotal - supplyUsed;
    }

    public boolean hasDetection() {
        return observerCount > 0 || cannonCount > 0;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private int minerals;
        private int gas;
        private int supplyUsed;
        private int supplyTotal;
        private int workerCount;
        private int baseCount = 1;
        private int gatewayCount;
        private int gatewaysTraining;
        private int combatUnits;
        private boolean hasCyberneticsCore;
        private boolean hasAssimilator;
        private boolean hasForge;
        private int cannonCount;
        private int observerCount;
        private boolean enemyHasHiddenUnits;
        private boolean baseUnderAttack;
        private int secondsElapsed;

        public Builder minerals(int minerals) {
            this.minerals = minerals;
            return this;
        }

        public Builder gas(int gas) {
            this.gas = gas;
            return this;
        }

        public Builder supply(int used, int total) {
            this.supplyUsed = used;
            this.supplyTotal = total;
            return this;
        }

        public Builder workers(int workerCount) {
            this.workerCount = workerCount;
            return this;
        }

        public Builder bases(int baseCount) {
            this.baseCount = baseCount;
            return this;
        }

        public Builder gateways(int gatewayCount) {
            this.gatewayCount = gatewayCount;
            return this;
        }

        public Builder gatewaysTraining(int gatewaysTraining) {
            this.gatewaysTraining = gatewaysTraining;
            return this;
        }

        public Builder army(int combatUnits) {
            this.combatUnits = combatUnits;
            return this;
        }

        public Builder core(boolean hasCyberneticsCore) {
            this.hasCyberneticsCore = hasCyberneticsCore;
            return this;
        }

        public Builder assimilator(boolean hasAssimilator) {
            this.hasAssimilator = hasAssimilator;
            return this;
        }

        public Builder forge(boolean hasForge) {
            this.hasForge = hasForge;
            return this;
        }

        public Builder cannons(int cannonCount) {
            this.cannonCount = cannonCount;
            return this;
        }

        public Builder observers(int observerCount) {
            this.observerCount = observerCount;
            return this;
        }

        public Builder hiddenUnits(boolean enemyHasHiddenUnits) {
            this.enemyHasHiddenUnits = enemyHasHiddenUnits;
            return this;
        }

        public Builder underAttack(boolean baseUnderAttack) {
            this.baseUnderAttack = baseUnderAttack;
            return this;
        }

        public Builder seconds(int secondsElapsed) {
            this.secondsElapsed = secondsElapsed;
            return this;
        }

        public BoardState build() {
            return new BoardState(this);
        }
    }
}
