package com.codingtest.movieticketbookingsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.codingtest.movieticketbookingsystem.entity.Event;

public interface EventRepository extends JpaRepository<Event, Long> {
}
