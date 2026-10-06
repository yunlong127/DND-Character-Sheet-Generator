package com.dndhero.repository;

import com.dndhero.model.entity.Superspell;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SuperspellRepository extends JpaRepository<Superspell, String> {
}
