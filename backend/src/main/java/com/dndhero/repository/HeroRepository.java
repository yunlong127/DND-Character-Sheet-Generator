package com.dndhero.repository;

import com.dndhero.model.entity.Hero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface HeroRepository extends JpaRepository<Hero, Integer> {
    @Query("SELECT COALESCE(MAX(h.id), 0) FROM Hero h")
    int getMaxId();
}
