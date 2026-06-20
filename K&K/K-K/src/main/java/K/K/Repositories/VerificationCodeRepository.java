package K.K.Repositories;

import K.K.Entities.VerificationCode;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface VerificationCodeRepository extends CrudRepository<VerificationCode, String> {
    List<VerificationCode> findByUsername(String username);
}
