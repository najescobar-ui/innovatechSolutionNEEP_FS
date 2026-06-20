package cl.duoc.innovatech.analytics.repository;

import cl.duoc.innovatech.analytics.entity.KpiSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface KpiSnapshotRepository extends JpaRepository<KpiSnapshot, Long> {

    Optional<KpiSnapshot> findTopByOrderByCapturedAtDesc();

    List<KpiSnapshot> findByCapturedAtAfterOrderByCapturedAtAsc(Instant since);

    List<KpiSnapshot> findByCapturedAtBetweenOrderByCapturedAtAsc(Instant from, Instant to);

    @Query("""
           select s from KpiSnapshot s
           where s.capturedAt <= :at
           order by s.capturedAt desc
           limit 1
           """)
    Optional<KpiSnapshot> findLatestAtOrBefore(@Param("at") Instant at);

    boolean existsByCapturedAtBetween(Instant from, Instant to);
}
