package com.core.constella.api.country.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.core.constella.api.country.domain.Country;
 
public interface CountryRepository extends JpaRepository<Country, Long> {
    boolean existsByNameKo(String nameKo);
} 