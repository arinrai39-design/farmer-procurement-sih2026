package com.sih.procurement.repository;

import com.sih.procurement.entity.Farmer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface FarmerRepository extends JpaRepository<Farmer, Long> {
  Optional<Farmer> findByFarmerCode(String farmerCode);
  Optional<Farmer> findByMobile(String mobile);
  Optional<Farmer> findByUserId(Long userId);
}
