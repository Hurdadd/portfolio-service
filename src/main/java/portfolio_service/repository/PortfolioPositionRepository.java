package portfolio_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import portfolio_service.domain.PortfolioPosition;

import java.util.List;
import java.util.UUID;

public interface PortfolioPositionRepository extends JpaRepository<PortfolioPosition, UUID> {
    List<PortfolioPosition> findByClientId(String clientId);
}
