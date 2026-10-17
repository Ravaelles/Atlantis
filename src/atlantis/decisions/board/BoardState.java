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
    public final boolean coreAwaitingSupply;
    public final boolean hasAssimilator;
    public final boolean hasForge;
    public final int cannonCount;
    public final int observerCount;
    public final boolean enemyHasHiddenUnits;
    public final boolean baseUnderAttack;
    public final int secondsElapsed;
    public final String enemyRace;
    public final int dragoonCount;
    public final int dragoonsUnfinished;
    public final int armyStrength;
    public final int enemyHydras;
    public final int enemyLurkers;
    public final int enemyMutalisks;
    public final boolean enemyCarrierTech;
    public final boolean spartaMission;
    public final int wallPiecesAtNatural;
    public final int armyStrengthWithoutCB;
    public final boolean enemyHasRanged;
    public final boolean enemyGoingHidden;
    public final int gatewaysUnfinished;

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
        this.coreAwaitingSupply = builder.coreAwaitingSupply;
        this.hasAssimilator = builder.hasAssimilator;
        this.hasForge = builder.hasForge;
        this.cannonCount = builder.cannonCount;
        this.observerCount = builder.observerCount;
        this.enemyHasHiddenUnits = builder.enemyHasHiddenUnits;
        this.baseUnderAttack = builder.baseUnderAttack;
        this.secondsElapsed = builder.secondsElapsed;
        this.enemyRace = builder.enemyRace;
        this.dragoonCount = builder.dragoonCount;
        this.dragoonsUnfinished = builder.dragoonsUnfinished;
        this.armyStrength = builder.armyStrength;
        this.enemyHydras = builder.enemyHydras;
        this.enemyLurkers = builder.enemyLurkers;
        this.enemyMutalisks = builder.enemyMutalisks;
        this.enemyCarrierTech = builder.enemyCarrierTech;
        this.spartaMission = builder.spartaMission;
        this.wallPiecesAtNatural = builder.wallPiecesAtNatural;
        this.armyStrengthWithoutCB = builder.armyStrengthWithoutCB;
        this.enemyHasRanged = builder.enemyHasRanged;
        this.enemyGoingHidden = builder.enemyGoingHidden;
        this.gatewaysUnfinished = builder.gatewaysUnfinished;
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
        private boolean coreAwaitingSupply;
        private boolean hasAssimilator;
        private boolean hasForge;
        private int cannonCount;
        private int observerCount;
        private boolean enemyHasHiddenUnits;
        private boolean baseUnderAttack;
        private int secondsElapsed;
        private String enemyRace = "Unknown";
        private int dragoonCount;
        private int dragoonsUnfinished;
        private int armyStrength;
        private int enemyHydras;
        private int enemyLurkers;
        private int enemyMutalisks;
        private boolean enemyCarrierTech;
        private boolean spartaMission;
        private int wallPiecesAtNatural;
        private int armyStrengthWithoutCB;
        private boolean enemyHasRanged;
        private boolean enemyGoingHidden;
        private int gatewaysUnfinished;

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

        public Builder coreAwaitingSupply(boolean coreAwaitingSupply) {
            this.coreAwaitingSupply = coreAwaitingSupply;
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

        public Builder enemyRace(String enemyRace) {
            this.enemyRace = enemyRace;
            return this;
        }

        public Builder dragoons(int dragoonCount) {
            this.dragoonCount = dragoonCount;
            return this;
        }

        public Builder dragoonsUnfinished(int dragoonsUnfinished) {
            this.dragoonsUnfinished = dragoonsUnfinished;
            return this;
        }

        public Builder armyStrength(int armyStrength) {
            this.armyStrength = armyStrength;
            return this;
        }

        public Builder enemyUnits(int hydras, int lurkers, int mutalisks, boolean carrierTech) {
            this.enemyHydras = hydras;
            this.enemyLurkers = lurkers;
            this.enemyMutalisks = mutalisks;
            this.enemyCarrierTech = carrierTech;
            return this;
        }

        public Builder sparta(boolean spartaMission) {
            this.spartaMission = spartaMission;
            return this;
        }

        /**
         * Wall pieces (Pylon/Gateway/Cannon, standing or unfinished) near
         * the natural choke. 99 when no natural choke exists - a wall
         * nobody can serve is noise, not a want.
         */
        public Builder wallPieces(int wallPiecesAtNatural) {
            this.wallPiecesAtNatural = wallPiecesAtNatural;
            return this;
        }

        /** Army strength without static defense: rush math, not siege math. */
        public Builder armyStrengthWithoutCB(int armyStrengthWithoutCB) {
            this.armyStrengthWithoutCB = armyStrengthWithoutCB;
            return this;
        }

        /** Enemy has ranged units (DT prediction: ranged mirrors need no Forge first). */
        public Builder enemyHasRanged(boolean enemyHasRanged) {
            this.enemyHasRanged = enemyHasRanged;
            return this;
        }

        /** Strategy reads the enemy going hidden units (DT prediction). */
        public Builder enemyGoingHidden(boolean enemyGoingHidden) {
            this.enemyGoingHidden = enemyGoingHidden;
            return this;
        }

        /** Gateways still warping (Forge timing waits for production first). */
        public Builder gatewaysUnfinished(int gatewaysUnfinished) {
            this.gatewaysUnfinished = gatewaysUnfinished;
            return this;
        }

        public BoardState build() {
            return new BoardState(this);
        }
    }
}
