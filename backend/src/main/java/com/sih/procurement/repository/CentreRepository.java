package com.sih.procurement.repository;

import com.sih.procurement.entity.ProcurementCentre;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CentreRepository extends JpaRepository<ProcurementCentre, Long> {}
