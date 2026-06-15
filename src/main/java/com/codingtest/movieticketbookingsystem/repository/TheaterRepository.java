package com.codingtest.movieticketbookingsystem.repository;

import com.codingtest.movieticketbookingsystem.domain.theater.Theater;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TheaterRepository extends JpaRepository<Theater, Long> {

    List<Theater> findByCityIdOrderByNameAsc(Long cityId);

    Page<Theater> findByCityIdOrderByNameAsc(Long cityId, Pageable pageable);

    boolean existsByCityIdAndNameIgnoreCase(Long cityId, String name);

    boolean existsByCityId(Long cityId);
}
