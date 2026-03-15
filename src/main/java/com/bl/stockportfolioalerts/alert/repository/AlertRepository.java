package com.bl.stockportfolioalerts.alert.repository;

import com.bl.stockportfolioalerts.alert.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertRepository extends JpaRepository<Alert, Long> {
}