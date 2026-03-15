package com.bl.stockportfolioalerts.alert.entity;

import com.bl.stockportfolioalerts.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ticker;

    private double thresholdPrice;

    @ManyToOne
    private User user;
}