package K.K.DataSeeder;

import K.K.Entities.Product;
import K.K.Entities.Role;
import K.K.Enums.ERole;
import K.K.Repositories.ProductRepository;
import K.K.Repositories.RoleRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.UUID;

@Component
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final ProductRepository productRepository;

    public DataSeeder(RoleRepository roleRepository, ProductRepository productRepository) {
        this.roleRepository = roleRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        if (roleRepository.count() == 0) {
            Role userRole = new Role();
            userRole.setName(ERole.USER);

            Role adminRole = new Role();
            adminRole.setName(ERole.ADMIN);

            roleRepository.saveAll(Arrays.asList(userRole, adminRole));
            log.debug("Seeded initial roles into the database via CommandLineRunner.");
        }

        if (productRepository.count() == 0){
            Product product1 = new Product();
            product1.setName("teniska");
            product1.setPrice(BigDecimal.valueOf(20));
            product1.setImageUrl("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRubRjNEKnz3j9y68xRyiFAKYjYWWym8uMg0keZ8jXUwQ&s=10");

            Product product2 = new Product();
            product2.setName("pantalon");
            product2.setPrice(BigDecimal.valueOf(40));
            product2.setImageUrl("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTPgUVmyNQG91Iz9w9a5f5AmYFo4_OiZdJ9uArbhWUFaQ&s=10");

            productRepository.saveAll(Arrays.asList(product1, product2));
            log.debug("Seeded initial roles into the database via CommandLineRunner.");
        }
    }
}
