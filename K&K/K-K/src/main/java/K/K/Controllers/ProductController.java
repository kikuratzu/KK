package K.K.Controllers;

import K.K.K.DTOs.SimpleProductDTO;
import K.K.Services.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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

    @GetMapping("/getProductById/{id}")
    public ResponseEntity<SimpleProductDTO> getProductById(@PathVariable final UUID id){
        log.info("sent product by Id");
        return ResponseEntity.ok(service.getProductById(id));
    }

}
