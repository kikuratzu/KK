package K.K.Services;

import K.K.Entities.Product;
import K.K.K.DTOs.ProductDTO;
import K.K.K.DTOs.SimpleProductDTO;
import K.K.Repositories.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class ProductService {

    private final ProductRepository repository;

    @Autowired
    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<SimpleProductDTO> getAllProducts() {
        return repository.findAll().stream()
                .map(product -> {
                    SimpleProductDTO dto = new SimpleProductDTO();
                    BeanUtils.copyProperties(product, dto);
                    return dto;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public SimpleProductDTO getProductById(final UUID id) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "item with specific id was not found"));
        SimpleProductDTO dto = new SimpleProductDTO();
        BeanUtils.copyProperties(product, dto);
        return dto;
    }



}
