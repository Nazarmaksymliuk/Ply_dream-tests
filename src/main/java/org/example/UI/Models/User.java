package org.example.UI.Models;

import net.datafaker.Faker;

import java.util.Random;


public record User(String firstname, String lastname, String BusinessLegalName, String password, String email){
    public static User randomUser() {
        Faker faker = new Faker();

        return new User(
                faker.name().firstName(),
                faker.name().lastName(),
                "AQA-API-Business-" + new Random().nextInt(1000),
                "Test+1234",
                faker.internet().emailAddress()
        );
    }
}
