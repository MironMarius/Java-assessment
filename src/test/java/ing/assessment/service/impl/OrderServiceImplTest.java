package ing.assessment.service.impl;

import ing.assessment.controller.OrderController;
import ing.assessment.db.order.Order;
import ing.assessment.db.order.OrderProduct;
import ing.assessment.db.product.Product;
import ing.assessment.db.product.ProductCK;
import ing.assessment.service.impl.OrderRepository;
import ing.assessment.service.impl.ProductRepository;
import ing.assessment.exception.OutOfStockException;
import ing.assessment.exception.ProductNotFoundException;
import ing.assessment.model.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderServiceImpl orderService;



    private Product testProduct1;
    private Product testProduct2;
    private Map<Integer, Integer> testOrderItems;

    @BeforeEach
    void setUp() {
        testProduct1 = new Product(
                new ProductCK(1, Location.MUNICH),
                "Test Product 1",
                100.0,
                50
        );

        testProduct2 = new Product(
                new ProductCK(2, Location.COLOGNE),
                "Test Product 2",
                200.0,
                50
        );

        testOrderItems = new HashMap<>();
        // (productId, quantity)
        testOrderItems.put(1, 2);
        testOrderItems.put(2, 1);
    }

    @Test
    void test_PlaceOrder_Success() throws Exception {
        when(productRepository.findByProductCk_Id(1))
                .thenReturn(Collections.singletonList(testProduct1));
        when(productRepository.findByProductCk_Id(2))
                .thenReturn(Collections.singletonList(testProduct2));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.placeOrder(testOrderItems);

        assertNotNull(result);
        assertEquals(2, result.getOrderProducts().size());
        assertEquals(400.0, result.getOrderCost());
        assertEquals(30, result.getDeliveryCost());
        assertEquals(4, result.getDeliveryTime());

        assertEquals(48, testProduct1.getQuantity());
        assertEquals(49, testProduct2.getQuantity());

        verify(productRepository, times(2)).save(any(Product.class));
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void test_PlaceOrder_ProductNotFound_ThrowsException() {
        when(productRepository.findByProductCk_Id(anyInt()))
                .thenReturn(Collections.emptyList());

        ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class,
                () -> orderService.placeOrder(testOrderItems)
        );

        assertEquals("Product with ID: 1 not found", exception.getMessage());
        verify(productRepository, never()).save(any(Product.class));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void test_PlaceOrder_OutOfStock_ThrowsException() {
        testOrderItems.put(1, 51);

        when(productRepository.findByProductCk_Id(1))
                .thenReturn(Collections.singletonList(testProduct1));

        OutOfStockException exception = assertThrows(
                OutOfStockException.class,
                () -> orderService.placeOrder(testOrderItems)
        );

        assertEquals(
                "There is not enough stock of Product: Test Product 1, remaining stock: 50",
                exception.getMessage()
        );
        verify(productRepository, never()).save(any(Product.class));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void test_PlaceOrder_FreeDeliveryForLargeOrder() throws Exception {
        testOrderItems.put(1, 6);
        testOrderItems.remove(2);

        when(productRepository.findByProductCk_Id(1))
                .thenReturn(Collections.singletonList(testProduct1));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.placeOrder(testOrderItems);

        assertEquals(0, result.getDeliveryCost());
        verify(productRepository).save(any(Product.class));
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void test_PlaceOrder_DiscountForVeryLargeOrder() throws Exception {
        testOrderItems.put(1, 11);
        testOrderItems.remove(2);

        when(productRepository.findByProductCk_Id(1))
                .thenReturn(Collections.singletonList(testProduct1));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.placeOrder(testOrderItems);

        assertEquals(990.0, result.getOrderCost());
        assertEquals(0, result.getDeliveryCost());
        assertEquals(110, result.getDiscount());
        verify(productRepository).save(any(Product.class));
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void test_GetAllOrders_ReturnsAllOrders() {
        List<Order> expectedOrders = Arrays.asList(new Order(), new Order());
        when(orderRepository.findAll()).thenReturn(expectedOrders);

        List<Order> result = orderService.getAllOrders();

        assertEquals(2, result.size());
        verify(orderRepository).findAll();
    }

    @Test
    void getOrderById_ReturnsOrder() {
        Order expectedOrder = new Order();
        when(orderRepository.findById(1)).thenReturn(Optional.of(expectedOrder));

        Optional<Order> result = orderService.getOrderById(1);

        assertTrue(result.isPresent());
        assertEquals(expectedOrder, result.get());
        verify(orderRepository).findById(1);
    }

    @Test
    void test_GetOrderById_ReturnsEmptyForNonExistingOrder() {
        when(orderRepository.findById(99)).thenReturn(Optional.empty());

        Optional<Order> result = orderService.getOrderById(99);

        assertTrue(result.isEmpty());
        verify(orderRepository).findById(99);
    }
}