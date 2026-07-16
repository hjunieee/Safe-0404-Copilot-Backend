package com.safe0404.backend.repository;

import com.safe0404.backend.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// 국가 데이터 레포지토리
@Repository
public interface CountryRepository extends JpaRepository<Country, String> {
}
