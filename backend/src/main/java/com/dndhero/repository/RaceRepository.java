package com.dndhero.repository;

import com.dndhero.model.entity.Race;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RaceRepository extends JpaRepository<Race, String> {
}
