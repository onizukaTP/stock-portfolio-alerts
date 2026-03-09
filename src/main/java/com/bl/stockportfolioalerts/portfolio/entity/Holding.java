package com.bl.stockportfolioalerts.portfolio.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class Holding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ticker;

    private int quantity;

    private double buyingPrice;

    private double value;

    @ManyToOne
    @JsonBackReference
    private Portfolio portfolio;

    public double getValue() {
        return quantity * buyingPrice;
    }
}