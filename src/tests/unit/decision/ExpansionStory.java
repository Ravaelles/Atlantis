package tests.unit.decision;

import atlantis.production.demand.ExpansionTiming;

/**
 * Natural-language builder over the expansion doctrine snapshot.
 * Numbers live here, sentences live in the test.
 */
final class ExpansionStory {

    private final ExpansionTiming.Snapshot in = new ExpansionTiming.Snapshot();

    private ExpansionStory() {
        in.minerals = 400;
        in.basesTotal = 1;
        in.basesInProduction = 0;
        in.basesPlanned = 1;
        in.basesUnfinished = 0;
        in.enemyZerg = true;
        in.armyStrength = 200;
        in.seconds = 400;
    }

    static ExpansionStory protoss() {
        return new ExpansionStory();
    }

    ExpansionStory minerals(int minerals) {
        in.minerals = minerals;
        return this;
    }

    ExpansionStory oneBase() {
        in.basesTotal = 1;
        in.basesPlanned = 1;
        return this;
    }

    ExpansionStory planned(int planned) {
        in.basesPlanned = planned;
        return this;
    }

    ExpansionStory oneBaseUnfinished() {
        in.basesTotal = 1;
        in.basesInProduction = 1;
        in.basesPlanned = 2;
        in.basesUnfinished = 1;
        return this;
    }

    ExpansionStory sevenBases() {
        in.basesTotal = 7;
        in.basesPlanned = 7;
        return this;
    }

    ExpansionStory quiet() {
        in.enemyMutalisks = 0;
        return this;
    }

    ExpansionStory mutas(int mutas) {
        in.enemyMutalisks = mutas;
        return this;
    }

    ExpansionStory cannons(int cannons) {
        in.cannons = cannons;
        return this;
    }

    ExpansionStory army(int strength) {
        in.armyStrength = strength;
        return this;
    }

    ExpansionStory earlyGame() {
        in.seconds = 300;
        return this;
    }

    ExpansionTiming.Verdict decide() {
        return ExpansionTiming.decide(in);
    }

    String says(String why) {
        return "expansion story (" + describe() + "): expected by rule - " + why
            + ", but decided " + decide();
    }

    private String describe() {
        return in.minerals + " minerals, " + in.basesTotal + " base(s), "
            + in.basesInProduction + " coming, army " + in.armyStrength
            + ", mutas " + in.enemyMutalisks + ", cannons " + in.cannons;
    }
}
