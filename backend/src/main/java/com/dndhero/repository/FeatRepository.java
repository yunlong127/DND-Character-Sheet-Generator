package com.dndhero.repository;

import com.dndhero.model.entity.Feat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeatRepository extends JpaRepository<Feat, String> {
}
