package com.dndhero.repository;

import com.dndhero.model.entity.SaveTable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaveRepository extends JpaRepository<SaveTable, String> {
}
