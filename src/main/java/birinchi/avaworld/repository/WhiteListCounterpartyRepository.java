package birinchi.avaworld.repository;

import birinchi.avaworld.model.entity.WhiteListCounterparty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WhiteListCounterpartyRepository extends JpaRepository<WhiteListCounterparty, Long> {
}
