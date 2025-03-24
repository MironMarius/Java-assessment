package ing.assessment.controller;

import ing.assessment.db.order.Order;
import ing.assessment.exception.InvalidOrderException;
import ing.assessment.exception.OutOfStockException;
import ing.assessment.exception.ProductNotFoundException;
import ing.assessment.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/place")
    public ResponseEntity<?> placeOrder(@RequestBody Map<Integer, Integer> productIdsToQuantity) throws Exception {
        if (productIdsToQuantity.isEmpty() || productIdsToQuantity.values().stream().anyMatch(qty -> qty <= 0)) {
            throw new InvalidOrderException("Invalid request, must provide productIds and positive quantities");
        }

        Order order = orderService.placeOrder(productIdsToQuantity);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrder(@PathVariable("id") Integer id) throws ProductNotFoundException {
        Optional<Order> order = orderService.getOrderById(id);

        if (order.isEmpty()) {
            throw new ProductNotFoundException("Product with id: " + id + " not found");
        }

        return ResponseEntity.ok(order.get());
    }

    @GetMapping("/all")
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();

        return ResponseEntity.ok(orders);
    }
}