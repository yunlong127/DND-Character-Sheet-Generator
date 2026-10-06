package com.dndhero.repository;

import com.dndhero.model.entity.Career;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CareerRepository extends JpaRepository<Career, String> {
}
