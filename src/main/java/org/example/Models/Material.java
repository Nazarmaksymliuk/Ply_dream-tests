package org.example.Models;

public class Material {
        public String name;
        public String itemNumber;
        public String description;
        public String brand;
        public String manufacturer;
        public String unitOfMeasurement;
        public String category;
        public String variationName;
        public String variationDescription;
        public double costForClient;
        public double costForBusiness;
        public int quantity;

        public Material(String name, String itemNumber,String description, String brand, String manufacturer, String category,String unitOfMeasurement,String variationName,String variationDescription, double costForClient, double costForBusiness, int quantity) {
            this.name = name;
            this.itemNumber = itemNumber;
            this.description = description;
            this.brand = brand;
            this.manufacturer = manufacturer;
            this.category = category;
            this.unitOfMeasurement = unitOfMeasurement;
            this.variationName = variationName;
            this.variationDescription = variationDescription;
            this.costForClient = costForClient;
            this.costForBusiness = costForBusiness;
            this.quantity = quantity;
        }
            public Material(String name, String itemNumber, double costForClient, double costForBusiness, int quantity) {
            this.name = name;
            this.itemNumber = itemNumber;
            this.unitOfMeasurement = unitOfMeasurement;
            this.variationName = variationName;
            this.costForClient = costForClient;
            this.costForBusiness = costForBusiness;
            this.quantity = quantity;
        }
        public Material(String name, String itemNumber,String unitOfMeasurement,String variation) {
            this.name = name;
            this.itemNumber = itemNumber;
            this.unitOfMeasurement = unitOfMeasurement;
            this.variationName = variation;
        }
        public Material(String name, String itemNumber,String unitOfMeasurement,String variation, int quantity) {
            this.name = name;
            this.itemNumber = itemNumber;
            this.unitOfMeasurement = unitOfMeasurement;
            this.variationName = variation;
            this.quantity = quantity;
        }

    // Конструктор тільки з цінами
    public Material(double costForClient, double costForBusiness) {
        this.costForClient = costForClient;
        this.costForBusiness = costForBusiness;
    }

}
