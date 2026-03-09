package com.bl.stockportfolioalerts.portfolio.entity;

import com.bl.stockportfolioalerts.auth.entity.User;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double totalValue;

    @ManyToOne
    private User user;

    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Holding> holdings;
}