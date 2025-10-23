package org.example.Models;

import java.util.List;

public class ToolUnit {
    // Detailed Information
    public String unitName;        // Unit name *
    public String serialNumber;    // Serial #
    public String status;  // Status (enum)
    public String employee;        // Employee (display name або id)

    // Location & Job
    public String location;        // Location *
    public List<String> jobs;      // Jobs (multi-select, імена чи id)

    // Position at Location
    public String aisle;           // optional
    public String bay;             // optional
    public String level;           // optional
    public String bin;

    public Double purchaseCost;
    public Double unitValue;

    public ToolUnit(String unitName, String serialNumber, String location, double purchaseCost, double unitValue) {
        this.unitName = unitName;
        this.serialNumber = serialNumber;
        this.location = location;
        this.purchaseCost = purchaseCost;
        this.unitValue = unitValue;
    }


}
