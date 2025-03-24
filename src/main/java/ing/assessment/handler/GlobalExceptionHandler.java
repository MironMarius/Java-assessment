package ing.assessment.handler;

import ing.assessment.dto.ErrorResponseDTO;
import ing.assessment.exception.InvalidOrderException;
import ing.assessment.exception.OrderNotFoundException;
import ing.assessment.exception.OutOfStockException;
import ing.assessment.exception.ProductNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleProductNotFound(ProductNotFoundException ex) {
        ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.NOT_FOUND.value(),
                "Product not found",
                ex.getMessage()
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleOrderNotFound(OrderNotFoundException ex) {
        ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.NOT_FOUND.value(),
                "Order not found",
                ex.getMessage()
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidOrderException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidOrder(InvalidOrderException ex) {
        ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                "Invalid order",
                ex.getMessage()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(OutOfStockException.class)
    public ResponseEntity<ErrorResponseDTO> handleOutOfStock(OutOfStockException ex) {
        ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.GONE.value(),
                "Out of stock",
                ex.getMessage()
        );
        return new ResponseEntity<>(error, HttpStatus.GONE);
    }
}