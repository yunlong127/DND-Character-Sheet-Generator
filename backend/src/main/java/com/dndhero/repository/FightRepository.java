package com.dndhero.repository;

import com.dndhero.model.entity.Fight;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FightRepository extends JpaRepository<Fight, String> {
}
