
package atlantis.production.orders;

/**
 * Represents sequence of commands to produce units/buildings.
 */
public abstract class ABuildOrderFactory {

    public static ABuildOrder forRace(String race, String name) {
        if ("Protoss".equals(race)) {
            return new ProtossBuildOrder(name);
        } else if ("Terran".equals(race)) {
            return new TerranBuildOrder(name);
        } else if ("Zerg".equals(race)) {
            return new ZergBuildOrder(name);
        } else {
            throw new RuntimeException("Invalid race: " + race);
        }
    }

}
