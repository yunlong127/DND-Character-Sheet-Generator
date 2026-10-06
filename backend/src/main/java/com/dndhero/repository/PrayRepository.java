package com.dndhero.repository;

import com.dndhero.model.entity.Pray;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrayRepository extends JpaRepository<Pray, String> {
}
