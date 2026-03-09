package com.bl.stockportfolioalerts.portfolio.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Holding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ticker;

    private int quantity;

    private double buyingPrice;

    @ManyToOne
    @JsonBackReference
    private Portfolio portfolio;

    @Transient
    public double getValue() {
        return quantity * buyingPrice;
    }
}