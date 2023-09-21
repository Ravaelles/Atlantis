package atlantis.production.requests.zerg;

import atlantis.game.A;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.strategy.EnemyStrategy;
import atlantis.information.strategy.GamePhase;
import atlantis.map.position.HasPosition;
import atlantis.production.constructing.Construction;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.requests.AntiLandBuildingManager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;

public class ZergSunkenColony extends AntiLandBuildingManager {

    @Override
    public AUnitType type() {
        return AUnitType.Zerg_Sunken_Colony;
    }

    @Override
    public AUnitType typeToBuildFirst() {
        return AUnitType.Zerg_Creep_Colony;
    }

    @Override
    public int expected() {
        if (!Have.a(AUnitType.Zerg_Spawning_Pool)) {
            return 0;
        }

        int existing = existingWithUnfinished();

        if (existing >= 6) {
            return 6;
        }

        int ourArmyRelativeStrength = ArmyStrength.ourArmyRelativeStrength();

        if (GamePhase.isEarlyGame() && EnemyStrategy.get().isRushOrCheese()) {
            return 6;
        }

        if (GamePhase.isEarlyGame()) {
            if (ourArmyRelativeStrength < 100) {
                int missingStrengthToEquillibrium = 100 - ourArmyRelativeStrength;
                int moreNeeded = Math.min(3, (int) Math.ceil(missingStrengthToEquillibrium / 10.0));

                return existing + moreNeeded;
            }
        }

        return 3;
//        int expected = EnemyStrategy.get().isRushOrCheese() ? 3 : (Enemy.terran() ? 0 : 1);
//
//        return expected;
    }

    @Override
    public boolean shouldBuildNew() {
        if (A.seconds() <= (EnemyStrategy.get().isRushOrCheese() ? 0 : 200)) return false;

        int creepColonies = Count.existingOrInProductionOrInQueue(AUnitType.Zerg_Creep_Colony);

        int maxInProduction = 3;
        if (
            Count.inProductionOrInQueue(AUnitType.Zerg_Creep_Colony) >= maxInProduction
                || Count.inProductionOrInQueue(AUnitType.Zerg_Sunken_Colony) >= maxInProduction
        ) return false;

        if (creepColonies >= 1) {
            if (!A.hasMinerals(150)) return false;

            int mineralsNeeded = 50 + creepColonies * 75;

            if (!A.hasMinerals(mineralsNeeded)) return false;
        }

        return super.shouldBuildNew();
    }

    /**
     * There is a discrepancy between amount of Sunken Colonies and Sunken Colonies.
     * If there is a Creep Colony, morph it into a Sunken if needed.
     */
    public boolean handleExistingCreepColonyIfNeeded() {
        int creepColonies = Count.creepColonies();
        if (creepColonies <= 0) return false;

        for (AUnit colony : Select.ourOfType(AUnitType.Zerg_Creep_Colony).list()) {
//            if (existingWithUnfinished() < expected()) {
            if (A.hasMinerals(75)) {
                colony.morph(type());
                String tooltip = "Into" + type();
                colony.addLog(tooltip);
                colony.setTooltip(tooltip);
//                    System.err.println("---- Morph " + colony + " into >>> " + type());
            }
        }

        return false;
    }

    @Override
    public HasPosition nextBuildingPosition() {
        HasPosition standard = super.nextBuildingPosition();

        if (standard == null) {
            return null;
        }

        HasPosition existing = Select.ourWithUnfinished(type()).inRadius(8, standard).nearestTo(standard);

        if (existing == null) {
            Construction construction = ConstructionRequests.getNotStartedOfType(AUnitType.Zerg_Creep_Colony);
            if (construction != null) {
                existing = construction.buildPosition();
            }
        }

        if (existing != null) {
            return existing;
        }

        return standard;
    }

    @Override
    public int existingWithUnfinished() {
        return Count.existingOrInProductionOrInQueue(type())
            + Count.existingOrInProductionOrInQueue(AUnitType.Zerg_Creep_Colony);
    }

}
