package K.K.Repositories;

import K.K.Entities.AnonymousOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AnonymousOrderRepository extends JpaRepository<AnonymousOrder, UUID> {
}
