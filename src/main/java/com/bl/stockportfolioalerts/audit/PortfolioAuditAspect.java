package com.bl.stockportfolioalerts.audit;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class PortfolioAuditAspect {

    @Before("execution(* com.bl.stockportfolioalerts.portfolio.service.PortfolioService.deletePortfolio(..))")
    public void logDeletionAttempt(JoinPoint joinPoint) {

        Object[] args = joinPoint.getArgs();

        Long portfolioId = (Long) args[0];

        log.info("AUDIT: Portfolio deletion attempt detected. portfolioId={}", portfolioId);
    }

    @AfterReturning("execution(* com.bl.stockportfolioalerts.portfolio.service.PortfolioService.deletePortfolio(..))")
    public void logDeletionSuccess(JoinPoint joinPoint) {

        Object[] args = joinPoint.getArgs();

        Long portfolioId = (Long) args[0];

        log.info("AUDIT: Portfolio deleted successfully. portfolioId={}", portfolioId);
    }

    @AfterThrowing(
            pointcut = "execution(* com.bl.stockportfolioalerts.portfolio.service.PortfolioService.deletePortfolio(..))",
            throwing = "ex")
    public void logDeletionFailure(JoinPoint joinPoint, Exception ex) {

        Object[] args = joinPoint.getArgs();

        Long portfolioId = (Long) args[0];

        log.error("AUDIT: Portfolio deletion failed. portfolioId={}, error={}",
                portfolioId, ex.getMessage());
    }
}