package com.codingtest.movieticketbookingsystem.repository;

import com.codingtest.movieticketbookingsystem.domain.city.City;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CityRepository extends JpaRepository<City, Long> {

    Optional<City> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
