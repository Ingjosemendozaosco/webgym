package com.example.backendgym.repository;

import com.example.backendgym.domain.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {
    Optional<VerificationCode> findTopByEmailAndPurposeOrderByIdDesc(String email, String purpose);
    @Modifying
    @Transactional
    void deleteByEmailAndPurpose(String email, String purpose);
}
