package com.codingtest.movieticketbookingsystem.repository;

import com.codingtest.movieticketbookingsystem.common.enums.ShowStatus;
import com.codingtest.movieticketbookingsystem.domain.show.Show;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ShowRepository extends JpaRepository<Show, Long> {

    List<Show> findByTheaterIdOrderByStartTimeAsc(Long theaterId);

    Page<Show> findByTheaterIdOrderByStartTimeAsc(Long theaterId, Pageable pageable);

    boolean existsByTheaterId(Long theaterId);

    boolean existsByPricingTier_Id(Long pricingTierId);

    @EntityGraph(attributePaths = {"theater", "theater.city", "pricingTier"})
    @Query("SELECT s FROM Show s WHERE s.id = :id")
    Optional<Show> findByIdWithDetails(@Param("id") Long id);

    @EntityGraph(attributePaths = {"theater", "theater.city", "pricingTier"})
    @Query("""
            SELECT s FROM Show s
            JOIN s.theater t
            WHERE t.city.id = :cityId
              AND s.status = :status
              AND s.startTime >= :start
              AND s.startTime < :end
            """)
    Page<Show> findScheduledShowsByCityAndStartTimeBetween(
            @Param("cityId") Long cityId,
            @Param("status") ShowStatus status,
            @Param("start") Instant start,
            @Param("end") Instant end,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"theater", "theater.city", "pricingTier"})
    @Query("""
            SELECT s FROM Show s
            WHERE s.status = :status
              AND s.startTime >= :fromTime
            """)
    Page<Show> findUpcomingShows(
            @Param("status") ShowStatus status,
            @Param("fromTime") Instant fromTime,
            Pageable pageable
    );
}
