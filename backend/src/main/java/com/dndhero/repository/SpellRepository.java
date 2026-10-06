package com.dndhero.repository;

import com.dndhero.model.entity.Spell;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpellRepository extends JpaRepository<Spell, String> {
}
