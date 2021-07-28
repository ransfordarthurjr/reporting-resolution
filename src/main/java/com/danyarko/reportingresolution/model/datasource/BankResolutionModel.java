package com.danyarko.reportingresolution.model.datasource;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "banks_resolutions")
@Entity
@ToString
public class BankResolutionModel implements Serializable {
    public BankResolutionModel(String month, String name, String workbookName,  String worksheetName, String description, String balanceAsAt2018, String receiversAdjustmentAtOpening, String balancesTransferredToCbg, String receiversOpeningBalances, String otherAdjustments, String totalRecoveriesAsAtMonthOfReporting, String estimatedRecoverableAssetsByTheReceiver, String balanceAsAtMonthOfReporting) {
        this.loadTimestamp = LocalDateTime.now();

        this.month = month;
        this.name = name;

        this.workbookName = workbookName;
        this.worksheetName = worksheetName;
        this.description = description;
        this.balanceAsAt2018 = balanceAsAt2018;
        this.receiversAdjustmentAtOpening = receiversAdjustmentAtOpening;
        this.balancesTransferredToCbg = balancesTransferredToCbg;
        this.receiversOpeningBalances = receiversOpeningBalances;
        this.otherAdjustments = otherAdjustments;
        this.totalRecoveriesAsAtMonthOfReporting = totalRecoveriesAsAtMonthOfReporting;
        this.estimatedRecoverableAssetsByTheReceiver = estimatedRecoverableAssetsByTheReceiver;
        this.balanceAsAtMonthOfReporting = balanceAsAtMonthOfReporting;
    }

    public BankResolutionModel(String month, String name, String workbookName,  String worksheetName) {
        this.loadTimestamp = LocalDateTime.now();

        this.month = month;
        this.name = name;

        this.workbookName = workbookName;
        this.worksheetName = worksheetName;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "load_timestamp")
    private LocalDateTime loadTimestamp;

    @Column(name = "month")
    private String month;

    @Column(name = "name")
    private String name;

    @Column(name = "workbook_name")
    private String workbookName;

    @Column(name = "worksheet_name")
    private String worksheetName;

    @Column(name = "description")
    private String description;

    @Column(name = "balance_as_at_2018")
    private String balanceAsAt2018;

    @Column(name = "receivers_adjustment_at_opening")
    private String receiversAdjustmentAtOpening;

    @Column(name = "balances_transferred_to_cbg")
    private String balancesTransferredToCbg;

    @Column(name = "receivers_opening_balances")
    private String receiversOpeningBalances;

    @Column(name = "other_adjustments")
    private String otherAdjustments;

    @Column(name = "total_recoveries_as_at_month_of_reporting")
    private String totalRecoveriesAsAtMonthOfReporting;

    @Column(name = "estimated_recoverable_assets_by_the_receiver")
    private String estimatedRecoverableAssetsByTheReceiver;

    @Column(name = "balance_as_at_month_of_reporting")
    private String balanceAsAtMonthOfReporting;
}
