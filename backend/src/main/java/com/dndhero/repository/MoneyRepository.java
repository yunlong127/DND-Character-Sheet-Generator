package com.dndhero.repository;

import com.dndhero.model.entity.Money;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MoneyRepository extends JpaRepository<Money, String> {
}
