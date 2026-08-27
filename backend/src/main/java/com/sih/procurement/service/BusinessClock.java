package com.sih.procurement.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

@Component
public class BusinessClock {
  private final ZoneId zone;

  public BusinessClock(@Value("${app.business-timezone}") String timezone) {
    this.zone = ZoneId.of(timezone);
  }

  public LocalDate today() {
    return LocalDate.now(zone);
  }
}
