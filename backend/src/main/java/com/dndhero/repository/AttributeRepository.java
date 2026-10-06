package com.dndhero.repository;

import com.dndhero.model.entity.Attribute;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttributeRepository extends JpaRepository<Attribute, String> {
}
