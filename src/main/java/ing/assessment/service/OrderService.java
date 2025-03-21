package ing.assessment.service;

import ing.assessment.db.order.Order;
import java.util.List;
import java.util.Map;

public interface OrderService {
    Order placeOrder(Map<Integer, Integer> productIds) throws Exception;
    List<Order> getAllOrders();
    Order getOrderById(Integer orderId);
}