//package atlantis.production;
//
//import java.util.ArrayList;
//
//import jnibwapi.types.UnitType;
//
//public class ProductionQueue {
//
//	private static ArrayList<ProductionOrder> orders = new ArrayList<>();
//
//	// =========================================================
//
//	// =========================================================
//	// Protected methods
//
//	protected static ProductionOrder enqueue(ProductionOrder order) {
//		orders.add(order);
//		return order;
//	}
//
//	protected static ProductionOrder enqueue(UnitType unitType) {
//		ProductionOrder order = new ProductionOrder(unitType);
//		return enqueue(order);
//	}
//
// }
