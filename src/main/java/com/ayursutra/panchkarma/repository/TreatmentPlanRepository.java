package com.ayursutra.panchkarma.repository;

import com.ayursutra.panchkarma.entity.TreatmentPlan;
import com.ayursutra.panchkarma.entity.TreatmentPlan.PlanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TreatmentPlanRepository extends JpaRepository<TreatmentPlan, Long> {

    List<TreatmentPlan> findByPatient_IdOrderByCreatedAtDesc(Long patientId);
    List<TreatmentPlan> findByDoctor_IdOrderByCreatedAtDesc(Long doctorId);
    List<TreatmentPlan> findByPatient_IdAndStatusOrderByCreatedAtDesc(Long patientId, PlanStatus status);
    List<TreatmentPlan> findByDoctor_IdAndPatient_IdOrderByCreatedAtDesc(Long doctorId, Long patientId);
}