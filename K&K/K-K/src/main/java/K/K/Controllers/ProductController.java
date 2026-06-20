package K.K.Controllers;

import K.K.K.DTOs.SimpleProductDTO;
import K.K.Services.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin(origins = {"http://127.0.0.1:5500", "http://localhost:5500"}, allowCredentials = "true")
@RestController
@RequestMapping("api/")
@Slf4j
public class ProductController {

    final ProductService service;

    @Autowired
    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping("/getAllProductData")
    public ResponseEntity<List<SimpleProductDTO>> getAllProductData() {
        log.info("sent all product info");
        return ResponseEntity.ok(service.getAllProducts());

    }

}
