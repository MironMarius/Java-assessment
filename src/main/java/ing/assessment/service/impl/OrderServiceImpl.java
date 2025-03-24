package ing.assessment.service.impl;

import ing.assessment.db.order.Order;
import ing.assessment.db.order.OrderProduct;
import ing.assessment.db.product.Product;
import ing.assessment.db.repository.OrderRepository;
import ing.assessment.db.repository.ProductRepository;
import ing.assessment.exception.OutOfStockException;
import ing.assessment.exception.ProductNotFoundException;
import ing.assessment.service.OrderService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderServiceImpl(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    @Override
    public Order placeOrder(Map<Integer, Integer> productIdsToQuantity) throws Exception {
        Map<Product, Integer> productToQuantity = new HashMap<>();

        for(Map.Entry<Integer, Integer> entry : productIdsToQuantity.entrySet()) {
            Integer productId = entry.getKey();
            Integer desiredQuantity = entry.getValue();

            List<Product> products = productRepository.findByProductCk_Id(productId);

            if (products.isEmpty()) {
                throw new ProductNotFoundException("Product with ID " + productId + " not found");
            }

            Product product = products.get(0);

            if (product.getQuantity() < desiredQuantity) {
                throw new OutOfStockException("There is not enough stock of Product: " + product.getName() + ", remaining stock: " + product.getQuantity());
            }

            productToQuantity.put(product, desiredQuantity);
        }

        return createOrder(productToQuantity);
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public Optional<Order> getOrderById(Integer orderId) {
        return orderRepository.findById(orderId);
    }

    private Order createOrder(Map<Product, Integer> productToQuantity) {
        List<OrderProduct> orderProducts = new ArrayList<>();
        double orderCost = 0.0;

        for (Map.Entry<Product, Integer> entry : productToQuantity.entrySet()) {
            Product product = entry.getKey();
            Integer quantity = entry.getValue();

            orderProducts.add(
                    new OrderProduct(
                        product.getProductCk().getId(),
                        quantity,
                        product.getName(),
                        product.getPrice() * quantity)
            );

            orderCost += product.getPrice() * quantity;

            product.setQuantity(product.getQuantity() - quantity);
            productRepository.save(product);
        }

        int deliveryCost = computeDeliveryCost(orderCost);
        int deliveryTime = computeDeliveryTime(
                new ArrayList<>(productToQuantity.keySet())
        );
        double finalOrderCost = computeFinalOrderCost(orderCost);

        Order order = new Order();
        order.setTimestamp(new Date());
        order.setOrderProducts(orderProducts);
        order.setDiscount(orderCost - finalOrderCost);
        order.setOrderCost(finalOrderCost);
        order.setDeliveryCost(deliveryCost);
        order.setDeliveryTime(deliveryTime);

        return orderRepository.save(order);
    }

    private int computeDeliveryCost(Double orderCost) {
        if (orderCost <= 500) return 30;

        return 0;
    }

    private double computeFinalOrderCost(Double orderCost) {
        if (orderCost > 1000) return orderCost * 0.9;

        return orderCost;
    }

    private int computeDeliveryTime(List<Product> products) {
        long uniqueLocations = products.stream()
                .map(product -> product.getProductCk().getLocation())
                .distinct()
                .count();

        return (int) uniqueLocations * 2;
    }
}