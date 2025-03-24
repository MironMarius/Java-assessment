package ing.assessment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.GONE)
public class OutOfStockException extends Exception {
    public OutOfStockException(String message) {
      super(message);
    }
}
