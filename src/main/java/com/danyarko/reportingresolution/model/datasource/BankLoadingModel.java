package com.danyarko.reportingresolution.model.datasource;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "banks_loader")
@Entity
@ToString
public class BankLoadingModel implements Serializable {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "loading")
    private int loading;
}
