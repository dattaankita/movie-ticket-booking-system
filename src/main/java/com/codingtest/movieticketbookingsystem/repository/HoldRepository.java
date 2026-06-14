package com.codingtest.movieticketbookingsystem.repository;

import com.codingtest.movieticketbookingsystem.entity.Hold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HoldRepository extends JpaRepository<Hold, String> {}
