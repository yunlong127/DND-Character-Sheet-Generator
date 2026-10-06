package com.dndhero.repository;

import com.dndhero.model.entity.NameTable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NameTableRepository extends JpaRepository<NameTable, String> {
}
