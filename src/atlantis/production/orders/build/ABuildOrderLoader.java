package atlantis.production.orders.build;

import atlantis.config.AtlantisIgniter;
import atlantis.game.A;
import atlantis.information.strategy.AStrategy;
import atlantis.production.orders.production.ProductionOrder;

import java.io.File;
import java.util.ArrayList;


public class ABuildOrderLoader {

    final int NUMBER_OF_COLUMNS_IN_FILE = 2;

    /**
     * Directory that contains build orders.
     */
    public static final String BUILD_ORDERS_PATH = "AI/build_orders/";

    // =========================================================

    public static ABuildOrder getBuildOrderForStrategy(AStrategy strategy) {
//        System.out.println("AtlantisIgniter.getBwapiDataPath() = " + AtlantisIgniter.getBwapiDataPath());
//        System.out.println("BUILD_ORDERS_PATH = " + BUILD_ORDERS_PATH);

        String filePath = AtlantisIgniter.getBwapiDataPath()
            + BUILD_ORDERS_PATH + strategy.race() + "/"
            + strategy.name() + ".txt";

        File f = new File(filePath);
        if (!f.exists()) {
            String message = "\n### Build order file does not exist:\r\n"
                + f.getAbsolutePath()
                + "\r\n### Strategy: " + strategy
                + "\r\n### Quit ###";

            System.err.println("Current strategy: " + strategy);

            if (A.seconds() <= 1) {
                System.err.println(message);

                throw new RuntimeException(message);
            } else {
                System.err.println(message);
            }
        }

        ABuildOrderLoader loader = new ABuildOrderLoader();
        return loader.readBuildOrdersFromFile(strategy.race(), strategy.name(), filePath);
    }

    // =========================================================

    /**
     * Reads build orders from CSV file and converts them into ArrayList.
     */
    protected ABuildOrder readBuildOrdersFromFile(String race, String name, String filePath) {
        ABuildOrder buildOrder = ABuildOrderFactory.forRace(race, filePath);

        // Read file into 2D String array
        String buildOrdersFile = filePath;

        // Parse CSV
        String[][] loadedFile = A.loadFile(buildOrdersFile, NUMBER_OF_COLUMNS_IN_FILE, ";");

        // We can display file here, if we want to
        //displayLoadedFile(loadedFile);

        // =========================================================

        ArrayList<ProductionOrder> productionOrders = new ArrayList<>();

        int currentSupply = 4;
        for (String[] row : loadedFile) {
            ProductionOrder productionOrder = BuildOrderRowParser.parseCsvRow(row, buildOrder, currentSupply);
            if (productionOrder != null) {
                productionOrders.add(productionOrder);

                currentSupply = productionOrder.minSupply();
                if (productionOrder.unitType() != null) {
                    currentSupply += productionOrder.unitType().supplyNeeded();
                }
            }
        }

        buildOrder.useProductionOrdersLoadedFromFile(productionOrders);

        // =========================================================
        // Converts shortcut notations like:
        //        6 - Barracks
        //        8 - Supply Depot
        //        8 - Marine - x2
        //        Marine - x3
        //        15 - Supply Depot
        //
        // To full build order sequence like this:
        //   - SCV
        //   - SCV
        //   - Barracks
        //   - SCV
        //   - Supply Depot
        //   - Marine
        //   - Marine
        //   - Marine
        //   - Marine
        //   - Marine
        //   - SCV
        //   - SCV
        //   - SCV
        //   - SCV
        //   - Supply Depot
//        if (A.notUms()) {
//            buildOrder.print();
//        }

//        buildOrder.overrideProductionOrders(BuildOrderNotationConverter.convertNotation(buildOrder));
//
//        buildOrder.print();

        return buildOrder;
    }

    /**
     * Auxiliary method that can be run to see what was loaded from CSV file.
     */
    @SuppressWarnings("unused")
    protected void displayLoadedFile(String[][] loadedFile) {
        int rowCounter = 0;
        System.out.println();
        System.out.println("===== LOADED FILE =====");
        for (String[] rows : loadedFile) {
//            if (rowCounter == 0) {
//                rowCounter++;
//                continue;
//            }

            // =========================================================
            for (String value : rows) {
                System.out.print(value + " | ");
            }
            System.out.println();

            // =========================================================
            rowCounter++;
        }
        System.out.println("===== End of LOADED FILE ==");
        System.out.println();
    }

}
