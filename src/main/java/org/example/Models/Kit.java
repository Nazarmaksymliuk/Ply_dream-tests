package org.example.Models;

public class Kit {

    public String name;            // Tool Name *
    public String description;     // Tool Description
    public String tags;
    public String location;        // Location *

    public Kit(String name, String description, String tags, String location) {
        this.name = name;
        this.description = description;
        this.tags = tags;
        this.location = location;
    }

    public Kit(String name, String description, String tags) {
        this.name = name;
        this.description = description;
        this.tags = tags;
    }

}
