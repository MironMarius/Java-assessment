package ing.assessment.service;

import ing.assessment.db.order.Order;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface OrderService {
    Order placeOrder(Map<Integer, Integer> productIds) throws Exception;
    List<Order> getAllOrders();
    Optional<Order> getOrderById(Integer orderId);
}