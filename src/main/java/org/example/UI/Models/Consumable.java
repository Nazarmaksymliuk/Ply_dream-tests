package org.example.UI.Models;

public class Consumable {
    public String name;
    public String itemNumber;
    public String description;
    public String unitOfMeasurement;
    public double costForBusiness;
    public Double quantity;
    public String tag;
    public String location;


    public Consumable(String name, String itemNumber, String description, String unitOfMeasurement, double costForBusiness, double qty,String location, String tag) {
        this.name = name;
        this.itemNumber = itemNumber;
        this.description = description;
        this.unitOfMeasurement = unitOfMeasurement;
        this.costForBusiness = costForBusiness;
        this.quantity = qty;
        this.location = location;
        this.tag = tag;
    }
    public Consumable(String name, double costForBusiness, double qty) {
        this.name = name;
        this.costForBusiness = costForBusiness;
        this.quantity = qty;
    }
    public Consumable(String name, double costForBusiness) {
        this.name = name;
        this.costForBusiness = costForBusiness;
    }
}
