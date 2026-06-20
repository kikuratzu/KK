package K.K.Services;

import K.K.K.DTOs.SimpleProductDTO;
import K.K.Repositories.ProductRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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



}
