package com.dndhero.repository;

import com.dndhero.model.entity.Language;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LanguageRepository extends JpaRepository<Language, String> {
}
