package com.dndhero.repository;

import com.dndhero.model.entity.Information;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InformationRepository extends JpaRepository<Information, String> {
}
