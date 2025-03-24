package ing.assessment.controller;

import ing.assessment.db.product.Product;
import ing.assessment.exception.ProductNotFoundException;
import ing.assessment.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProducts();

        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<List<Product>> getProduct(@PathVariable("id") Integer id) throws ProductNotFoundException {
        List<Product> product = productService.getProductsById(id);

        if (product.isEmpty()) throw new ProductNotFoundException("Product with ID: " + id + " was not found.");

        return ResponseEntity.ok(product);
    }
}