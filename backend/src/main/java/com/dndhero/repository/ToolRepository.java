package com.dndhero.repository;

import com.dndhero.model.entity.ToolTable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ToolRepository extends JpaRepository<ToolTable, String> {
}
