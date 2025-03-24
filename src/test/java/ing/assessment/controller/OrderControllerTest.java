package ing.assessment.controller;

import ing.assessment.db.order.Order;
import ing.assessment.exception.*;
import ing.assessment.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private Map<Integer, Integer> validOrderRequest;
    private Order mockOrder;

    @BeforeEach
    void setUp() {
        validOrderRequest = new HashMap<>();
        // (productId, quantity)
        validOrderRequest.put(1, 2);
        validOrderRequest.put(2, 1);

        mockOrder = new Order();
        mockOrder.setId(1);
    }

    @Test
    void test_PlaceOrder_ValidRequest_ReturnsCreated() throws Exception {
        when(orderService.placeOrder(any())).thenReturn(mockOrder);

        ResponseEntity<?> response = orderController.placeOrder(validOrderRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockOrder, response.getBody());
        verify(orderService).placeOrder(validOrderRequest);
    }

    @Test
    void test_PlaceOrder_EmptyOrder_ThrowsInvalidOrderException() throws Exception {
        Map<Integer, Integer> emptyOrder = Collections.emptyMap();

        InvalidOrderException exception = assertThrows(
                InvalidOrderException.class,
                () -> orderController.placeOrder(emptyOrder)
        );

        assertEquals("Invalid request, must provide productIds and positive quantities",
                exception.getMessage());
        verify(orderService, never()).placeOrder(any());
    }

    @Test
    void test_PlaceOrder_ZeroQuantity_ThrowsInvalidOrderException() throws Exception {
        Map<Integer, Integer> invalidOrder = new HashMap<>();
        invalidOrder.put(1, 0);

        InvalidOrderException exception = assertThrows(
                InvalidOrderException.class,
                () -> orderController.placeOrder(invalidOrder)
        );

        assertEquals("Invalid request, must provide productIds and positive quantities",
                exception.getMessage());
        verify(orderService, never()).placeOrder(any());
    }

    @Test
    void test_PlaceOrder_ProductNotFound_PropagatesException() throws Exception {
        when(orderService.placeOrder(any()))
                .thenThrow(new ProductNotFoundException("Product not found"));

        assertThrows(
                ProductNotFoundException.class,
                () -> orderController.placeOrder(validOrderRequest)
        );
    }

    @Test
    void test_PlaceOrder_OutOfStock_PropagatesException() throws Exception {
        when(orderService.placeOrder(any()))
                .thenThrow(new OutOfStockException("Out of stock"));

        assertThrows(
                OutOfStockException.class,
                () -> orderController.placeOrder(validOrderRequest)
        );
    }

    @Test
    void test_GetOrder_ExistingOrder_ReturnsOrder() throws OrderNotFoundException {
        when(orderService.getOrderById(1)).thenReturn(Optional.of(mockOrder));

        ResponseEntity<?> response = orderController.getOrder(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockOrder, response.getBody());
    }

    @Test
    void test_GetOrder_NonExistingOrder_ThrowsNotFoundException() {
        when(orderService.getOrderById(99)).thenReturn(Optional.empty());

        OrderNotFoundException exception = assertThrows(
                OrderNotFoundException.class,
                () -> orderController.getOrder(99)
        );

        assertEquals("Order with id: 99 not found", exception.getMessage());
    }

    @Test
    void test_GetAllOrders_ReturnsOrderList() {
        List<Order> orders = Arrays.asList(mockOrder, new Order());
        when(orderService.getAllOrders()).thenReturn(orders);

        ResponseEntity<List<Order>> response = orderController.getAllOrders();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, Objects.requireNonNull(response.getBody()).size());
        verify(orderService).getAllOrders();
    }

    @Test
    void test_GetAllOrders_EmptyList_ReturnsEmptyList() {
        when(orderService.getAllOrders()).thenReturn(Collections.emptyList());

        ResponseEntity<List<Order>> response = orderController.getAllOrders();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).isEmpty());
    }
}