package ai.patchpilot.api.repository;

import ai.patchpilot.api.model.TroubleshootingTicket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TroubleshootingTicketRepository extends JpaRepository<TroubleshootingTicket, Long> {

    List<TroubleshootingTicket> findAllByOrderByCreatedAtDesc();
}
