package K.K.Repositories;

import K.K.Entities.VerificationCode;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;


public interface VerificationCodeRepository extends CrudRepository<VerificationCode, String> {
Optional<VerificationCode> findById(String code);
}
