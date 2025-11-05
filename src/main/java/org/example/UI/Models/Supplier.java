package org.example.UI.Models;

public class Supplier {
    public String name;
    public String city;
    public String note;
    public String contactName;
    public String contactEmail;
    public String contactPhone;

    public Supplier(String name, String city, String note,
                    String contactName, String contactEmail, String contactPhone) {
        this.name = name;
        this.city = city;
        this.note = note;
        this.contactName = contactName;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
    }
}
