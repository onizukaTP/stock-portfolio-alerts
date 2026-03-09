package com.bl.stockportfolioalerts.portfolio.repository;

import com.bl.stockportfolioalerts.portfolio.entity.Holding;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HoldingRepository extends JpaRepository<Holding, Long> {

    void deleteByPortfolio_Id(Long portfolioId);

}