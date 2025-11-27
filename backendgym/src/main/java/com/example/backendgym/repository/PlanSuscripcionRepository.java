package com.example.backendgym.repository;

import com.example.backendgym.domain.PlanSuscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlanSuscripcionRepository extends JpaRepository<PlanSuscripcion, Long> {
}
