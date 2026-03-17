package com.bl.stockportfolioalerts.alert.repository;

import com.bl.stockportfolioalerts.alert.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByTicker(String ticker);
}