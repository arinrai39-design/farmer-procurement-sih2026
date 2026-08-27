package com.sih.procurement.repository;

import com.sih.procurement.entity.Counter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CounterRepository extends JpaRepository<Counter, Long> {
  long countByCentreIdAndActiveFlagTrue(Long centreId);
}
