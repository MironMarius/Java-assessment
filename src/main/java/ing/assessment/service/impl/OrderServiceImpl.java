package ing.assessment.service.impl;

import ing.assessment.db.order.Order;
import ing.assessment.db.order.OrderProduct;
import ing.assessment.db.product.Product;
import ing.assessment.exception.OutOfStockException;
import ing.assessment.exception.ProductNotFoundException;
import ing.assessment.service.OrderService;
import org.springframework.stereotype.Service;

import java.util.Comparator;
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
    public Order placeOrder(Map<Integer, Integer> productIdsToQuantity)
            throws ProductNotFoundException, OutOfStockException {
        Map<Product, Integer> productToQuantity = new HashMap<>();

        for(Map.Entry<Integer, Integer> entry : productIdsToQuantity.entrySet()) {
            Integer productId = entry.getKey();
            Integer desiredQuantity = entry.getValue();

            List<Product> availableProducts = productRepository.findByProductCk_Id(productId)
                    .stream()
                    .sorted(Comparator.comparing(p -> p.getProductCk().getLocation()))
                    .toList();

            if (availableProducts.isEmpty()) {
                throw new ProductNotFoundException("Product with ID: " + productId + " not found");
            }

            int totalAvailable = availableProducts.stream()
                    .mapToInt(Product::getQuantity)
                    .sum();

            if (totalAvailable < desiredQuantity) {
                throw new OutOfStockException("There is not enough stock of Product with ID: " + productId + ", remaining stock: " + totalAvailable);
            }

            // allocate products until demand is satisfied
            // if one location runs out, allocate the turnover to the next location of the product
            int remainingToAllocate = desiredQuantity;
            for (Product product : availableProducts) {
                if (remainingToAllocate <= 0) break;

                int allocated = Math.min(remainingToAllocate, product.getQuantity());
                if (allocated > 0) {
                    productToQuantity.put(product, allocated);
                    remainingToAllocate -= allocated;
                }
            }
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