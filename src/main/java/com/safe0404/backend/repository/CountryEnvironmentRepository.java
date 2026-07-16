package com.safe0404.backend.repository;

import com.safe0404.backend.entity.CountryEnvironment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// 국가 종합환경 레포지토리
@Repository
public interface CountryEnvironmentRepository extends JpaRepository<CountryEnvironment, String> {
}
